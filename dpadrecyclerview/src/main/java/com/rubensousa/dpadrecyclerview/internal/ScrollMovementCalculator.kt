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

package com.rubensousa.dpadrecyclerview.internal

import android.view.View


internal object ScrollMovementCalculator {

    @JvmStatic
    fun calculate(isHorizontal: Boolean, isRTL: Boolean, direction: Int): ScrollMovement? {
        return if (isHorizontal) {
            when (direction) {
                View.FOCUS_LEFT -> {
                    if (isRTL) ScrollMovement.NEXT_ROW else ScrollMovement.PREVIOUS_ROW
                }
                View.FOCUS_RIGHT -> {
                    if (isRTL) ScrollMovement.PREVIOUS_ROW else ScrollMovement.NEXT_ROW
                }
                View.FOCUS_UP -> ScrollMovement.PREVIOUS_COLUMN
                View.FOCUS_DOWN -> ScrollMovement.NEXT_COLUMN
                else -> null
            }
        } else {
            when (direction) {
                View.FOCUS_LEFT -> {
                    if (isRTL) ScrollMovement.NEXT_COLUMN else ScrollMovement.PREVIOUS_COLUMN
                }
                View.FOCUS_RIGHT -> {
                    if (isRTL) ScrollMovement.PREVIOUS_COLUMN else ScrollMovement.NEXT_COLUMN
                }
                View.FOCUS_UP -> ScrollMovement.PREVIOUS_ROW
                View.FOCUS_DOWN -> ScrollMovement.NEXT_ROW
                else -> null
            }
        }
    }

}