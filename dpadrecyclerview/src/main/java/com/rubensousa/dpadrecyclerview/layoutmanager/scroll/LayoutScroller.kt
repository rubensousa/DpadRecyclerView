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
import com.rubensousa.dpadrecyclerview.BuildConfig
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class LayoutScroller(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration,
    private val pivotSelector: PivotSelector
) {

    companion object {
        const val TAG = "LayoutScroller"
    }

    var isSelectionInProgress = false
        private set

    private var recyclerView: RecyclerView? = null
    private var searchPivotSmoothScroller: SearchPivotSmoothScroller? = null
    private val idleScrollListener = IdleScrollListener()
    private val searchPivotListener = SearchPivotListener()

    fun setRecyclerView(newRecyclerView: RecyclerView?) {
        recyclerView?.removeOnScrollListener(idleScrollListener)
        newRecyclerView?.addOnScrollListener(idleScrollListener)
        recyclerView = newRecyclerView
    }

    /**
     * Since the layout pass can finish after the RecyclerView gains focus,
     * make sure that we focus the new selected view when layout is done
     */
    fun onLayoutCompleted(recyclerView: RecyclerView, requestFocus: Boolean) {
        scrollToSelectedPosition(
            recyclerView,
            smooth = false,
            requestFocus = requestFocus
        )
    }

    /**
     * We can only scroll to views if they exist when this is called
     * and there's no pending layout request.
     *
     * If the view doesn't exist and there's no pending layout request,
     * we use [SearchPivotSmoothScroller] to scroll until it's found.
     *
     * Otherwise, the selection is deferred to the next layout pass.
     */
    fun scrollToPosition(
        recyclerView: RecyclerView,
        position: Int,
        subPosition: Int,
        smooth: Boolean
    ) {
        val view = layoutManager.findViewByPosition(position)

        // Check if we can immediately scroll to the existing view
        if (!layoutManager.isSmoothScrolling
            && view != null
            && isScrollToViewPossible(recyclerView, position, view)
        ) {
            startScroll(recyclerView, position, subPosition, view, smooth)
            return
        }

        // Otherwise update the selection and start searching for the new pivot
        if (smooth && !recyclerView.isLayoutRequested) {
            pivotSelector.update(position, subPosition)
            startSmoothScrollingToPivot(recyclerView, position, subPosition)
            return
        }

        if (layoutManager.isSmoothScrolling) {
            stopSmoothScrolling(recyclerView)
        }

        if (view != null && isScrollToViewPossible(recyclerView, position, view)) {
            startScroll(recyclerView, position, subPosition, view, smooth)
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
        recyclerView: RecyclerView,
        position: Int,
        view: View
    ): Boolean {
        return !recyclerView.isLayoutRequested && layoutInfo.getAdapterPositionOf(view) == position
    }

    private fun startSmoothScrollingToPivot(
        recyclerView: RecyclerView,
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
        layoutManager.startSmoothScroll(
            SearchPivotSmoothScroller(
                recyclerView,
                position,
                subPosition,
                layoutInfo,
                layoutAlignment,
                searchPivotListener
            )
        )
    }

    private fun stopSmoothScrolling(recyclerView: RecyclerView) {
        cancelSmoothScroller()
        recyclerView.stopScroll()
    }

    private fun startScroll(
        recyclerView: RecyclerView,
        position: Int,
        subPosition: Int,
        view: View,
        smooth: Boolean
    ) {
        isSelectionInProgress = true
        val subPositionView = layoutAlignment.getViewAtSubPosition(recyclerView, view, subPosition)
        if (subPositionView == null && subPosition != 0) {
            Log.w(
                DpadRecyclerView.TAG,
                "Subposition $position doesn't exist for position $position," +
                        "scroll instead started for subposition 0"
            )
        }
        scrollToView(
            recyclerView,
            view,
            subPositionView,
            smooth,
            requestFocus = layoutManager.hasFocus()
        )
        isSelectionInProgress = false
    }

    fun scrollToSelectedPosition(
        recyclerView: RecyclerView,
        smooth: Boolean,
        requestFocus: Boolean
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
        if (targetSubPosition != 0) {
            scrollToPosition(recyclerView, targetPosition, targetSubPosition, smooth)
        } else {
            scrollToView(
                recyclerView, layoutManager.findViewByPosition(targetPosition), smooth, requestFocus
            )
        }
    }

    fun scrollToPosition(position: Int, subPosition: Int = 0) {
        if (pivotSelector.update(position, subPosition)) {
            pivotSelector.setSelectionUpdatePending()
            layoutManager.requestLayout()
        }
    }

    fun setSmoothScroller(smoothScroller: RecyclerView.SmoothScroller) {
        searchPivotSmoothScroller = if (smoothScroller.isRunning
            && smoothScroller is SearchPivotSmoothScroller
        ) {
            smoothScroller
        } else {
            null
        }
    }

    fun cancelSmoothScroller() {
        searchPivotSmoothScroller?.cancel()
        searchPivotSmoothScroller = null
    }

    private fun scrollToView(
        recyclerView: RecyclerView,
        view: View?,
        smooth: Boolean,
        requestFocus: Boolean
    ) {
        scrollToView(recyclerView, view, subPositionView = view?.findFocus(), smooth, requestFocus)
    }

    fun scrollToView(
        recyclerView: RecyclerView,
        view: View?,
        subPositionView: View?,
        smooth: Boolean,
        requestFocus: Boolean
    ) {
        val newPosition = if (view == null) {
            RecyclerView.NO_POSITION
        } else {
            layoutInfo.getAdapterPositionOf(view)
        }
        if (newPosition == RecyclerView.NO_POSITION) {
            return
        }
        val newSubPosition = layoutAlignment.getSubPositionOfView(
            recyclerView, view, subPositionView
        )
        val selectionChanged = pivotSelector.update(newPosition, newSubPosition)
        var selectViewHolder = false
        if (selectionChanged) {
            if (!layoutInfo.isLayoutInProgress) {
                selectViewHolder = true
            } else {
                pivotSelector.setSelectionUpdatePending()
            }
            if (configuration.isChildDrawingOrderEnabled) {
                recyclerView.invalidate()
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

        if (!selectionChanged) {
            return
        }

        performScrollToView(
            recyclerView, view, subPositionView, selectViewHolder, smooth
        )
    }

    private fun performScrollToView(
        recyclerView: RecyclerView,
        view: View,
        subPositionView: View?,
        selectViewHolder: Boolean,
        smooth: Boolean
    ) {
        val scrollOffset = layoutAlignment.calculateScrollOffset(
            recyclerView, view, subPositionView
        )

        scrollBy(recyclerView, scrollOffset, smooth)

        if (selectViewHolder) {
            pivotSelector.dispatchViewHolderSelected(recyclerView)
            // If we didn't scroll, dispatch aligned event already
            if (scrollOffset == 0) {
                pivotSelector.dispatchViewHolderSelectedAndAligned(recyclerView)
            }
        }
    }

    private fun scrollBy(
        recyclerView: RecyclerView,
        offset: Int,
        smooth: Boolean
    ) {
        if (offset == 0) {
            return
        }
        if (layoutInfo.isLayoutInProgress) {
            Log.i(DpadRecyclerView.TAG, "Scrolling immediately since layout is in progress")
            // TODO check if this is working correctly
            scroll(recyclerView, offset)
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
            recyclerView.smoothScrollBy(scrollX, scrollY)
        } else {
            recyclerView.scrollBy(scrollX, scrollY)
        }
    }

    private fun scroll(recyclerView: RecyclerView, dx: Int) {
        val offset = calculateScrollAmount(dx)
        if (offset == 0) {
            return
        }
        layoutInfo.orientationHelper.offsetChildren(-offset)
        recyclerView.invalidate()
    }

    private fun calculateScrollAmount(offset: Int): Int {
        var scrollOffset = offset
        if (!layoutInfo.isLayoutInProgress) {
            scrollOffset = layoutAlignment.getCappedScroll(scrollOffset)
        }
        return scrollOffset
    }

    /**
     * Logs the internal representation of children for debugging purposes.
     */
    internal fun logChildren() {
        Log.d(TAG, "Children laid out:")
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)!!
            val position = layoutManager.getPosition(child)
            val childStart = layoutInfo.getDecoratedStart(child)
            val childEnd = layoutInfo.getDecoratedEnd(child)
            Log.d(TAG, "View $position, start: $childStart, end: $childEnd")
        }
    }

    private inner class SearchPivotListener : SearchPivotSmoothScroller.Listener {

        override fun onPivotFound(pivotView: View, position: Int, subPosition: Int) {
            if (layoutManager.hasFocus()) {
                isSelectionInProgress = true
                recyclerView?.let {
                    val subPositionView = layoutAlignment.getViewAtSubPosition(
                        it, pivotView, subPosition
                    )
                    subPositionView?.requestFocus() ?: pivotView.requestFocus()
                }
                isSelectionInProgress = false
            }
            recyclerView?.apply {
                pivotSelector.dispatchViewHolderSelected(this)
                pivotSelector.dispatchViewHolderSelectedAndAligned(this)
            }
        }

        /**
         * If the smooth scroller didn't find the target view for whatever reason,
         * we should just scroll immediately to the target position with a new layout pass
         */
        override fun onPivotNotFound(position: Int) {
            scrollToPosition(position)
        }

        override fun onSmoothScrollerStopped() {
            searchPivotSmoothScroller = null
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
                pivotSelector.dispatchViewHolderSelectedAndAligned(recyclerView)
                previousSelectedPosition = RecyclerView.NO_POSITION
                if (BuildConfig.DEBUG) {
                    logChildren()
                }
            }
        }
    }

}
