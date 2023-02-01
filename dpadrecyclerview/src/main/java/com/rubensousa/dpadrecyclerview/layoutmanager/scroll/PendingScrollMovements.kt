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

import androidx.annotation.VisibleForTesting
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
