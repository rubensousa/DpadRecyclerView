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

package com.rubensousa.dpadrecyclerview.internal.layout

import android.view.View
import androidx.recyclerview.widget.RecyclerView

class TvLayoutScroller(
    private val layoutManager: TvLayoutManager,
    private val layoutArchitect: LayoutArchitect,
    private val layoutInfo: TvLayoutInfo,
    private val selectionState: TvSelectionState
) {

    companion object {
        const val SCROLL_NONE = 0
        const val SCROLL_START = -1
        const val SCROLL_END = 1
    }

    var isSelectionInProgress = false
        private set

    // TODO
    fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return 0
    }

    // TODO
    fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return 0
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

    // TODO
    fun scrollToView(
        recyclerView: RecyclerView,
        viewHolderView: View?,
        subPositionView: View?,
        smooth: Boolean
    ) {
        if (viewHolderView == null) {
            selectionState.update(
                position = RecyclerView.NO_POSITION,
                subPosition = 0
            )
            return
        }
        selectionState.update(
            position = layoutInfo.getAdapterPositionOfView(viewHolderView),
            subPosition = 0
        )
        layoutManager.requestLayout()
    }


}