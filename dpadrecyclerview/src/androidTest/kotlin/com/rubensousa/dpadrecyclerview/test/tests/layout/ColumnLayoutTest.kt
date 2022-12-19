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
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRelativeItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.max

class ColumnLayoutTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offsetRatio = 0.0f
            ),
            childAlignment = ChildAlignment(
                offsetRatio = 0.0f
            )
        )
    }

    private lateinit var column: LayoutColumn
    private var itemWidth: Int = 0
    private var itemHeight: Int = 0

    @Before
    fun setup() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        val itemViewBounds = getRelativeItemViewBounds(position = 0)
        itemWidth = itemViewBounds.width()
        itemHeight = itemViewBounds.height()
        column = LayoutColumn(
            width = recyclerViewBounds.width(),
            height = recyclerViewBounds.height()
        )
    }

    @Test
    fun testNewViewIsLaidOutInDirectionOfScroll() {
        appendPage()
        val lastViewPosition = column.getNumberOfViewsInLayout() - 1

        val lastVisibleView = scrollDown()
        val bounds = getRelativeItemViewBounds(position = lastViewPosition + 1)
        assertThat(bounds).isEqualTo(lastVisibleView.bounds)
    }

    @Test
    fun testExtraSpaceIsNotLaidOutAfterFirstLayout() {
        appendPage()
        assertChildrenPositions()
    }

    @Test
    fun testNoExtraSpaceIsAddedWhenScrollingByDefault() {
        appendPage()

        repeat(30) {
            scrollDown()
        }

        assertChildrenPositions()

        repeat(30) {
            scrollUp()
        }

        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtEnd() {
        appendPage()
        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    extraLayoutSpace: IntArray
                ) {
                    extraLayoutSpace[1] = recyclerView.height
                }
            })
        }
        appendPage()
        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtTop() {
        appendPage()
        repeat(30) {
            scrollDown()
        }

        assertChildrenPositions()

        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    extraLayoutSpace: IntArray
                ) {
                    extraLayoutSpace[0] = recyclerView.height
                }
            })
        }
        prependPage()
        assertChildrenPositions()
    }

    @Test
    fun testRequestLayoutDuringScrollStillAlignsViews() {
        appendPage()
        repeat(5) {
            scrollDown()
            onRecyclerView("RequestLayout") { recyclerView ->
                recyclerView.requestLayout()
            }
        }
        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtTopDuringScroll() {
        appendPage()
        repeat(30) {
            scrollDown()
        }
        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    extraLayoutSpace: IntArray
                ) {
                    extraLayoutSpace[0] = recyclerView.height
                }
            })
        }
        column.clear()
        appendPage()
        prependPage()
        assertChildrenPositions()
    }

    private fun prependPage(count: Int = 1) {
        require(count > 0)
        column.prepend(column.height * count, itemWidth, itemHeight)
    }

    private fun appendPage(count: Int = 1) {
        require(count > 0)
        column.append(column.height * count, itemWidth, itemHeight)
    }

    private fun scrollUp(
        extraLayoutSpaceStart: Int = 0,
        extraLayoutSpaceEnd: Int = 0
    ): ViewItem {
        KeyEvents.pressUp()
        column.scrollBy(itemHeight)
        val newView = column.prepend(itemWidth, itemHeight)
        val availableScrollSpace = max(0, -column.getFirstView()!!.getDecoratedTop())
        val extraFillSpace = extraLayoutSpaceStart - availableScrollSpace
        column.prepend(extraFillSpace, itemWidth, itemHeight)
        column.recycleEnd(extraLayoutSpaceEnd)
        return newView
    }

    private fun scrollDown(
        extraLayoutSpaceStart: Int = 0,
        extraLayoutSpaceEnd: Int = 0,
    ): ViewItem {
        KeyEvents.pressDown()
        column.scrollBy(-itemHeight)
        val newView = column.append(itemWidth, itemHeight)
        val availableScrollSpace =
            max(0, column.getLastView()!!.getDecoratedBottom() - column.height)
        val extraFillSpace = max(0, extraLayoutSpaceEnd - availableScrollSpace)
        column.append(extraFillSpace, itemWidth, itemHeight)
        column.recycleStart(extraLayoutSpaceStart)
        return newView
    }

    private fun assertChildrenPositions() {
        waitForIdleScrollState()
        onRecyclerView("Assert children positions") { recyclerView ->
            assertThat(recyclerView.childCount).isEqualTo(column.getNumberOfViewsInLayout())
            column.assertChildrenBounds(recyclerView)
        }
    }

}
