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

package com.rubensousa.dpadrecyclerview.testfixtures

import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.offsetVerticallyBy
import org.junit.Test

class ColumnLayoutTest {

    private val screenWidth = 1920
    private val screenHeight = 1080
    private val childWidth = screenWidth
    private val childHeight = 300
    private val column = ColumnLayout(
        LayoutConfig(
            parentWidth = screenWidth,
            parentHeight = screenHeight,
            viewWidth = childWidth,
            viewHeight = childHeight,
            defaultItemCount = 1000,
            parentKeyline = screenHeight / 2,
            childKeyline = 0.5f
        )
    )

    private val expectedFirstBounds = listOf(
        ViewBounds(left = 0, top = 390, right = screenWidth, bottom = 690),
        ViewBounds(left = 0, top = 690, right = screenWidth, bottom = 990),
        ViewBounds(left = 0, top = 990, right = screenWidth, bottom = 1290)
    )
    private val expectedSecondBounds = listOf(
        ViewBounds(left = 0, top = 90, right = screenWidth, bottom = 390),
        ViewBounds(left = 0, top = 390, right = screenWidth, bottom = 690),
        ViewBounds(left = 0, top = 690, right = screenWidth, bottom = 990),
        ViewBounds(left = 0, top = 990, right = screenWidth, bottom = 1290)
    )
    private val expectedOtherBounds = listOf(
        ViewBounds(left = 0, top = -210, right = screenWidth, bottom = 90),
        ViewBounds(left = 0, top = 90, right = screenWidth, bottom = 390),
        ViewBounds(left = 0, top = 390, right = screenWidth, bottom = 690),
        ViewBounds(left = 0, top = 690, right = screenWidth, bottom = 990),
        ViewBounds(left = 0, top = 990, right = screenWidth, bottom = 1290)
    )

    @Test
    fun `initial layout renders only views that fit in the screen`() {
        column.init(position = 0)
        column.assertViewBounds(expectedFirstBounds)
    }

    @Test
    fun `layout at any position renders correct alignment`() {
        column.init(position = 1)
        column.assertViewBounds(expectedSecondBounds)

        repeat(10) { index ->
            column.init(position = 2 + index)
            column.assertViewBounds(expectedOtherBounds)
        }
    }

    @Test
    fun `scroll down does not add any extra space by default`() {
        column.init(position = 0)

        column.scrollDown()
        column.assertViewBounds(expectedSecondBounds)

        repeat(10) {
            column.scrollDown()
            column.assertViewBounds(expectedOtherBounds)
        }

    }

    @Test
    fun `scroll up does not add any extra space by default`() {
        column.init(position = 10)

        while (column.selectedPosition > 1) {
            column.assertViewBounds(expectedOtherBounds)
            column.scrollUp()
        }

        column.assertViewBounds(expectedSecondBounds)

        column.scrollUp()

        column.assertViewBounds(expectedFirstBounds)
    }

    @Test
    fun `partial scroll honors extra space required`() {
        column.init(position = 0)
        val scrollDistance = childHeight / 2
        column.scrollBy(scrollDistance)
        column.assertViewBounds(expectedFirstBounds.offsetVerticallyBy(-scrollDistance))
    }


}