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
    private val reverseLayoutFlags = setOf(false, true)

    @Test
    fun `absolute direction returns correct values for absolute directions`() {
        absoluteDirections.forEach { absoluteDirection ->
            orientations.forEach { orientation ->
                val isVertical = orientation == RecyclerView.VERTICAL
                reverseLayoutFlags.forEach { reverseLayout ->
                    assertThat(
                        FocusDirection.getAbsoluteDirection(
                            direction = absoluteDirection,
                            isVertical = isVertical,
                            reverseLayout = reverseLayout
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
                reverseLayout = false
            )
        ).isEqualTo(View.FOCUS_DOWN)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_BACKWARD,
                isVertical = true,
                reverseLayout = false
            )
        ).isEqualTo(View.FOCUS_UP)
    }

    @Test
    fun `absolute direction returns correct values for horizontal orientation`() {
        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_FORWARD,
                isVertical = false,
                reverseLayout = false
            )
        ).isEqualTo(View.FOCUS_RIGHT)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_FORWARD,
                isVertical = false,
                reverseLayout = true
            )
        ).isEqualTo(View.FOCUS_LEFT)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_BACKWARD,
                isVertical = false,
                reverseLayout = false
            )
        ).isEqualTo(View.FOCUS_LEFT)

        assertThat(
            FocusDirection.getAbsoluteDirection(
                direction = View.FOCUS_BACKWARD,
                isVertical = false,
                reverseLayout = true
            )
        ).isEqualTo(View.FOCUS_RIGHT)
    }

    @Test
    fun `FocusDirection is correctly parsed from vertical configuration`() {
        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_UP,
                isVertical = true,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_ROW)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_UP,
                isVertical = true,
                reverseLayout = true
            )
        ).isEqualTo(FocusDirection.NEXT_ROW)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_DOWN,
                isVertical = true,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.NEXT_ROW)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_DOWN,
                isVertical = true,
                reverseLayout = true
            )
        ).isEqualTo(FocusDirection.PREVIOUS_ROW)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = true,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = true,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.NEXT_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = true,
                reverseLayout = true
            )
        ).isEqualTo(FocusDirection.NEXT_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = true,
                reverseLayout = true
            )
        ).isEqualTo(FocusDirection.PREVIOUS_COLUMN)
    }

    @Test
    fun `FocusDirection is correctly parsed from horizontal configuration`() {
        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_UP,
                isVertical = false,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_DOWN,
                isVertical = false,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.NEXT_COLUMN)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = false,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.PREVIOUS_ROW)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = false,
                reverseLayout = false
            )
        ).isEqualTo(FocusDirection.NEXT_ROW)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_LEFT,
                isVertical = false,
                reverseLayout = true
            )
        ).isEqualTo(FocusDirection.NEXT_ROW)

        assertThat(
            FocusDirection.from(
                direction = View.FOCUS_RIGHT,
                isVertical = false,
                reverseLayout = true
            )
        ).isEqualTo(FocusDirection.PREVIOUS_ROW)
    }

    @Test
    fun `scroll sign from focus direction is correctly parsed`() {
        assertThat(
            FocusDirection.NEXT_COLUMN.getScrollSign(
                reverseLayout = false
            )
        ).isEqualTo(0)
        assertThat(
            FocusDirection.PREVIOUS_COLUMN.getScrollSign(
                reverseLayout = false
            )
        ).isEqualTo(0)
        assertThat(
            FocusDirection.NEXT_ROW.getScrollSign(
                reverseLayout = false
            )
        ).isEqualTo(1)
        assertThat(
            FocusDirection.NEXT_ROW.getScrollSign(
                reverseLayout = true
            )
        ).isEqualTo(-1)
        assertThat(
            FocusDirection.PREVIOUS_ROW.getScrollSign(
                reverseLayout = false
            )
        ).isEqualTo(-1)
        assertThat(
            FocusDirection.PREVIOUS_ROW.getScrollSign(
                reverseLayout = true
            )
        ).isEqualTo(1)
    }

    @Test
    fun testPrimaryAndSecondaryDirections() {
        assertThat(FocusDirection.NEXT_ROW.isPrimary()).isTrue()
        assertThat(FocusDirection.PREVIOUS_ROW.isPrimary()).isTrue()
        assertThat(FocusDirection.NEXT_COLUMN.isPrimary()).isFalse()
        assertThat(FocusDirection.PREVIOUS_COLUMN.isPrimary()).isFalse()

        assertThat(FocusDirection.NEXT_ROW.isSecondary()).isFalse()
        assertThat(FocusDirection.PREVIOUS_ROW.isSecondary()).isFalse()
        assertThat(FocusDirection.NEXT_COLUMN.isSecondary()).isTrue()
        assertThat(FocusDirection.PREVIOUS_COLUMN.isSecondary()).isTrue()
    }

}
