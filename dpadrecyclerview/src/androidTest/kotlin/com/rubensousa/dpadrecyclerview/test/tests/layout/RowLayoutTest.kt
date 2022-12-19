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

package com.rubensousa.dpadrecyclerview.test.tests.layout

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRelativeItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.max

class RowLayoutTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration()
            .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal)
    }

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offsetRatio = 0.0f
            ),
            childAlignment = ChildAlignment(
                offsetRatio = 0.0f
            )
        )
    }

    private lateinit var row: LayoutRow
    private var itemWidth: Int = 0
    private var itemHeight: Int = 0

    @Before
    fun setup() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        val itemViewBounds = getItemViewBounds(position = 0)
        itemWidth = itemViewBounds.width()
        itemHeight = itemViewBounds.height()
        row = LayoutRow(
            width = recyclerViewBounds.width(),
            height = recyclerViewBounds.height()
        )
    }

    @Test
    fun testNewViewIsLaidOutInDirectionOfScroll() {
        row.append(row.width, itemWidth, itemHeight)
        val lastViewPosition = row.getNumberOfViewsInLayout() - 1

        val lastVisibleView = scrollRight()
        val bounds = getRelativeItemViewBounds(position = lastViewPosition + 1)
        assertThat(bounds).isEqualTo(lastVisibleView.bounds)
    }

    @Test
    fun testExtraSpaceIsNotLaidOutAfterFirstLayout() {
        row.append(row.width, itemWidth, itemHeight)
        assertChildrenPositions()
    }

    @Test
    fun testNoExtraSpaceIsAddedWhenScrollingByDefault() {
        row.append(row.width, itemWidth, itemHeight)

        repeat(10) {
            scrollRight()
        }

        assertChildrenPositions()

        repeat(10) {
            scrollLeft()
        }

        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtEnd() {
        row.append(row.width * 2, itemWidth, itemHeight)
        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    extraLayoutSpace: IntArray
                ) {
                    extraLayoutSpace[1] = recyclerView.width
                }
            })
        }
        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtStart() {
        row.append(row.width, itemWidth, itemHeight)
        repeat(10) {
            scrollRight()
        }

        assertChildrenPositions()

        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    extraLayoutSpace: IntArray
                ) {
                    extraLayoutSpace[0] = recyclerView.width
                }
            })
        }
        row.prepend(row.width, itemWidth, itemHeight)
        assertChildrenPositions()
    }

    @Test
    fun testRequestLayoutDuringScrollStillAlignsViews() {
        row.append(row.width, itemWidth, itemHeight)
        repeat(5) {
            scrollRight()
            onRecyclerView("RequestLayout") { recyclerView ->
                recyclerView.requestLayout()
            }
        }
        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtStartDuringScroll() {
        row.append(row.width, itemWidth, itemHeight)
        repeat(10) {
            scrollRight()
        }
        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    extraLayoutSpace: IntArray
                ) {
                    extraLayoutSpace[0] = recyclerView.width
                }
            })
        }
        row.clear()
        row.append(row.width, itemWidth, itemHeight)
        row.prepend(row.width, itemWidth, itemHeight)
        assertChildrenPositions()
    }

    private fun scrollLeft(
        extraLayoutSpaceStart: Int = 0,
        extraLayoutSpaceEnd: Int = 0
    ): ViewItem {
        KeyEvents.pressLeft()
        row.scrollBy(itemWidth)
        val newView = row.prepend(itemWidth, itemHeight)
        val availableScrollSpace = max(0, -row.getFirstView()!!.getDecoratedLeft())
        val extraFillSpace = extraLayoutSpaceStart - availableScrollSpace
        row.prepend(extraFillSpace, itemWidth, itemHeight)
        row.recycleEnd(extraLayoutSpaceEnd)
        return newView
    }

    private fun scrollRight(
        extraLayoutSpaceStart: Int = 0,
        extraLayoutSpaceEnd: Int = 0,
    ): ViewItem {
        KeyEvents.pressRight()
        row.scrollBy(-itemWidth)
        val newView = row.append(itemWidth, itemHeight)
        val availableScrollSpace = max(0, row.getLastView()!!.getDecoratedRight() - row.width)
        val extraFillSpace = max(0, extraLayoutSpaceEnd - availableScrollSpace)
        row.append(extraFillSpace, itemWidth, itemHeight)
        row.recycleStart(extraLayoutSpaceStart)
        return newView
    }

    private fun assertChildrenPositions() {
        waitForIdleScrollState()
        onRecyclerView("Assert children positions") { recyclerView ->
            assertThat(recyclerView.childCount).isEqualTo(row.getNumberOfViewsInLayout())
            row.assertChildrenBounds(recyclerView)
        }
    }

}
