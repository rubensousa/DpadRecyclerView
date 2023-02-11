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

package com.rubensousa.dpadrecyclerview.test.tests.alignment

import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.updateChildAlignment
import com.rubensousa.dpadrecyclerview.test.helpers.updateParentAlignment
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs

class VerticalAlignmentTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = 0,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            )
        )
    }

    @Test
    fun testMiddleItemsAreAlignedToParentOffsets() {
        launchFragment()
        KeyEvents.pressDown(times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
            KeyEvents.pressDown()
            position++
        }

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 100
            )
        )
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() + 100)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 0
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = -100
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() - 100)
    }

    @Test
    fun testMiddleItemsAreAlignedToChildOffsets() {
        launchFragment()
        KeyEvents.pressDown(times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
            KeyEvents.pressDown()
            position++
        }

        updateChildAlignment(ChildAlignment(offset = 100))
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() - 100)

        updateChildAlignment(ChildAlignment(offset = 0))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateChildAlignment(ChildAlignment(offset = -100))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() + 100)
    }

    @Test
    fun testFirstItemAlignmentForEdgeAlignments() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(viewBounds.height() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(viewBounds.height() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
    }

    @Test
    fun testLastItemAlignmentForEdgeAlignments() {
        launchFragment()
        val lastPosition = selectLastPosition()
        val recyclerViewBounds = getRecyclerViewBounds()

        var viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)
    }

    @Test
    fun testItemsAreAlignedToParentOffset() {
        val offset = 100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = offset,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0f
            )
        )
        KeyEvents.pressDown(times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.top).isEqualTo(recyclerViewBounds.centerY() + offset)
            KeyEvents.pressDown()
        }
    }

    @Test
    fun testItemsAreAlignedToBothParentAndChildAlignmentOffsets() {
        val containerOffset = 100
        val itemOffset = -100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = containerOffset,
                offsetRatio = 0f
            ),
            childAlignment = ChildAlignment(
                offset = itemOffset,
                offsetRatio = 0f
            )
        )
        KeyEvents.pressDown(times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.top)
                .isEqualTo(recyclerViewBounds.top + containerOffset + abs(itemOffset))
            KeyEvents.pressDown()
        }
    }

    @Test
    fun testItemsAreAlignedToBothParentAndChildAlignmentOffsetRatios() {
        val containerOffset = 100
        val itemOffset = -100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = containerOffset,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = itemOffset,
                offsetRatio = 0.5f
            )
        )
        KeyEvents.pressDown(times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.centerY())
                .isEqualTo(recyclerViewBounds.centerY() + containerOffset + abs(itemOffset))
            KeyEvents.pressDown()
        }
    }

    @Test
    fun testGravityAffectsBoundsOfItems() {
        val parentAlignment = ParentAlignment(
            edge = Edge.NONE,
            offset = 0,
            offsetRatio = 0.5f
        )
        val layoutConfig = TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            gravity = Gravity.CENTER,
            parentAlignment = parentAlignment,
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            )
        )
        val adapterConfig = TestAdapterConfiguration(
            itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal
        )
        launchFragment(layoutConfig, adapterConfig)

        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        onRecyclerView("Changing gravity to END") { recyclerView ->
            recyclerView.setGravity(Gravity.END)
        }

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.right).isEqualTo(recyclerViewBounds.right)

        onRecyclerView("Changing gravity to START") { recyclerView ->
            recyclerView.setGravity(Gravity.START)
        }

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.left).isEqualTo(recyclerViewBounds.left)
    }

    @Test
    fun testLayoutAlignsToStartEdgeWhenThereAreNotManyItems() {
        launchFragment(
            getDefaultLayoutConfiguration(),
            getDefaultAdapterConfiguration().copy(numberOfItems = 4)
        )
        selectPosition(position = 3)
        onRecyclerView("Request new layout") { recyclerView ->
            recyclerView.requestLayout()
        }
        val viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.top).isEqualTo(0)
    }

    @Test
    fun testLayoutAlignsToKeylineWhenThereAreNotManyItems() {
        val parentAlignment = ParentAlignment(
            edge = Edge.MIN,
            offset = 0,
            offsetRatio = 0.5f,
            preferKeylineOverEdge = true
        )
        launchFragment(
            getDefaultLayoutConfiguration().copy(parentAlignment = parentAlignment),
            getDefaultAdapterConfiguration().copy(numberOfItems = 2)
        )
        val viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(getRecyclerViewBounds().centerY())
    }

    @Test
    fun testLayoutAlignsToMaxEdgeWhenThereAreNotManyItems() {
        val parentAlignment = ParentAlignment(
            edge = Edge.MAX,
            offset = 0,
            offsetRatio = 0.5f,
            preferKeylineOverEdge = false
        )
        launchFragment(
            getDefaultLayoutConfiguration().copy(parentAlignment = parentAlignment),
            getDefaultAdapterConfiguration().copy(numberOfItems = 3)
        )
        val viewBounds = getItemViewBounds(position = 2)
        assertThat(viewBounds.bottom).isEqualTo(getRecyclerViewBounds().bottom)
    }

    @Test
    fun testReverseLayoutAlignsToStartEdgeWhenThereAreNotManyItems() {
        launchFragment(
            getDefaultLayoutConfiguration().copy(reverseLayout = true),
            getDefaultAdapterConfiguration().copy(numberOfItems = 4)
        )
        selectPosition(position = 3)
        onRecyclerView("Request new layout") { recyclerView ->
            recyclerView.requestLayout()
        }
        val recyclerViewBounds = getRecyclerViewBounds()
        val viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)
    }

}
