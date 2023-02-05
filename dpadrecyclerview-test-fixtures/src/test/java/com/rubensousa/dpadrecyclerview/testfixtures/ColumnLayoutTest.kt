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
    private val itemCount = 1000
    private val childWidth = screenWidth
    private val childHeight = 300
    private val config = LayoutConfig(
        parentWidth = screenWidth,
        parentHeight = screenHeight,
        viewWidth = childWidth,
        viewHeight = childHeight,
        defaultItemCount = itemCount,
        parentKeyline = screenHeight / 2,
        childKeyline = 0.5f
    )
    private var column = ColumnLayout(config)
    private val centeredBounds = ViewBounds(
        top = 390, bottom = 690, left = 0, right = screenWidth
    )
    private val topBounds = ViewBounds(
        top = 0, bottom = childHeight, left = 0, right = screenWidth
    )
    private val bottomBounds = ViewBounds(
        top = screenHeight - childHeight, bottom = screenHeight, left = 0, right = screenWidth
    )

    private val expectedFirstBounds = listOf(
        centeredBounds,
        centeredBounds.next(1),
        centeredBounds.next(2)
    )
    private val expectedSecondBounds = listOf(
        centeredBounds.next(-1),
        centeredBounds,
        centeredBounds.next(1),
        centeredBounds.next(2)
    )
    private val expectedOtherBounds = listOf(
        centeredBounds.next(-2),
        centeredBounds.next(-1),
        centeredBounds,
        centeredBounds.next(1),
        centeredBounds.next(2)
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

    @Test
    fun `last view is aligned to keyline`() {
        column.init(position = itemCount - 1)
        column.assertViewBounds(
            listOf(
                centeredBounds.next(-2),
                centeredBounds.next(-1),
                centeredBounds
            )
        )
    }

    private fun ViewBounds.next(index: Int): ViewBounds {
        return ViewBounds(
            top = this.top + childHeight * index,
            bottom = this.bottom + childHeight * index,
            left = this.left,
            right = this.right
        )
    }


}