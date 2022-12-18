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
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class ScrollMovements(
    private val layoutInfo: LayoutInfo
) {

    private var pendingMoves = 0
    private val maxPendingMoves = 1

    fun getPendingMoves() = pendingMoves

    fun hasPendingMoves() = pendingMoves != 0

    fun shouldStopScrolling(): Boolean {
        return !hasPendingMoves() || isLayoutCompleteInScrollingDirection()
    }

    private fun isLayoutCompleteInScrollingDirection(): Boolean {
        return layoutInfo.hasCreatedFirstItem() && pendingMoves < 0
                || layoutInfo.hasCreatedLastItem() && pendingMoves > 0
    }

    fun clear() {
        pendingMoves = 0
    }

    fun increase() {
        if (pendingMoves < maxPendingMoves) {
            pendingMoves++
        }
        Log.i(LayoutScroller.TAG, "Increased pending moves to: $pendingMoves")
    }

    fun decrease() {
        if (pendingMoves > -maxPendingMoves) {
            pendingMoves--
        }
        Log.i(LayoutScroller.TAG, "Decreased pending moves to: $pendingMoves")
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
