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
    PREVIOUS_ITEM,
    NEXT_ITEM,
    PREVIOUS_COLUMN,
    NEXT_COLUMN;

    companion object {

        @JvmStatic
        fun getAbsoluteDirection(direction: Int, isVertical: Boolean, isRTL: Boolean): Int {
            if (direction != View.FOCUS_FORWARD && direction != View.FOCUS_BACKWARD) {
                return direction
            }
            return if (isVertical) {
                if (direction == View.FOCUS_FORWARD) View.FOCUS_DOWN else View.FOCUS_UP
            } else if ((direction == View.FOCUS_FORWARD) xor isRTL) {
                View.FOCUS_RIGHT
            } else {
                View.FOCUS_LEFT
            }
        }

        @JvmStatic
        fun from(
            direction: Int,
            isVertical: Boolean,
            isRTL: Boolean,
        ): FocusDirection? {
            val absoluteDirection = getAbsoluteDirection(direction, isVertical, isVertical)
            return if (isVertical) {
                when (absoluteDirection) {
                    View.FOCUS_UP -> PREVIOUS_ITEM
                    View.FOCUS_DOWN -> NEXT_ITEM
                    View.FOCUS_LEFT -> {
                        if (isRTL) NEXT_COLUMN else PREVIOUS_COLUMN
                    }
                    View.FOCUS_RIGHT -> {
                        if (isRTL) PREVIOUS_COLUMN else NEXT_COLUMN
                    }
                    else -> null
                }
            } else {
                when (absoluteDirection) {
                    View.FOCUS_LEFT -> {
                        if (isRTL) NEXT_ITEM else PREVIOUS_ITEM
                    }
                    View.FOCUS_RIGHT -> {
                        if (isRTL) PREVIOUS_ITEM else NEXT_ITEM
                    }
                    View.FOCUS_UP -> PREVIOUS_COLUMN
                    View.FOCUS_DOWN -> NEXT_COLUMN
                    else -> null
                }
            }
        }
    }

}
