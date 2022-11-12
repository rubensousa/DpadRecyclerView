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
import com.rubensousa.dpadrecyclerview.layoutmanager.ViewHolderSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class LayoutScroller(
    private val layoutInfo: LayoutInfo,
    private val layoutAlignment: LayoutAlignment,
    private val layoutArchitect: LayoutArchitect,
    private val viewHolderSelector: ViewHolderSelector
) {

    companion object {
        const val TAG = "LayoutScroller"
    }

    var isSelectionUpdatePending = false
        private set
    
    var isSelectionInProgress = false
        private set

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

    // TODO
    fun scrollToView(
        recyclerView: RecyclerView,
        viewHolderView: View?,
        subPositionView: View?,
        smooth: Boolean
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
        val focusChanged = viewHolderSelector.update(newFocusPosition, newSubFocusPosition)
        var selectViewHolder = false
        if (focusChanged) {
            if (!layoutInfo.isLayoutInProgress) {
                selectViewHolder = true
            } else {
                isSelectionUpdatePending = true
            }
            if (layoutInfo.isChildDrawingOrderEnabled) {
                recyclerView.invalidate()
            }
        }
        if (viewHolderView == null) {
            return
        }
        if (subPositionView != null && !subPositionView.hasFocus() && recyclerView.hasFocus()) {
            subPositionView.requestFocus()
        } else if (!viewHolderView.hasFocus() && recyclerView.hasFocus()) {
            viewHolderView.requestFocus()
        }

        val scrolled = layoutAlignment.updateScroll(recyclerView, viewHolderView, subPositionView)
            ?.let { scrollOffset ->
                scroll(recyclerView, scrollOffset, smooth)
            } != null

        if (selectViewHolder) {
            viewHolderSelector.dispatchViewHolderSelected()
            if (!scrolled) {
                viewHolderSelector.dispatchViewHolderSelectedAndAligned()
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
