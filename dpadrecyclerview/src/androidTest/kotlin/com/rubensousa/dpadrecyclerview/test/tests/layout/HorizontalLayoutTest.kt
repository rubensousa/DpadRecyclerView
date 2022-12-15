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
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRelativeItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Before
import org.junit.Test

class HorizontalLayoutTest : DpadRecyclerViewTest() {

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
    fun testExtraSpaceIsNotLaidOutAfterFirstLayout() {
        row.append(row.width, itemWidth, itemHeight)
        assertChildrenPositions()
    }

    @Test
    fun testNewViewIsLaidOutInDirectionOfScroll() {
        row.append(row.width, itemWidth, itemHeight)

        KeyEvents.pressRight()
        row.scrollHorizontallyBy(-itemWidth)
        val newView = row.append(itemWidth, itemHeight)

        val bounds = getRelativeItemViewBounds(position = row.getNumberOfViewsInLayout() - 1)
        assertThat(bounds).isEqualTo(newView.bounds)
    }

    private fun assertChildrenPositions() {
        onRecyclerView("Assert children positions") { recyclerView ->
            assertThat(recyclerView.childCount).isEqualTo(row.getNumberOfViewsInLayout())
            row.assertChildrenBounds(recyclerView)
        }
    }

}
