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
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.SpanFocusFinder
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
        if (forward != layoutInfo.shouldReverseLayout()) {
            increase()
        } else {
            decrease()
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

    fun consumeGridMovements(pivotPosition: Int, spanFocusFinder: SpanFocusFinder): View? {
        if (!hasPendingMoves()) {
            return null
        }
        val reverseLayout = layoutInfo.shouldReverseLayout()
        val isScrollingForwards =  if (!reverseLayout) pendingMoves > 0 else pendingMoves < 0
        val edgeView = if (isScrollingForwards != reverseLayout) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
        if (edgeView == null) {
            return null
        }
        val spanSizeLookup = layoutInfo.getConfiguration().spanSizeLookup
        val edgePosition = layoutInfo.getLayoutPositionOf(edgeView)
        var targetView: View? = null
        var targetPosition: Int = pivotPosition

        while (targetPosition != edgePosition && hasPendingMoves()) {
            targetPosition = spanFocusFinder.findNextSpanPosition(
                focusedPosition = targetPosition,
                spanSizeLookup = spanSizeLookup,
                forward = isScrollingForwards,
                edgePosition = edgePosition,
                reverseLayout = reverseLayout
            )
            if (targetPosition == RecyclerView.NO_POSITION) {
                break
            }
            spanFocusFinder.save(targetPosition, spanSizeLookup)
            val view = layoutInfo.findViewByPosition(targetPosition)
            if (view == null || !layoutInfo.isViewFocusable(view)) {
                continue
            }
            targetView = view
            consume()
        }
        // Reset the focus back to the original position
        if (targetView == null) {
            spanFocusFinder.save(pivotPosition, spanSizeLookup)
        }
        return targetView
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
