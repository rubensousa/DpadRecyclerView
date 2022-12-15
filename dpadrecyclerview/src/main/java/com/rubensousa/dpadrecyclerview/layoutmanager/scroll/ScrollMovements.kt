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
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class ScrollMovements(
    private val layoutInfo: LayoutInfo,
    private val pivotSelector: PivotSelector,
    private val listener: Listener
) {

    private var pendingMoves = 0
    private val maxPendingMoves = 10

    fun getPendingMoves() = pendingMoves

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

    fun consumeBeforeLayout() {
        if (pendingMoves == 0) {
            return
        }
        var pivotView: View? = null
        var currentPosition = pivotSelector.position
        while (pendingMoves != 0) {
            val newView = layoutInfo.findViewByPosition(currentPosition) ?: break
            if (!layoutInfo.shouldFocusView(newView)) {
                continue
            }
            pivotSelector.update(currentPosition, subPosition = 0)
            if (pendingMoves > 0) {
                pendingMoves--
                currentPosition++
            } else {
                pendingMoves++
                currentPosition--
            }
            pivotView = newView
            Log.i(
                LayoutScroller.TAG, "Changed new selected position: $currentPosition, " +
                        "pending moves: $pendingMoves"
            )
        }

        if (pivotView != null) {
            listener.onNewPivotFound(pivotView)
        }
    }

    fun consumeAfterLayout() {
        val lastItemWithinReach = pendingMoves > 0 && layoutInfo.hasCreatedLastItem()
        val firstItemWithinReach = pendingMoves < 0 && layoutInfo.hasCreatedFirstItem()
        if (pendingMoves == 0 || lastItemWithinReach || firstItemWithinReach) {
            pendingMoves = 0
            val view = layoutInfo.findViewByPosition(pivotSelector.position)
            if (view != null) {
                listener.onNewPivotFound(view)
            }
        }
    }

    interface Listener {
        fun onNewPivotFound(pivotView: View)
    }

}