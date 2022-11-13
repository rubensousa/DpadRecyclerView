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
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutState
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class LayoutScroller(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration,
    private val pivotLayoutState: PivotLayoutState
) {

    companion object {
        const val TAG = "LayoutScroller"
    }

    var isSelectionUpdatePending = false
        private set

    var isSelectionInProgress = false
        private set

    private var isFocusUpdatePending = false
    private var recyclerView: RecyclerView? = null
    private var scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            layoutInfo.setIsScrolling(newState != RecyclerView.SCROLL_STATE_IDLE)
        }
    }

    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView?.removeOnScrollListener(scrollListener)
        recyclerView?.addOnScrollListener(scrollListener)
        this.recyclerView = recyclerView
    }

    /**
     * Scrolls to a position-subPosition pair.
     *
     * If don't need to smooth scroll, we can scroll immediately by triggering a layout pass
     * with the new pivot position
     */
    fun scrollToPosition(
        position: Int,
        subPosition: Int,
        smooth: Boolean
    ) {
        Log.d(
            TAG, "Scrolling requested to position $position " +
                    "and subPosition $subPosition with smooth: $smooth"
        )
        if (!smooth) {
            if (pivotLayoutState.update(position, subPosition)) {
                isSelectionUpdatePending = true
                isFocusUpdatePending = true
                layoutManager.requestLayout()
            }
            return
        }
    }

    /**
     * Since the layout pass can finish after the RecyclerView gains focus,
     * make sure that we focus the new selected view when layout is done
     */
    fun onLayoutCompleted(recyclerView: RecyclerView) {
        scrollToSelectedPosition(
            recyclerView,
            smooth = false,
            requestFocus = recyclerView.hasFocus() || isFocusUpdatePending
        )
        if (isSelectionUpdatePending) {
            isSelectionUpdatePending = false
            pivotLayoutState.dispatchViewHolderSelected(recyclerView)
            pivotLayoutState.dispatchViewHolderSelectedAndAligned(recyclerView)
        }
        isFocusUpdatePending = false
    }

    /**
     * Scrolls to the current focused position
     */
    fun scrollToSelectedPosition(
        recyclerView: RecyclerView,
        smooth: Boolean,
        requestFocus: Boolean
    ) {
        val itemCount = layoutManager.itemCount
        var targetPosition = pivotLayoutState.position
        var targetSubPosition = pivotLayoutState.subPosition
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
            scrollToPosition(targetPosition, targetSubPosition, smooth)
        } else {
            scrollToView(
                recyclerView, layoutManager.findViewByPosition(targetPosition), smooth, requestFocus
            )
        }
    }

    // TODO
    fun scrollToPosition(position: Int) {

    }

    // TODO
    fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {

    }

    // TODO
    fun startSmoothScroll(smoothScroller: RecyclerView.SmoothScroller?) {

    }

    // TODO
    fun dispatchPendingMovement(forward: Boolean) {

    }

    /**
     * Move the focus position multiple steps in the same row.
     * Stops when moves are all consumed or reach first/last visible item.
     * @return pending moves
     */
    // TODO
    fun dispatchSelectionMoves(preventScroll: Boolean, moves: Int): Int {
        return moves
    }


    private fun scrollToView(
        recyclerView: RecyclerView,
        view: View?,
        smooth: Boolean,
        requestFocus: Boolean
    ) {
        scrollToView(recyclerView, view, subPositionView = view?.findFocus(), smooth, requestFocus)
    }

    // TODO
    fun scrollToView(
        recyclerView: RecyclerView,
        viewHolderView: View?,
        subPositionView: View?,
        smooth: Boolean,
        requestFocus: Boolean
    ) {
        val newFocusPosition = if (viewHolderView == null) {
            RecyclerView.NO_POSITION
        } else {
            layoutInfo.getAdapterPositionOfView(viewHolderView)
        }
        if (newFocusPosition == RecyclerView.NO_POSITION) {
            return
        }
        val newSubFocusPosition = layoutAlignment.findSubPositionOfChild(
            recyclerView, viewHolderView, subPositionView
        )
        val focusChanged = pivotLayoutState.update(newFocusPosition, newSubFocusPosition)
        var selectViewHolder = false
        if (focusChanged) {
            if (!layoutInfo.isLayoutInProgress) {
                selectViewHolder = true
            } else {
                isSelectionUpdatePending = true
            }
            if (configuration.isChildDrawingOrderEnabled) {
                recyclerView.invalidate()
            }
        }
        if (viewHolderView == null) {
            return
        }
        if (subPositionView != null && !subPositionView.hasFocus() && requestFocus) {
            subPositionView.requestFocus()
        } else if (!viewHolderView.hasFocus() && requestFocus) {
            viewHolderView.requestFocus()
        }

        if (!focusChanged) {
            return
        }

        val scrolled = layoutAlignment.updateScroll(recyclerView, viewHolderView, subPositionView)
            ?.let { scrollOffset ->
                scroll(recyclerView, scrollOffset, smooth)
            } != null

        if (selectViewHolder) {
            pivotLayoutState.dispatchViewHolderSelected(recyclerView)
            if (!scrolled) {
                pivotLayoutState.dispatchViewHolderSelectedAndAligned(recyclerView)
            }
        }
    }

    private fun scroll(
        recyclerView: RecyclerView,
        offset: Int,
        smooth: Boolean
    ) {
        if (layoutInfo.isLayoutInProgress) {
            Log.d(TAG, "Scrolling immediately since layout is in progress")
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
            Log.d(TAG, "Smooth scrolling: $scrollX dx / $scrollY dy")
            recyclerView.smoothScrollBy(scrollX, scrollY)
        } else {
            Log.d(TAG, "Scrolling: $scrollX dx / $scrollY dy")
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

    // TODO
    private fun updateScrollLimits() {

    }

}
