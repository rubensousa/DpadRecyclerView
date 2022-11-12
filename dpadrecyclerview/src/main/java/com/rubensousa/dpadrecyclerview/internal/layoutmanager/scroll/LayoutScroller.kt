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

package com.rubensousa.dpadrecyclerview.internal.layoutmanager.scroll

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.PivotLayoutManager
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.ViewSelector
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.layout.LayoutArchitect

internal class LayoutScroller(
    private val layoutManager: PivotLayoutManager,
    private val configuration: LayoutConfiguration,
    private val layoutArchitect: LayoutArchitect,
    private val layoutInfo: LayoutInfo,
    private val viewSelector: ViewSelector
) {

    companion object {
        const val SCROLL_NONE = 0
        const val SCROLL_START = -1
        const val SCROLL_END = 1
    }

    var isSelectionInProgress = false
        private set

    fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (configuration.isVertical()) {
            return 0
        }
        return scrollBy(dx, recycler, state)
    }

    fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        if (configuration.isHorizontal()) {
            return 0
        }
        return scrollBy(dy, recycler, state)
    }

    private fun scrollBy(
        offset: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State
    ): Int {
        if (layoutManager.childCount == 0 || offset == 0) {
            return 0
        }
        return layoutArchitect.scroll(offset, recycler, state)
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
            viewSelector.update(
                position = RecyclerView.NO_POSITION,
                subPosition = 0
            )
            return
        }
        // Todo get sub position
        val previousPosition = viewSelector.position
        val nextPosition = layoutInfo.getAdapterPositionOfView(viewHolderView)
        if (nextPosition == previousPosition) {
            return
        }
        viewSelector.update(
            position = nextPosition,
            subPosition = 0
        )
         layoutManager.requestLayout()
    }

    // TODO
    private fun updateScrollLimits() {

    }

}
