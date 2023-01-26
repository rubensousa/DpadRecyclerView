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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.focus

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.FocusDirection
import org.junit.Test

class FocusDirectionTest {

    private val absoluteDirections = setOf(
        View.FOCUS_DOWN,
        View.FOCUS_LEFT,
        View.FOCUS_UP,
        View.FOCUS_RIGHT
    )
    private val orientations = setOf(RecyclerView.VERTICAL, RecyclerView.HORIZONTAL)
    private val rtl = setOf(false, true)

    @Test
    fun `absolute direction returns correct values for absolute directions`() {
        absoluteDirections.forEach { absoluteDirection ->
            orientations.forEach { orientation ->
                val isVertical = orientation == RecyclerView.VERTICAL
                rtl.forEach { isRTL ->
                    assertThat(
                        FocusDirection.getAbsoluteDirection(
                            direction = absoluteDirection,
                            isVertical = isVertical,
                            isRTL = isRTL
                        )
                    ).isEqualTo(absoluteDirection)
                }
            }
        }
    }

    @Test
    fun `absolute direction returns correct values for vertical orientation`() {
        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_FORWARD,
                isVertical = true,
                isRTL = false
            )
        ).isEqualTo(View.FOCUS_DOWN)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_BACKWARD,
                isVertical = true,
                isRTL = false
            )
        ).isEqualTo(View.FOCUS_UP)
    }

    @Test
    fun `absolute direction returns correct values for horizontal orientation`() {
        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_FORWARD,
                isVertical = false,
                isRTL = false
            )
        ).isEqualTo(View.FOCUS_RIGHT)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_FORWARD,
                isVertical = false,
                isRTL = true
            )
        ).isEqualTo(View.FOCUS_LEFT)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_BACKWARD,
                isVertical = false,
                isRTL = false
            )
        ).isEqualTo(View.FOCUS_LEFT)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_BACKWARD,
                isVertical = false,
                isRTL = true
            )
        ).isEqualTo(View.FOCUS_RIGHT)
    }

    @Test
    fun `FocusDirection is correctly parsed from vertical configuration`() {
        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_UP,
                isVertical = true,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_ITEM)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_DOWN,
                isVertical = true,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.NEXT_ITEM)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = true,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = true,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.NEXT_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = true,
                isRTL = true
            )
        ).isEqualTo(FocusDirection.NEXT_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = true,
                isRTL = true
            )
        ).isEqualTo(FocusDirection.PREVIOUS_COLUMN)
    }

    @Test
    fun `FocusDirection is correctly parsed from horizontal configuration`() {
        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_UP,
                isVertical = false,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_DOWN,
                isVertical = false,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.NEXT_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = false,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_ITEM)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = false,
                isRTL = false
            )
        ).isEqualTo(FocusDirection.NEXT_ITEM)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = false,
                isRTL = true
            )
        ).isEqualTo(FocusDirection.NEXT_ITEM)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = false,
                isRTL = true
            )
        ).isEqualTo(FocusDirection.PREVIOUS_ITEM)
    }

}
