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

package com.rubensousa.dpadrecyclerview.layoutmanager.scroll

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.FocusDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.SpanFocusFinder
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class LayoutScroller(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration,
    private val pivotSelector: PivotSelector,
    private val spanFocusFinder: SpanFocusFinder
) {

    companion object {
        const val TAG = "LayoutScroller"
    }

    var isSelectionInProgress = false
        private set

    private var recyclerView: RecyclerView? = null
    private var pivotSelectionScroller: PivotSelectionSmoothScroller? = null
    private var searchPivotScroller: SearchPivotSmoothScroller? = null
    private val alignmentQueue = ScrollAlignmentQueue(configuration, layoutAlignment, layoutInfo)
    private val idleScrollListener = IdleScrollListener()
    private val searchPivotListener = SearchPivotListener()
    private val selectionPivotListener = SelectionPivotListener()

    fun setRecyclerView(newRecyclerView: RecyclerView?) {
        recyclerView?.removeOnScrollListener(idleScrollListener)
        newRecyclerView?.addOnScrollListener(idleScrollListener)
        recyclerView = newRecyclerView
    }

    /**
     * We can only scroll to views if they exist when this is called
     * and there's no pending layout request.
     *
     * If the view doesn't exist and there's no pending layout request,
     * we use [PivotSelectionSmoothScroller] to scroll until it's found.
     *
     * Otherwise, the selection is deferred to the next layout pass.
     */
    fun scrollToPosition(
        position: Int,
        subPosition: Int,
        smooth: Boolean
    ) {
        val view = layoutManager.findViewByPosition(position)

        // Check if we can immediately scroll to the existing view
        if (!layoutManager.isSmoothScrolling
            && view != null
            && isScrollToViewPossible(position, view)
        ) {
            startScroll(position, subPosition, view, smooth)
            return
        }

        // If layout is disabled, skip scrolling and just update the new pivot
        if (!configuration.isLayoutEnabled) {
            pivotSelector.update(position, subPosition)
            pivotSelector.disablePositionOffset()
            return
        }

        // Otherwise update the selection and start searching for the new pivot
        if (smooth && recyclerView?.isLayoutRequested == false) {
            pivotSelector.update(position, subPosition)
            pivotSelector.disablePositionOffset()
            startSmoothScrollingToPivot(position, subPosition)
            return
        }

        if (layoutManager.isSmoothScrolling) {
            stopSmoothScrolling()
        }

        if (view != null && isScrollToViewPossible(position, view)) {
            startScroll(position, subPosition, view, smooth)
            return
        }

        /**
         * If we reach this point, it means the View doesn't exist at this position,
         * so we need to update the pivot state and request layout to align it
         */
        scrollToPosition(position, subPosition)
    }

    /**
     * We can only start a new scroll if the item at the position is still valid
     * and no layout request was made.
     * If there's a pending layout request, we defer the selection to the next layout pass.
     */
    private fun isScrollToViewPossible(
        position: Int,
        view: View
    ): Boolean {
        return recyclerView?.isLayoutRequested == false
                && layoutInfo.getAdapterPositionOf(view) == position
    }

    private fun startSmoothScrollingToPivot(
        position: Int,
        subPosition: Int
    ) {
        if (!layoutInfo.hasLaidOutViews) {
            Log.w(
                DpadRecyclerView.TAG,
                "smooth scrolling is not supported when there are no views in the layout"
            )
            return
        }
        recyclerView?.let { nonNullRecyclerView ->
            layoutManager.startSmoothScroll(
                PivotSelectionSmoothScroller(
                    nonNullRecyclerView,
                    position,
                    subPosition,
                    layoutInfo,
                    layoutAlignment,
                    selectionPivotListener
                )
            )
            layoutInfo.setIsScrollingToTarget(true)
        }
    }

    private fun stopSmoothScrolling() {
        cancelSmoothScroller()
        recyclerView?.stopScroll()
    }

    private fun startScroll(
        position: Int,
        subPosition: Int,
        view: View,
        smooth: Boolean
    ) {
        isSelectionInProgress = true
        val subPositionView = layoutAlignment.getViewAtSubPosition(view, subPosition)
        if (subPositionView == null && subPosition != 0) {
            Log.w(
                DpadRecyclerView.TAG,
                "Subposition $position doesn't exist for position $position," +
                        "scroll instead started for subposition 0"
            )
        }
        scrollToView(
            view,
            subPositionView,
            smooth,
            requestFocus = layoutManager.hasFocus()
        )
        isSelectionInProgress = false
    }

    fun scrollToSelectedPosition(
        smooth: Boolean,
        requestFocus: Boolean = layoutManager.hasFocus()
    ) {
        val itemCount = layoutManager.itemCount
        var targetPosition = pivotSelector.position
        var targetSubPosition = pivotSelector.subPosition
        if (itemCount == 0) {
            targetPosition = 0
            targetSubPosition = 0
        } else if (targetPosition >= itemCount) {
            targetPosition = itemCount - 1
            targetSubPosition = 0
        } else if (targetPosition == RecyclerView.NO_POSITION) {
            targetPosition = 0
            targetSubPosition = 0
        }
        val view = layoutManager.findViewByPosition(targetPosition) ?: return
        scrollToView(
            view, layoutAlignment.getViewAtSubPosition(view, targetSubPosition),
            smooth, requestFocus
        )
    }

    fun scrollToPosition(position: Int, subPosition: Int = 0) {
        if (pivotSelector.update(position, subPosition)) {
            pivotSelector.disablePositionOffset()
            pivotSelector.setSelectionUpdatePending()
            layoutManager.requestLayout()
        }
    }

    fun setSmoothScroller(smoothScroller: RecyclerView.SmoothScroller) {
        pivotSelectionScroller = if (smoothScroller.isRunning
            && smoothScroller is PivotSelectionSmoothScroller
        ) {
            smoothScroller
        } else {
            null
        }
        searchPivotScroller = if (smoothScroller.isRunning
            && smoothScroller is SearchPivotSmoothScroller
        ) {
            smoothScroller
        } else {
            null
        }
    }

    fun isSearchingPivot() = searchPivotScroller != null

    fun cancelSmoothScroller() {
        layoutInfo.setIsScrollingToTarget(false)
        searchPivotScroller?.cancel()
        searchPivotScroller = null
        pivotSelectionScroller?.cancel()
        pivotSelectionScroller = null
    }

    fun addScrollMovement(forward: Boolean, consume: Boolean = false): Boolean {
        // Skip action if there's no need to scroll already
        if (!layoutInfo.shouldReverseLayout()) {
            when {
                forward && layoutInfo.hasCreatedLastItem() -> {
                    if (!layoutInfo.isLoopingAllowed) {
                        return false
                    }
                }
                !forward && layoutInfo.hasCreatedFirstItem() -> {
                    if (!layoutInfo.isLoopingStart) {
                        return false
                    }
                }
            }
        } else {
            when {
                forward && layoutInfo.hasCreatedFirstItem() -> {
                    if (!layoutInfo.isLoopingAllowed) {
                        return false
                    }
                }
                !forward && layoutInfo.hasCreatedLastItem() -> {
                    if (!layoutInfo.isLoopingStart) {
                        return false
                    }
                }
            }
        }
        val currentRecyclerView = recyclerView ?: return false
        if (searchPivotScroller == null) {
            val newSmoothScroller = SearchPivotSmoothScroller(
                currentRecyclerView,
                configuration.maxPendingMoves,
                layoutInfo,
                spanFocusFinder,
                pivotSelector,
                layoutAlignment,
                searchPivotListener
            )
            layoutAlignment.updateScrollLimits()
            pivotSelector.resetPositionOffset()
            newSmoothScroller.addScrollMovement(forward)
            layoutManager.startSmoothScroll(newSmoothScroller)
        } else {
            searchPivotScroller?.addScrollMovement(forward)
        }
        if (consume) {
            searchPivotScroller?.consumeOneMovement()
        }
        return true
    }

    fun scrollToView(view: View?, subPositionView: View?, smooth: Boolean, requestFocus: Boolean) {
        val newPosition = if (view == null) {
            RecyclerView.NO_POSITION
        } else {
            layoutInfo.getAdapterPositionOf(view)
        }
        if (newPosition == RecyclerView.NO_POSITION) {
            return
        }
        val newSubPosition = layoutAlignment.getSubPositionOfView(view, subPositionView)
        val selectionChanged = pivotSelector.update(newPosition, newSubPosition)
        var selectViewHolder = false
        if (selectionChanged) {
            pivotSelector.resetPositionOffset()
            if (!layoutInfo.isLayoutInProgress) {
                selectViewHolder = true
            } else {
                pivotSelector.setSelectionUpdatePending()
            }
            if (configuration.isChildDrawingOrderEnabled) {
                recyclerView?.invalidate()
            }
        }
        if (view == null) {
            return
        }

        if (subPositionView != null && requestFocus) {
            subPositionView.requestFocus()
        } else if (requestFocus) {
            view.requestFocus()
        }
        performScrollToView(view, subPositionView, selectViewHolder, smooth)
    }

    private fun performScrollToView(
        view: View,
        subPositionView: View?,
        selectViewHolder: Boolean,
        smooth: Boolean
    ) {
        var scrollOffset = 0

        if (configuration.isScrollEnabled) {
            scrollOffset = layoutAlignment.calculateScrollOffset(view, subPositionView)
            scrollBy(scrollOffset, smooth)
        }

        if (selectViewHolder) {
            pivotSelector.dispatchViewHolderSelected()
            // If we didn't scroll, dispatch aligned event already
            if (scrollOffset == 0) {
                pivotSelector.dispatchViewHolderSelectedAndAligned()
            }
        }
    }

    private fun scrollBy(offset: Int, smooth: Boolean) {
        if (offset == 0) {
            return
        }
        if (layoutInfo.isLayoutInProgress) {
            // Ignore scroll actions during layout since these are already handled
            return
        }
        var scrollX = 0
        var scrollY = 0
        if (layoutInfo.isHorizontal()) {
            scrollX = offset
        } else {
            scrollY = offset
        }
        if (smooth) {
            recyclerView?.smoothScrollBy(scrollX, scrollY)
        } else {
            recyclerView?.scrollBy(scrollX, scrollY)
        }
    }

    fun onChildCreated(view: View) {
        searchPivotScroller?.onChildCreated(view)
    }

    fun onChildLaidOut(view: View) {
        searchPivotScroller?.onChildLaidOut(view)
    }

    fun onBlockLaidOut() {
        searchPivotScroller?.onBlockLaidOut()
    }

    fun hasReachedPendingAlignmentLimit(focusDirection: FocusDirection): Boolean {
        return alignmentQueue.hasReachedLimit(focusDirection)
    }

    fun addPendingAlignment(newFocusedView: View): Boolean {
        val viewHolder = layoutInfo.getChildViewHolder(newFocusedView) ?: return false
        val parentView = viewHolder.itemView
        val subView = if (newFocusedView !== parentView) {
            newFocusedView
        } else {
            null
        }
        val scrollOffset = layoutAlignment.calculateScrollOffset(parentView, subView)
        return alignmentQueue.push(parentView, subView, scrollOffset)
    }

    private inner class SearchPivotListener : SearchPivotSmoothScroller.Listener {

        override fun onPivotAttached(adapterPosition: Int) {
            pivotSelector.update(adapterPosition)
        }

        override fun onPivotLaidOut(pivotView: View) {
            if (layoutManager.hasFocus()) {
                isSelectionInProgress = true
                pivotView.requestFocus()
                isSelectionInProgress = false
            }
            addPendingAlignment(pivotView)
            pivotSelector.dispatchViewHolderSelected()
        }

        override fun onPivotFound(pivotView: View) {
            scrollToView(
                pivotView,
                subPositionView = null,
                smooth = true,
                requestFocus = layoutManager.hasFocus()
            )
        }

        override fun onPivotNotFound(targetPosition: Int) {
            // If a new pivot didn't exist in the layout, just scroll back to the previous one
            if (targetPosition >= 0) {
                scrollToPosition(targetPosition, subPosition = 0, smooth = true)
            } else {
                scrollToSelectedPosition(smooth = false)
            }
        }

        override fun onSmoothScrollerStopped() {
            searchPivotScroller = null
        }

    }

    private inner class SelectionPivotListener : PivotSelectionSmoothScroller.Listener {

        override fun onPivotFound(pivotView: View, position: Int, subPosition: Int) {
            if (layoutManager.hasFocus()) {
                isSelectionInProgress = true
                val subPositionView = layoutAlignment.getViewAtSubPosition(
                    pivotView, subPosition
                )
                subPositionView?.requestFocus() ?: pivotView.requestFocus()
                isSelectionInProgress = false
            }
            pivotSelector.dispatchViewHolderSelected()
        }

        /**
         * If the smooth scroller didn't find the target view for whatever reason,
         * we should just scroll immediately to the target position with a new layout pass
         */
        override fun onPivotNotFound(position: Int) {
            scrollToPosition(position)
        }

        override fun onSmoothScrollerStopped() {
            layoutInfo.setIsScrollingToTarget(false)
            pivotSelectionScroller = null
        }
    }


    /**
     * Takes care of dispatching [OnViewHolderSelectedListener.onViewHolderSelectedAndAligned]
     */
    private inner class IdleScrollListener : RecyclerView.OnScrollListener() {

        private var isScrolling = false
        private var previousSelectedPosition = RecyclerView.NO_POSITION

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            layoutInfo.setIsScrolling(newState != RecyclerView.SCROLL_STATE_IDLE)
            val wasScrolling = isScrolling
            isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            if (wasScrolling == isScrolling) return
            if (isScrolling) {
                // If we're now scrolling, save the current selection state
                previousSelectedPosition = pivotSelector.position
            } else if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                // If we're no longer scrolling, check if we need to send a new event
                pivotSelector.dispatchViewHolderSelectedAndAligned()
                previousSelectedPosition = RecyclerView.NO_POSITION
            }
            if (!isScrolling) {
                alignmentQueue.consumeAll()
            }
        }

    }

}
