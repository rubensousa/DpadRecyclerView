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

package com.rubensousa.dpadrecyclerview.layoutmanager.focus

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

/**
 * Implementation for [FocusableDirection.CONTINUOUS]
 * TODO: Add tests
 */
internal class ContinuousFocusInterceptor(
    private val layoutInfo: LayoutInfo,
) : FocusInterceptor {

    override fun findFocus(
        recyclerView: RecyclerView,
        focusedView: View,
        position: Int,
        direction: Int
    ): View? {
        val focusDirection = FocusDirection.from(
            isVertical = layoutInfo.isVertical(),
            reverseLayout = layoutInfo.shouldReverseLayout(),
            direction = direction
        ) ?: return null
        return findFocus(position, focusDirection)
    }

    private fun findFocus(position: Int, direction: FocusDirection): View? {
        if (direction != FocusDirection.PREVIOUS_COLUMN
            && direction != FocusDirection.NEXT_COLUMN
        ) {
            return null
        }
        val startRow = layoutInfo.getRowIndex(position)
        var nextPosition = if (direction == FocusDirection.NEXT_COLUMN) {
            position + 1
        } else {
            position - 1
        }
        var nextRow = layoutInfo.getRowIndex(nextPosition)

        // If we still have focusable views in the movement direction, bail out
        while (nextRow == startRow && nextPosition >= 0) {
            val nextView = layoutInfo.findViewByPosition(nextPosition)
            if (nextView != null && layoutInfo.isViewFocusable(nextView)) {
                return null
            }
            if (direction == FocusDirection.NEXT_COLUMN) {
                nextPosition++
            } else {
                nextPosition--
            }
            nextRow = layoutInfo.getRowIndex(nextPosition)
        }

        if (nextRow == 0 && startRow == 0) {
            return null
        }

        var targetView = layoutInfo.findViewByPosition(nextPosition)

        // Now check if we still need to go deeper to find a focusable view
        while (targetView != null && !layoutInfo.isViewFocusable(targetView)) {
            if (direction == FocusDirection.NEXT_COLUMN) {
                nextPosition++
            } else {
                nextPosition--
            }
            targetView = layoutInfo.findViewByPosition(nextPosition)
        }

        if (targetView != null && !layoutInfo.isViewFocusable(targetView)) {
            return null
        }
        return targetView
    }
}

