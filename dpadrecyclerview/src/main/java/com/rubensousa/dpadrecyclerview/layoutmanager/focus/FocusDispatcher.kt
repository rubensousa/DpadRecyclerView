/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.layoutmanager.focus

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller

/**
 * Responsibilities:
 * - Determining which view should take focus based on key events
 * - Requesting focus to views
 */
internal class FocusDispatcher(
    private val layout: LayoutManager,
    private val configuration: LayoutConfiguration,
    private val scroller: LayoutScroller,
    private val layoutInfo: LayoutInfo,
    private val pivotSelector: PivotSelector,
    private val spanFocusFinder: SpanFocusFinder,
) {

    private val addFocusableChildrenRequest = AddFocusableChildrenRequest(layoutInfo)
    private val defaultFocusInterceptor = DefaultFocusInterceptor(
        layoutInfo, configuration
    )
    private var focusInterceptor: FocusInterceptor = defaultFocusInterceptor
    private var parentRecyclerView: RecyclerView? = null
    private var lastFocusRequestTimestamp: Long = 0L
    private var focusSearchDebounceMs: Int? = null

    fun updateParentRecyclerView(childRecyclerView: RecyclerView) {
        var parent: ViewParent? = childRecyclerView.parent
        while (parent != null && parent is ViewGroup) {
            if (parent is RecyclerView) {
                parentRecyclerView = parent
                return
            }
            parent = parent.parent
        }
    }

    fun clearParentRecyclerView() {
        parentRecyclerView = null
    }

    fun updateFocusableDirection(direction: FocusableDirection) {
        focusInterceptor = when (direction) {
            FocusableDirection.CONTINUOUS -> ContinuousFocusInterceptor(layoutInfo)
            FocusableDirection.CIRCULAR -> CircularFocusInterceptor(layoutInfo)
            FocusableDirection.STANDARD -> defaultFocusInterceptor
        }
    }

    fun updateFocusSearchDebounceMs(debounceMs: Int?) {
        focusSearchDebounceMs = debounceMs
    }

    fun getFocusSearchDebounceMs(): Int? {
        return focusSearchDebounceMs
    }


    /**
     * When [RecyclerView.requestFocus] is called, we need to focus the first focusable child
     */
    fun onFocusChanged(gainFocus: Boolean) {
        /**
         * If we gain focus during a search for a new pivot,
         * it means an existing view was deleted and RecyclerView is retaining focus for it.
         * In this case, just ignore and do nothing
         */
        if (gainFocus && scroller.isSearchingPivot()) {
            return
        }

        /**
         * If we gain focus while layout is disabled,
         * we shouldn't do anything since we're still removing Views
         */
        if (gainFocus && !configuration.isLayoutEnabled) {
            return
        }

        // Skip if we didn't gain focus or no view is selected
        if (!gainFocus || pivotSelector.position == RecyclerView.NO_POSITION) {
            return
        }
        var index = pivotSelector.position
        while (index < layout.itemCount) {
            val view = layout.findViewByPosition(index) ?: break
            if (layoutInfo.isViewFocusable(view)) {
                if (!view.hasFocus()) {
                    view.postOnAnimation {
                        pivotSelector.focus(view)
                    }
                }
                break
            }
            index++
        }
    }

    fun focusSelectedView() {
        if (configuration.isFocusSearchDisabled) {
            return
        }
        val view = layoutInfo.findViewByAdapterPosition(pivotSelector.position) ?: return
        if (layoutInfo.isViewFocusable(view) && !view.hasFocus()) {
            pivotSelector.focus(view)
        }
    }

    fun onInterceptFocusSearch(
        recyclerView: DpadRecyclerView?,
        focused: View,
        direction: Int,
    ): View? {
        val currentRecyclerView = recyclerView ?: return focused

        // If the parent RecyclerView does not allow focusing children,
        // just delegate focus to its parent
        if (currentRecyclerView.descendantFocusability == ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
            return currentRecyclerView.parent?.focusSearch(focused, direction)
        }

        if (!configuration.isFocusSearchEnabledDuringAnimations
            && currentRecyclerView.isAnimating
        ) {
            return focused
        }

        // Get the new focus direction and exit early if none is valid
        val focusDirection: FocusDirection = FocusDirection.from(
            direction = direction,
            isVertical = layoutInfo.isVertical(),
            reverseLayout = layoutInfo.shouldReverseLayout()
        ) ?: return focused

        if (isLimitedByFocusSearchDebounce()) {
            return focused
        }

        var newFocusedView: View? = focusInterceptor.findFocus(
            recyclerView = recyclerView,
            focusedView = focused,
            position = pivotSelector.position,
            direction = direction
        )

        // If we found the view using our interceptor, return it immediately
        if (newFocusedView != null) {
            // Check if we can't
            if (configuration.hasMaxPendingAlignments()
                && !scroller.addPendingAlignment(newFocusedView)
            ) {
                return focused
            }
            lastFocusRequestTimestamp = System.currentTimeMillis()
            return newFocusedView
        }

        // Check if we need to fallback to the default interceptor
        if (focusInterceptor !== defaultFocusInterceptor) {
            defaultFocusInterceptor.findFocus(
                recyclerView, focused, pivotSelector.position, direction
            )?.let { view ->
                scroller.addPendingAlignment(view)
                lastFocusRequestTimestamp = System.currentTimeMillis()
                return view
            }
        }

        val isScrolling = currentRecyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE
        when (focusDirection) {
            FocusDirection.NEXT_ROW -> {
                if (isScrolling || !configuration.focusOutBack) {
                    newFocusedView = focused
                }
                if (configuration.isScrollEnabled
                    && configuration.maxPendingMoves > 0
                    && scroller.addScrollMovement(forward = true)
                ) {
                    newFocusedView = focused
                }
            }

            FocusDirection.PREVIOUS_ROW -> {
                if (isScrolling || !configuration.focusOutFront) {
                    newFocusedView = focused
                }
                if (configuration.isScrollEnabled
                    && configuration.maxPendingMoves > 0
                    && scroller.addScrollMovement(forward = false)
                ) {
                    newFocusedView = focused
                }
            }

            FocusDirection.NEXT_COLUMN -> {
                if (isScrolling || !configuration.focusOutSideBack) {
                    newFocusedView = focused
                }
            }

            FocusDirection.PREVIOUS_COLUMN -> {
                if (isScrolling || !configuration.focusOutSideFront) {
                    newFocusedView = focused
                }
            }
        }
        lastFocusRequestTimestamp = System.currentTimeMillis()
        if (newFocusedView != null) {
            return newFocusedView
        }
        newFocusedView = currentRecyclerView.parent?.focusSearch(focused, direction)
        return newFocusedView ?: focused
    }

    private fun isLimitedByFocusSearchDebounce(): Boolean {
        val currentDebounceValue = focusSearchDebounceMs ?: return false
        val currentTime = System.currentTimeMillis()
        return currentTime - lastFocusRequestTimestamp < currentDebounceValue
    }

    private fun isFocusSearchEnabled(recyclerView: RecyclerView): Boolean {
        if (configuration.isFocusSearchDisabled(recyclerView)) {
            return false
        }
        // Check if this RecyclerView is a Nested RecyclerView and delay focus changes
        // until the parent is no longer smooth scrolling
        val isParentSmoothScrolling = parentRecyclerView
            ?.layoutManager?.isSmoothScrolling ?: return true

        return !isParentSmoothScrolling
    }

    fun onAddFocusables(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int,
    ): Boolean {
        if (configuration.isFocusSearchDisabled(recyclerView)) {
            if (recyclerView.isFocusable) {
                views.add(recyclerView)
            }
            return true
        }
        if (recyclerView.hasFocus()) {
            // Don't keep adding new views if we're already searching for a pivot
            if (scroller.isSearchingPivot()) {
                return true
            }
            addFocusableChildren(
                recyclerView,
                views,
                direction = direction,
                focusableMode = focusableMode
            )
            return true
        }
        val focusableCount = views.size
        layoutInfo.findViewByPosition(pivotSelector.position)
            ?.addFocusables(views, direction, focusableMode)
        // if still cannot find any, fall through and add itself
        if (views.size != focusableCount) {
            return true
        }
        if (recyclerView.isFocusable) {
            views.add(recyclerView)
        }
        return true
    }

    /**
     * Request focus to the current pivot if it exists
     */
    fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?,
    ): Boolean {
        if (configuration.isFocusSearchDisabled) return false
        val view = layout.findViewByPosition(pivotSelector.position) ?: return false
        return view.requestFocus(direction, previouslyFocusedRect)
    }

    fun onRequestChildFocus(recyclerView: RecyclerView, child: View, focused: View?) {
        if (!isFocusSearchEnabled(recyclerView)) {
            return
        }
        val childPosition = layoutInfo.getAdapterPositionOf(child)
        // This could be the last view in DISAPPEARING animation, so ignore immediately
        if (childPosition == RecyclerView.NO_POSITION) {
            return
        }
        spanFocusFinder.save(childPosition, configuration.spanSizeLookup)
        val canScrollToView = !scroller.isSelectionInProgress && !layoutInfo.isLayoutInProgress
        if (canScrollToView) {
            scroller.scrollToView(
                child, focused, configuration.isSmoothFocusChangesEnabled, requestFocus = true
            )
        }
        pivotSelector.onChildFocused(focused)
    }

    private fun addFocusableChildren(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int,
    ) {
        if (layout.childCount == 0) {
            // No need to continue since there's no children
            return
        }

        val focused: View? = recyclerView.findFocus()
        var focusedAdapterPosition = RecyclerView.NO_POSITION
        if (focused != null) {
            focusedAdapterPosition = layoutInfo.getChildViewHolder(focused)
                ?.absoluteAdapterPosition ?: RecyclerView.NO_POSITION
        }

        // Even if focusedPosition != NO_POSITION, findViewByPosition could return null if the view
        // is ignored or getLayoutPosition does not match the adapter position of focused view.
        val focusedChild: View? = layout.findViewByPosition(focusedAdapterPosition)
        focusedChild?.addFocusables(views, direction, focusableMode)

        val focusDirection = FocusDirection.from(
            direction = direction,
            isVertical = configuration.isVertical(),
            reverseLayout = layoutInfo.shouldReverseLayout()
        ) ?: return

        if ((focusDirection == FocusDirection.NEXT_COLUMN
                    || focusDirection == FocusDirection.PREVIOUS_COLUMN)
            && configuration.spanCount == 1
        ) {
            // In single spans we cannot navigate to previous/next columns.
            return
        }

        if (focusSpan(
                focusedAdapterPosition,
                focusDirection,
                views,
                direction,
                focusableMode
            )
        ) {
            return
        }

        if (focusedChild == null) {
            return
        }

        val focusedChildIndex = layoutInfo.findIndexOf(focusedChild)
        addFocusableChildrenRequest.update(
            focusedChild = focusedChild,
            focusedChildIndex = focusedChildIndex,
            focusedAdapterPosition = focusedAdapterPosition,
            focusDirection = focusDirection
        )

        addFocusableChildren(
            request = addFocusableChildrenRequest,
            views = views,
            direction = direction,
            focusableMode = focusableMode
        )
    }

    private fun addFocusableChildren(
        request: AddFocusableChildrenRequest,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int,
    ) {
        var index = request.start
        val increment = request.increment
        // Add focusables of neighbour depending on the focus search direction.
        while ((index <= request.end && increment > 0) || (index >= request.end && increment < 0)) {
            val child = layout.getChildAt(index)
            if (child == null || !layoutInfo.isViewFocusable(child)) {
                index += increment
                continue
            }
            // Exit early if we don't have any focused view yet
            if (request.focused == null) {
                child.addFocusables(views, direction, focusableMode)
                break
            }
            val position = layoutInfo.getAdapterPositionOf(child)
            // View can be outdated at this point, so ignore if position is invalid
            if (position >= 0) {
                val spanIndex = layoutInfo.getStartSpanIndex(position)
                if (request.focusDirection == FocusDirection.NEXT_ROW) {
                    child.addFocusables(views, direction, focusableMode)
                } else if (request.focusDirection == FocusDirection.PREVIOUS_ROW) {
                    child.addFocusables(views, direction, focusableMode)
                } else if (request.focusDirection == FocusDirection.NEXT_COLUMN) {
                    // Add all focusable items after this item whose row index is bigger
                    if (spanIndex == request.focusedSpanIndex) {
                        index += increment
                        continue
                    } else if (spanIndex < request.focusedSpanIndex) {
                        break
                    }
                    child.addFocusables(views, direction, focusableMode)
                } else if (request.focusDirection == FocusDirection.PREVIOUS_COLUMN) {
                    // Add all focusable items before this item whose column index is smaller
                    if (spanIndex == request.focusedSpanIndex) {
                        index += increment
                        continue
                    } else if (spanIndex > request.focusedSpanIndex) {
                        break
                    }
                    child.addFocusables(views, direction, focusableMode)
                }
            }
            index += increment
        }
    }

    private fun focusSpan(
        focusedPosition: Int,
        movement: FocusDirection,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int,
    ): Boolean {
        if (configuration.spanCount == 1 || focusedPosition == RecyclerView.NO_POSITION) {
            return false
        }

        if (movement == FocusDirection.NEXT_COLUMN || movement == FocusDirection.PREVIOUS_COLUMN) {
            return focusNextSpanColumn(
                focusedPosition = focusedPosition,
                next = if (!layoutInfo.shouldReverseLayout()) {
                    movement == FocusDirection.NEXT_COLUMN
                } else {
                    movement == FocusDirection.PREVIOUS_COLUMN
                },
                views = views,
                direction = direction,
                focusableMode = focusableMode
            )
        }

        val reverseLayout = layoutInfo.shouldReverseLayout()
        val edgeView = if (movement == FocusDirection.NEXT_ROW != reverseLayout) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
        if (edgeView == null) {
            return false
        }
        val edgePosition = layout.getPosition(edgeView)
        var nextPosition: Int = focusedPosition
        do {
            nextPosition = spanFocusFinder.findNextSpanPosition(
                focusedPosition = nextPosition,
                spanSizeLookup = configuration.spanSizeLookup,
                forward = movement == FocusDirection.NEXT_ROW,
                edgePosition = edgePosition,
                reverseLayout = layoutInfo.shouldReverseLayout()
            )
            val nextView = layout.findViewByPosition(nextPosition) ?: return false
            if (nextView.hasFocusable()) {
                nextView.addFocusables(views, direction, focusableMode)
                return true
            }
        } while (nextPosition != RecyclerView.NO_POSITION)
        return false

    }

    private fun focusNextSpanColumn(
        focusedPosition: Int,
        next: Boolean,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int,
    ): Boolean {
        val positionIncrement = layoutInfo.getPositionIncrement(next)
        val nextPosition = focusedPosition + positionIncrement
        if (nextPosition < 0 || nextPosition >= layout.itemCount) {
            return false
        }
        val focusedRow = layoutInfo.getSpanGroupIndex(focusedPosition)
        val nextRow = layoutInfo.getSpanGroupIndex(focusedPosition + positionIncrement)
        if (focusedRow != nextRow) {
            // Consume the focus since we can only focus within the same row here
            return true
        }
        val nextView = layout.findViewByPosition(nextPosition) ?: return false
        if (nextView.hasFocusable()) {
            nextView.addFocusables(views, direction, focusableMode)
            return true
        }
        return true
    }

    class AddFocusableChildrenRequest(private val layoutInfo: LayoutInfo) {

        var start: Int = 0
            private set

        var end: Int = 0
            private set

        var increment: Int = 1
            private set

        var focused: View? = null
            private set

        var focusedAdapterPosition: Int = RecyclerView.NO_POSITION
            private set

        var focusDirection: FocusDirection = FocusDirection.NEXT_ROW
            private set

        var focusedSpanIndex: Int = RecyclerView.NO_POSITION
            private set

        fun update(
            focusedChild: View?,
            focusedChildIndex: Int,
            focusedAdapterPosition: Int,
            focusDirection: FocusDirection,
        ) {
            this.focused = focusedChild
            this.focusedAdapterPosition = focusedAdapterPosition
            this.focusDirection = focusDirection
            this.focusedSpanIndex = if (focusedChild != null) {
                layoutInfo.getStartSpanIndex(focusedAdapterPosition)
            } else {
                RecyclerView.NO_POSITION
            }

            increment = if (focusDirection == FocusDirection.NEXT_ROW
                || focusDirection == FocusDirection.NEXT_COLUMN
            ) {
                1
            } else {
                -1
            }
            if (layoutInfo.shouldReverseLayout()
                && (focusDirection == FocusDirection.NEXT_ROW
                        || focusDirection == FocusDirection.PREVIOUS_ROW)
            ) {
                increment *= -1
            }
            end = if (increment > 0) {
                layoutInfo.getChildCount() - 1
            } else {
                0
            }
            start = if (focusedChildIndex == RecyclerView.NO_POSITION) {
                if (increment > 0) {
                    0
                } else {
                    layoutInfo.getChildCount() - 1
                }
            } else if (layoutInfo.shouldReverseLayout()) {
                focusedChildIndex - increment
            } else {
                focusedChildIndex + increment
            }
        }

    }

}
