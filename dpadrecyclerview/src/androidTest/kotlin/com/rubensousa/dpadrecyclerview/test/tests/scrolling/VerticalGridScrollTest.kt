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

package com.rubensousa.dpadrecyclerview.test.tests.scrolling

import androidx.recyclerview.widget.RecyclerView
import androidx.test.platform.app.InstrumentationRegistry
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testfixtures.LayoutConfig
import com.rubensousa.dpadrecyclerview.testfixtures.VerticalGridLayout
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class VerticalGridScrollTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    private val spanCount = 5
    private val numberOfItems = 200

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = spanCount,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offset = 0,
                fraction = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                fraction = 0.5f
            )
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(
            numberOfItems = numberOfItems,
            itemLayoutId = R.layout.dpadrecyclerview_test_item_grid
        )
    }

    @Test
    fun testScrollingDown() {
        launchFragment()
        val grid = createGrid()
        grid.init(position = 0)

        repeat(5) { step ->
            scrollDown(grid)
            val nextPosition = (step + 1) * spanCount
            assertFocusAndSelection(nextPosition)
            assertChildrenPositions(grid)
        }
    }

    @Test
    fun testScrollingUp() {
        launchFragment()
        val grid = createGrid()
        val startPosition = numberOfItems - 1
        grid.init(position = startPosition)
        selectPosition(position = startPosition)

        repeat(5) { step ->
            scrollUp(grid)
            val previousPosition = startPosition - (step + 1) * spanCount
            assertFocusAndSelection(previousPosition)
            assertChildrenPositions(grid)
        }
    }

    @Test
    fun testScrollingLeft() {
        launchFragment()
        val grid = createGrid()
        val startPosition = spanCount - 1
        grid.init(position = startPosition)
        selectPosition(position = startPosition)

        repeat(spanCount - 1) { step ->
            KeyEvents.pressLeft()
            assertFocusAndSelection(startPosition - 1 - step)
            assertChildrenPositions(grid)
        }

        repeat(spanCount - 1) {
            KeyEvents.pressLeft()
            assertFocusAndSelection(0)
        }
    }

    @Test
    fun testScrollingRight() {
        launchFragment()
        val grid = createGrid()
        val startPosition = 0
        grid.init(position = startPosition)

        repeat(spanCount - 1) { step ->
            KeyEvents.pressRight()
            assertFocusAndSelection(startPosition + step + 1)
            assertChildrenPositions(grid)
        }

        repeat(spanCount - 1) {
            KeyEvents.pressRight()
            assertFocusAndSelection(spanCount - 1)
        }
    }

    @Test
    fun testContinuousScrollForwardAndBackwards() {
        launchFragment(
            getDefaultLayoutConfiguration().copy(
                focusableDirection = FocusableDirection.CONTINUOUS
            )
        )
        KeyEvents.pressRight(times = 10)
        waitForIdleScrollState()
        assertFocusPosition(position = 10)

        KeyEvents.pressLeft(times = 10)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testCircularFocusAroundGrid() {
        launchFragment(
            getDefaultLayoutConfiguration().copy(
                focusableDirection = FocusableDirection.CIRCULAR
            )
        )

        repeat(2) {
            repeat(spanCount - 1) { step ->
                KeyEvents.pressRight()
                assertFocusAndSelection(position = step + 1)
            }
            KeyEvents.pressRight()
            assertFocusAndSelection(position = 0)
        }

        KeyEvents.pressLeft()
        val startPosition = spanCount - 1
        assertFocusAndSelection(position = startPosition)

        repeat(2) {
            repeat(spanCount - 1) { step ->
                KeyEvents.pressLeft()
                assertFocusAndSelection(position = startPosition - 1 - step)
            }
            KeyEvents.pressLeft()
            assertFocusAndSelection(position = startPosition)
        }
    }

    @Test
    fun testMultipleSpanFocusChanges() {
        launchFragment()
        onRecyclerView("Change span size lookup") { recyclerView ->
            recyclerView.setSpanSizeLookup(object : DpadSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0) {
                        spanCount
                    } else {
                        1
                    }
                }
            })
        }
        val scrollDownTimes = 10
        KeyEvents.pressDown()
        assertFocusAndSelection(position = 1)
        repeat(spanCount) { spanIndex ->
            KeyEvents.pressDown(times = scrollDownTimes)
            KeyEvents.pressUp(times = scrollDownTimes + 1)
            assertFocusAndSelection(position = 0)
            KeyEvents.pressDown()
            assertFocusAndSelection(position = spanIndex + 1)
            if (spanIndex != spanCount - 1) {
                KeyEvents.pressRight()
                assertFocusAndSelection(position = spanIndex + 2)
            }
        }
    }

    @Test
    fun testMultipleDifferentSpanFocusChanges() {
        launchFragment()
        onRecyclerView("Change span size lookup") { recyclerView ->
            recyclerView.setSpanSizeLookup(object : DpadSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0 || position.rem(spanCount + 1) == 0) {
                        spanCount
                    } else {
                        1
                    }
                }
            })
        }
        KeyEvents.pressDown()
        assertFocusAndSelection(position = 1)
        repeat(spanCount) { spanIndex ->
            KeyEvents.pressDown()
            assertFocusAndSelection(position = spanCount + 1)
            KeyEvents.pressDown()
            assertFocusAndSelection(position = spanCount + 2 + spanIndex)
            KeyEvents.pressUp()
            assertFocusAndSelection(position = spanCount + 1)
            KeyEvents.pressUp()
            assertFocusAndSelection(spanIndex + 1)
            if (spanIndex != spanCount - 1) {
                KeyEvents.pressRight()
                assertFocusAndSelection(position = spanIndex + 2)
            }
        }
    }

    @Test
    fun testMultipleFastDifferentSpanFocusChanges() {
        launchFragment()
        onRecyclerView("Change span size lookup") { recyclerView ->
            recyclerView.setSpanSizeLookup(object : DpadSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position == 0 || position.rem(spanCount + 1) == 0) {
                        spanCount
                    } else {
                        1
                    }
                }
            })
        }
        KeyEvents.pressDown()
        assertFocusAndSelection(position = 1)
        KeyEvents.pressRight()
        repeat(25) {
            KeyEvents.pressDown()
        }
        assertFocusAndSelection(position = 78)
    }

    private fun scrollUp(grid: VerticalGridLayout) {
        KeyEvents.pressUp()
        grid.scrollUp()
    }

    private fun scrollDown(grid: VerticalGridLayout) {
        KeyEvents.pressDown()
        grid.scrollDown()
    }

    private fun createGrid(): VerticalGridLayout {
        val recyclerViewBounds = getRecyclerViewBounds()
        val itemWidth = recyclerViewBounds.width() / spanCount
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val itemHeight = resources.getDimensionPixelOffset(
            R.dimen.dpadrecyclerview_test_grid_item_size
        )

        return VerticalGridLayout(
            config = LayoutConfig(
                parentWidth = recyclerViewBounds.width(),
                parentHeight = recyclerViewBounds.height(),
                viewWidth = itemWidth,
                viewHeight = itemHeight,
                defaultItemCount = numberOfItems,
                parentKeyline = recyclerViewBounds.height() / 2,
                childKeyline = 0.5f
            ),
            spanCount = spanCount
        )
    }

}
