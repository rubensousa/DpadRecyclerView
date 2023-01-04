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
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.offsetHorizontallyBy
import org.junit.Test

class RowLayoutTest {

    private val screenWidth = 1920
    private val screenHeight = 1080
    private val childWidth = 500
    private val childHeight = screenHeight
    private val row = RowLayout(
        LayoutConfig(
            parentWidth = screenWidth,
            parentHeight = screenHeight,
            viewWidth = childWidth,
            viewHeight = childHeight,
            defaultItemCount = 1000,
            parentKeyline = screenWidth / 2,
            childKeyline = 0.5f
        )
    )

    private val expectedFirstBounds = listOf(
        ViewBounds(top = 0, bottom = screenHeight, left = 710, right = 1210),
        ViewBounds(top = 0, bottom = screenHeight, left = 1210, right = 1710),
        ViewBounds(top = 0, bottom = screenHeight, left = 1710, right = 2210),
    )
    private val expectedSecondBounds = listOf(
        ViewBounds(top = 0, bottom = screenHeight, left = 210, right = 710),
        ViewBounds(top = 0, bottom = screenHeight, left = 710, right = 1210),
        ViewBounds(top = 0, bottom = screenHeight, left = 1210, right = 1710),
        ViewBounds(top = 0, bottom = screenHeight, left = 1710, right = 2210),
    )
    private val expectedOtherBounds = listOf(
        ViewBounds(top = 0, bottom = screenHeight, left = -290, right = 210),
        ViewBounds(top = 0, bottom = screenHeight, left = 210, right = 710),
        ViewBounds(top = 0, bottom = screenHeight, left = 710, right = 1210),
        ViewBounds(top = 0, bottom = screenHeight, left = 1210, right = 1710),
        ViewBounds(top = 0, bottom = screenHeight, left = 1710, right = 2210),
    )

    @Test
    fun `initial layout renders only views that fit in the screen`() {
        row.init(position = 0)
        row.assertViewBounds(expectedFirstBounds)
    }

    @Test
    fun `layout at any position renders correct alignment`() {
        row.init(position = 1)
        row.assertViewBounds(expectedSecondBounds)

        repeat(10) { index ->
            row.init(position = 2 + index)
            row.assertViewBounds(expectedOtherBounds)
        }
    }

    @Test
    fun `scroll right does not add any extra space by default`() {
        row.init(position = 0)

        row.scrollRight()
        row.assertViewBounds(expectedSecondBounds)

        repeat(10) {
            row.scrollRight()
            row.assertViewBounds(expectedOtherBounds)
        }

    }

    @Test
    fun `scroll left does not add any extra space by default`() {
        row.init(position = 10)

        while (row.selectedPosition > 1) {
            row.assertViewBounds(expectedOtherBounds)
            row.scrollLeft()
        }

        row.assertViewBounds(expectedSecondBounds)

        row.scrollLeft()

        row.assertViewBounds(expectedFirstBounds)
    }

    @Test
    fun `partial scroll honors extra space required`() {
        row.init(position = 0)
        val scrollDistance = childWidth / 4
        row.scrollBy(scrollDistance)
        row.assertViewBounds(expectedFirstBounds.offsetHorizontallyBy(-scrollDistance))
    }


}