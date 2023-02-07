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
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
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
    private val pivotSelector: PivotSelector
) {

    private val spanFocusFinder = SpanFocusFinder()
    private val addFocusableChildrenRequest = AddFocusableChildrenRequest(layoutInfo)
    private val defaultFocusInterceptor = DefaultFocusInterceptor(
        layoutInfo, configuration
    )
    private var focusInterceptor: FocusInterceptor = defaultFocusInterceptor
    private var parentRecyclerView: RecyclerView? = null

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

    fun resetSpanFocusCache(spanCount: Int) {
        spanFocusFinder.reset(spanCount)
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
        // Skip if we didn't gain focus or no view is selected
        if (!gainFocus || pivotSelector.position == RecyclerView.NO_POSITION) {
            return
        }
        var index = pivotSelector.position
        while (index < layout.itemCount) {
            val view = layout.findViewByPosition(index) ?: break
            if (layoutInfo.isViewFocusable(view)) {
                view.requestFocus()
                break
            }
            index++
        }
    }

    fun onInterceptFocusSearch(recyclerView: RecyclerView?, focused: View, direction: Int): View? {
        val currentRecyclerView = recyclerView ?: return focused

        if (!isFocusSearchEnabled(currentRecyclerView)) {
            return focused
        }

        if (scroller.hasReachedPendingAlignmentLimit()) {
            return focused
        }

        var newFocusedView: View? = focusInterceptor.findFocus(
            recyclerView, focused,
            pivotSelector.position, direction
        )

        // If we found the view using our interceptor, return it immediately
        if (newFocusedView != null) {
            scroller.addPendingAlignment(newFocusedView)
            return newFocusedView
        }

        // Check if we need to fallback to the default interceptor
        if (focusInterceptor !== defaultFocusInterceptor) {
            defaultFocusInterceptor.findFocus(
                recyclerView, focused, pivotSelector.position, direction
            )?.let { view ->
                scroller.addPendingAlignment(view)
                return view
            }
        }

        // Get the new focus direction and exit early if none is valid
        val focusDirection: FocusDirection = FocusDirection.from(
            direction = direction,
            isVertical = layoutInfo.isVertical(),
            reverseLayout = layoutInfo.shouldReverseLayout()
        ) ?: return focused


        // If the parent RecyclerView does not allow focusing children,
        // just delegate focus to its parent
        if (currentRecyclerView.descendantFocusability == ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
            return currentRecyclerView.parent?.focusSearch(focused, direction)
        }

        val isScrolling = currentRecyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE
        when (focusDirection) {
            FocusDirection.NEXT_ITEM -> {
                if (isScrolling || !configuration.focusOutBack) {
                    newFocusedView = focused
                }
                scroller.addScrollMovement(forward = true)
                if (!layoutInfo.hasCreatedLastItem()) {
                    newFocusedView = focused
                }
            }
            FocusDirection.PREVIOUS_ITEM -> {
                if (isScrolling || !configuration.focusOutFront) {
                    newFocusedView = focused
                }
                scroller.addScrollMovement(forward = false)
                if (!layoutInfo.hasCreatedFirstItem()) {
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
            else -> {}
        }
        if (newFocusedView != null) {
            return newFocusedView
        }
        newFocusedView = currentRecyclerView.parent?.focusSearch(focused, direction)
        return newFocusedView ?: focused
    }

    private fun isFocusSearchEnabled(recyclerView: RecyclerView): Boolean {
        if (configuration.isFocusSearchDisabled) {
            return false
        }
        if (!configuration.isFocusSearchEnabledDuringAnimations && recyclerView.isAnimating) {
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
        focusableMode: Int
    ): Boolean {
        if (configuration.isFocusSearchDisabled) {
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
    fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        val view = layout.findViewByPosition(pivotSelector.position) ?: return false
        return view.requestFocus(direction, previouslyFocusedRect)
    }

    fun onRequestChildFocus(
        recyclerView: RecyclerView,
        child: View,
        focused: View?
    ): Boolean {
        if (!isFocusSearchEnabled(recyclerView)) {
            return true
        }
        val childPosition = layoutInfo.getAdapterPositionOf(child)
        // This could be the last view in DISAPPEARING animation, so ignore immediately
        if (childPosition == RecyclerView.NO_POSITION) {
            return true
        }
        spanFocusFinder.updateFocus(childPosition, configuration.spanSizeLookup)
        val canScrollToView = !scroller.isSelectionInProgress && !layoutInfo.isLayoutInProgress
        if (canScrollToView) {
            scroller.scrollToView(
                child, focused, configuration.isSmoothFocusChangesEnabled, requestFocus = true
            )
        }
        return true
    }

    private fun addFocusableChildren(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ) {
        if (layout.childCount == 0) {
            // No need to continue since there's no children
            return
        }

        val focused: View? = recyclerView.findFocus()
        val focusedChildIndex = layoutInfo.findIndexOf(focused)
        val focusedAdapterPosition = layoutInfo.getAdapterPositionOfChildAt(focusedChildIndex)
        // Even if focusedPosition != NO_POSITION, findViewByPosition could return null if the view
        // is ignored or getLayoutPosition does not match the adapter position of focused view.
        val focusedChild: View? = if (focusedAdapterPosition != RecyclerView.NO_POSITION) {
            layout.findViewByPosition(focusedAdapterPosition)
        } else {
            null
        }
        // Add focusables of focused item.
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
        focusableMode: Int
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
            // if there wasn't any focused item, add the very first focusable
            // items and stop.
            if (request.focused == null) {
                child.addFocusables(views, direction, focusableMode)
                index += increment
                continue
            }
            val position = layoutInfo.getAdapterPositionOf(child)
            val spanIndex = layoutInfo.getStartSpanIndex(position)
            if (request.focusDirection == FocusDirection.NEXT_ITEM) {
                // Add first focusable item on the same row
                if (position > request.focusedAdapterPosition) {
                    child.addFocusables(views, direction, focusableMode)
                }
            } else if (request.focusDirection == FocusDirection.PREVIOUS_ITEM) {
                // Add first focusable item on the same row
                if (position < request.focusedAdapterPosition) {
                    child.addFocusables(views, direction, focusableMode)
                }
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
            index += increment
        }
    }

    private fun focusSpan(
        focusedPosition: Int,
        movement: FocusDirection,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ): Boolean {
        if (configuration.spanCount == 1
            || configuration.spanSizeLookup === DpadSpanSizeLookup.DEFAULT
            || (movement != FocusDirection.PREVIOUS_ITEM && movement != FocusDirection.NEXT_ITEM)
        ) {
            return false
        }
        val reverseLayout = layoutInfo.shouldReverseLayout()
        val edgeView = if (movement != FocusDirection.NEXT_ITEM != reverseLayout) {
            layoutInfo.getChildClosestToStart()
        } else {
            layoutInfo.getChildClosestToEnd()
        }
        if (edgeView == null) {
            return false
        }
        val edgePosition = layout.getPosition(edgeView)
        val nextPosition = spanFocusFinder.findNextSpanPosition(
            focusedPosition,
            spanSizeLookup = configuration.spanSizeLookup,
            forward = movement == FocusDirection.NEXT_ITEM,
            edgePosition = edgePosition,
            reverseLayout = layoutInfo.shouldReverseLayout()
        )
        if (nextPosition == RecyclerView.NO_POSITION) {
            return false
        }
        val newView = layout.findViewByPosition(nextPosition) ?: return false
        newView.addFocusables(views, direction, focusableMode)
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

        var focusDirection: FocusDirection = FocusDirection.NEXT_ITEM
            private set

        var focusedSpanIndex: Int = RecyclerView.NO_POSITION
            private set

        fun update(
            focusedChild: View?,
            focusedChildIndex: Int,
            focusedAdapterPosition: Int,
            focusDirection: FocusDirection
        ) {
            this.focused = focusedChild
            this.focusedAdapterPosition = focusedAdapterPosition
            this.focusDirection = focusDirection
            this.focusedSpanIndex = if (focusedChild != null) {
                layoutInfo.getStartSpanIndex(focusedAdapterPosition)
            } else {
                RecyclerView.NO_POSITION
            }

            increment = if (focusDirection == FocusDirection.NEXT_ITEM
                || focusDirection == FocusDirection.NEXT_COLUMN
            ) {
                1
            } else {
                -1
            }
            if (layoutInfo.shouldReverseLayout()
                && (focusDirection == FocusDirection.NEXT_ITEM
                        || focusDirection == FocusDirection.PREVIOUS_ITEM)
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
