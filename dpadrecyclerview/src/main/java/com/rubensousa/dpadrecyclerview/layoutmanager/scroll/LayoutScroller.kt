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
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotState
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class LayoutScroller(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration,
    private val pivotState: PivotState
) {

    companion object {
        const val TAG = "LayoutScroller"
    }

    var isSelectionUpdatePending = true
        private set

    var isSelectionInProgress = false
        private set

    private var isFocusUpdatePending = false
    private var recyclerView: RecyclerView? = null
    private val idleScrollListener = IdleScrollListener()

    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView?.removeOnScrollListener(idleScrollListener)
        recyclerView?.addOnScrollListener(idleScrollListener)
        this.recyclerView = recyclerView
    }

    fun onRestoreInstanceState() {
        if (pivotState.position != RecyclerView.NO_POSITION) {
            isSelectionUpdatePending = true
            isFocusUpdatePending = recyclerView?.hasFocus() ?: false
            layoutManager.requestLayout()
        }

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
            if (pivotState.update(position, subPosition)) {
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
            pivotState.dispatchViewHolderSelected(recyclerView)
            pivotState.dispatchViewHolderSelectedAndAligned(recyclerView)
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
        var targetPosition = pivotState.position
        var targetSubPosition = pivotState.subPosition
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
            layoutInfo.getAdapterPositionOf(viewHolderView)
        }
        if (newFocusPosition == RecyclerView.NO_POSITION) {
            return
        }
        val newSubFocusPosition = layoutAlignment.findSubPositionOfChild(
            recyclerView, viewHolderView, subPositionView
        )
        val focusChanged = pivotState.update(newFocusPosition, newSubFocusPosition)
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
        if (subPositionView != null && requestFocus) {
            subPositionView.requestFocus()
        } else if (requestFocus) {
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
            pivotState.dispatchViewHolderSelected(recyclerView)
            if (!scrolled) {
                pivotState.dispatchViewHolderSelectedAndAligned(recyclerView)
            }
        }
    }

    private fun scroll(
        recyclerView: RecyclerView,
        offset: Int,
        smooth: Boolean
    ) {
        if (layoutInfo.isLayoutInProgress) {
            Log.i(DpadRecyclerView.TAG, "Scrolling immediately since layout is in progress")
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
                previousSelectedPosition = pivotState.position
            } else if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                // If we're no longer scrolling, check if we need to send a new event
                pivotState.dispatchViewHolderSelectedAndAligned(recyclerView)
                previousSelectedPosition = RecyclerView.NO_POSITION
                if (BuildConfig.DEBUG) {
                    logChildren()
                }
            }
        }
    }

}
