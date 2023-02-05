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

import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import kotlin.math.max

internal class PendingScrollMovements(
    private var maxPendingMoves: Int,
    private val layoutInfo: LayoutInfo
) {

    var pendingMoves = 0
        private set

    @VisibleForTesting
    fun setMaxPendingMoves(max: Int) {
        maxPendingMoves = max(1, max)
    }

    fun hasPendingMoves() = pendingMoves != 0

    fun shouldStopScrolling(): Boolean {
        return !hasPendingMoves() || isLayoutCompleteInScrollingDirection()
    }

    fun shouldScrollToView(viewPosition: Int, pivotPosition: Int): Boolean {
        if (viewPosition == pivotPosition) {
            return true
        }
        return if (!layoutInfo.shouldReverseLayout()) {
            (pendingMoves < 0 && viewPosition < pivotPosition
                    || pendingMoves > 0 && viewPosition > pivotPosition)
        } else {
            (pendingMoves < 0 && viewPosition > pivotPosition
                    || pendingMoves > 0 && viewPosition < pivotPosition)
        }
    }

    private fun isLayoutCompleteInScrollingDirection(): Boolean {
        return if (!layoutInfo.shouldReverseLayout()) {
            (layoutInfo.hasCreatedFirstItem() && pendingMoves < 0
                    || layoutInfo.hasCreatedLastItem() && pendingMoves > 0)
        } else {
            (layoutInfo.hasCreatedLastItem() && pendingMoves < 0
                    || layoutInfo.hasCreatedFirstItem() && pendingMoves > 0)
        }

    }

    fun clear() {
        pendingMoves = 0
    }

    fun add(forward: Boolean) {
        if (layoutInfo.shouldReverseLayout()) {
            if (forward) {
                decrease()
            } else {
                increase()
            }
        } else {
            if (forward) {
                increase()
            } else {
                decrease()
            }
        }
    }

    private fun increase() {
        if (pendingMoves < maxPendingMoves) {
            pendingMoves++
        }
    }

    private fun decrease() {
        if (pendingMoves > -maxPendingMoves) {
            pendingMoves--
        }
    }

    fun consumeGridMovements(pivotPosition: Int): View? {
        if (!hasPendingMoves()) {
            return null
        }

        var focusedSpanIndex = if (pivotPosition != RecyclerView.NO_POSITION) {
            layoutInfo.getStartColumnIndex(pivotPosition)
        } else {
            RecyclerView.NO_POSITION
        }
        var focusedSpanGroup = if (pivotPosition != RecyclerView.NO_POSITION) {
            layoutInfo.getSpanGroupIndex(pivotPosition)
        } else {
            0
        }

        var currentIndex = getIndexOfPivotView(pivotPosition)
        val childCount = layoutInfo.getChildCount()

        val increment = if (pendingMoves > 0) {
            1
        } else {
            -1
        }
        currentIndex += increment

        var targetView: View? = null

        /**
         * Search in the scrolling direction
         */
        while (currentIndex in 1 until childCount && hasPendingMoves()) {
            val child = layoutInfo.getChildAt(currentIndex)
            currentIndex += increment
            if (child == null || !layoutInfo.isViewFocusable(child)) {
                continue
            }
            val childPosition = layoutInfo.getAdapterPositionOf(child)
            val spanSize = layoutInfo.getSpanSize(childPosition)
            val spanIndex = layoutInfo.getStartColumnIndex(childPosition)
            val spanGroup = layoutInfo.getSpanGroupIndex(childPosition)
            if (shouldFocusChildAt(
                    spanIndex, spanSize, spanGroup, focusedSpanIndex, focusedSpanGroup
                )
            ) {
                focusedSpanGroup = spanGroup
                focusedSpanIndex = spanIndex
                targetView = child
                consume()
            }
        }
        return targetView
    }

    private fun shouldFocusChildAt(
        spanIndex: Int,
        spanSize: Int,
        spanGroup: Int,
        focusedSpanIndex: Int,
        focusedSpanGroup: Int
    ): Boolean {
        // If there's no current span focused, we need to focus the new one
        // if it
        if (focusedSpanIndex == RecyclerView.NO_POSITION) {
            return true
        }
        // Don't allow changing focus within the same span group
        if (spanGroup == focusedSpanGroup) {
            return false
        }
        // If we're scrolling forwards, accept spans after the current one
        val scrollingForwards = if (!layoutInfo.shouldReverseLayout()) {
            pendingMoves > 0
        } else {
            pendingMoves < 0
        }
        if (scrollingForwards && spanIndex + spanSize - 1 >= focusedSpanIndex) {
            return true
        }
        // If we're scrolling backwards, accept spans before the current one
        return !scrollingForwards && spanIndex - (spanSize - 1) <= focusedSpanIndex
    }

    private fun getIndexOfPivotView(pivotPosition: Int): Int {
        val pivotView = layoutInfo.findViewByAdapterPosition(pivotPosition)
            ?: layoutInfo.findViewByPosition(pivotPosition)
        return layoutInfo.findIndexOf(pivotView)
    }

    fun consume(): Boolean {
        if (pendingMoves == 0) {
            return false
        }
        if (pendingMoves > 0) {
            pendingMoves--
        } else {
            pendingMoves++
        }
        return true
    }

}
