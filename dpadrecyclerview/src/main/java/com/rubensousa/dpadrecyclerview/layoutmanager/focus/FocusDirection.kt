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

internal enum class FocusDirection {
    PREVIOUS_ROW,
    NEXT_ROW,
    PREVIOUS_COLUMN,
    NEXT_COLUMN;

    fun getScrollSign(reverseLayout: Boolean): Int {
        if (this == NEXT_COLUMN || this == PREVIOUS_COLUMN) {
            return 0
        }
        return if (this == NEXT_ROW != reverseLayout) {
            1
        } else {
            -1
        }
    }

    companion object {

        @JvmStatic
        fun getAbsoluteDirection(direction: Int, isVertical: Boolean, reverseLayout: Boolean): Int {
            if (direction != View.FOCUS_FORWARD && direction != View.FOCUS_BACKWARD) {
                return direction
            }
            return if (isVertical) {
                if (direction == View.FOCUS_FORWARD) View.FOCUS_DOWN else View.FOCUS_UP
            } else if ((direction == View.FOCUS_FORWARD) xor reverseLayout) {
                View.FOCUS_RIGHT
            } else {
                View.FOCUS_LEFT
            }
        }

        @JvmStatic
        fun from(
            direction: Int,
            isVertical: Boolean,
            reverseLayout: Boolean,
        ): FocusDirection? {
            val absoluteDirection = getAbsoluteDirection(direction, isVertical, isVertical)
            return if (isVertical) {
                when (absoluteDirection) {
                    View.FOCUS_UP -> if (reverseLayout) NEXT_ROW else PREVIOUS_ROW
                    View.FOCUS_DOWN -> if (reverseLayout) PREVIOUS_ROW else NEXT_ROW
                    View.FOCUS_LEFT -> {
                        if (reverseLayout) NEXT_COLUMN else PREVIOUS_COLUMN
                    }
                    View.FOCUS_RIGHT -> {
                        if (reverseLayout) PREVIOUS_COLUMN else NEXT_COLUMN
                    }
                    else -> null
                }
            } else {
                when (absoluteDirection) {
                    View.FOCUS_LEFT -> {
                        if (reverseLayout) NEXT_ROW else PREVIOUS_ROW
                    }
                    View.FOCUS_RIGHT -> {
                        if (reverseLayout) PREVIOUS_ROW else NEXT_ROW
                    }
                    View.FOCUS_UP -> if (reverseLayout) NEXT_COLUMN else PREVIOUS_COLUMN
                    View.FOCUS_DOWN -> if (reverseLayout) PREVIOUS_COLUMN else NEXT_COLUMN
                    else -> null
                }
            }
        }
    }

}
