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
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRelativeItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testfixtures.LayoutConfig
import com.rubensousa.dpadrecyclerview.testfixtures.LayoutManagerAssertions
import com.rubensousa.dpadrecyclerview.testfixtures.RowLayout
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Before
import org.junit.Test

class HorizontalRowTest : DpadRecyclerViewTest() {

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

    private lateinit var row: RowLayout
    private var itemWidth: Int = 0
    private var itemHeight: Int = 0

    @Before
    fun setup() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        val itemViewBounds = getRelativeItemViewBounds(position = 0)
        itemWidth = itemViewBounds.width()
        itemHeight = itemViewBounds.height()
        row = RowLayout(
            LayoutConfig(
                parentWidth = recyclerViewBounds.width(),
                parentHeight = recyclerViewBounds.height(),
                viewWidth = itemViewBounds.width(),
                viewHeight = itemViewBounds.height(),
                defaultItemCount = 1000,
                parentKeyline = 0,
                childKeyline = 0.0f
            )
        )
        row.init(position = 0)
    }

    @Test
    fun testNewViewIsLaidOutInDirectionOfScroll() {
        val lastViewPosition = row.getChildCount() - 1

        scrollRight()
        val lastVisibleView = row.getLastView()!!
        val bounds = getRelativeItemViewBounds(position = lastViewPosition + 1)
        assertThat(bounds).isEqualTo(lastVisibleView.bounds.asRect())
    }

    @Test
    fun testExtraSpaceIsNotLaidOutAfterFirstLayout() {
        assertChildrenPositions()
    }

    @Test
    fun testNoExtraSpaceIsAddedWhenScrollingByDefault() {

        repeat(1) {
            scrollRight()
        }

        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtEnd() {
        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateEndExtraLayoutSpace(state: RecyclerView.State): Int {
                    return row.getSize()
                }
            })
        }
        row.setExtraLayoutSpace(end = row.getSize())
        assertChildrenPositions()
    }

    @Test
    fun testExtraLayoutSpaceIsAddedAtStart() {
        repeat(10) {
            scrollRight()
        }
        onRecyclerView("Change extra layout space") { recyclerView ->
            recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateStartExtraLayoutSpace(state: RecyclerView.State): Int {
                    return row.getSize()
                }
            })
        }
        row.setExtraLayoutSpace(start = row.getSize())
        assertChildrenPositions()
    }

    @Test
    fun testRequestLayoutDuringScrollStillAlignsViews() {
        repeat(5) {
            scrollRight()
            onRecyclerView("RequestLayout") { recyclerView ->
                recyclerView.requestLayout()
            }
        }
        assertChildrenPositions()
    }

    private fun scrollLeft() {
        KeyEvents.pressLeft()
        row.scrollLeft()
    }

    private fun scrollRight() {
        KeyEvents.pressRight()
        row.scrollRight()
    }

    private fun assertChildrenPositions() {
        waitForIdleScrollState()
        val expectedChildCount = row.getChildCount()
        var childCount = 0
        onRecyclerView("Getting child count") { recyclerView ->
            childCount = recyclerView.layoutManager?.childCount ?: 0
        }
        assertThat(childCount).isEqualTo(expectedChildCount)
        onRecyclerView("Assert children positions") { recyclerView ->
            LayoutManagerAssertions.assertChildrenBounds(recyclerView.layoutManager!!, row)
        }
    }

}
