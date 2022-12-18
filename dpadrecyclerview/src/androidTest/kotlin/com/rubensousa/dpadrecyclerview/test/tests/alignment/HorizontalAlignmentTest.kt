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
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.updateChildAlignment
import com.rubensousa.dpadrecyclerview.test.helpers.updateParentAlignment
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class HorizontalAlignmentTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
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

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(
            itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal
        )
    }

    @Test
    fun testMiddleItemsAreAlignedToContainerOffsets() {
        launchFragment()
        KeyEvents.pressRight(times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
            KeyEvents.pressRight()
            position++
            waitForIdleScrollState()
        }

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 100
            )
        )
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() + 100)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 0
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = -100
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() - 100)
    }

    @Test
    fun testMiddleItemsAreAlignedToItemOffsets() {
        launchFragment()
        KeyEvents.pressRight(times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
            KeyEvents.pressRight()
            position++
        }

        updateChildAlignment(ChildAlignment(offset = 100))
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() + 100)

        updateChildAlignment(ChildAlignment(offset = 0))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateChildAlignment(ChildAlignment(offset = -100))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() - 100)
    }

    @Test
    fun testFirstItemAlignmentForEdgeAlignments() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(viewBounds.width() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(viewBounds.width() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
    }

    @Test
    fun testLastItemAlignmentForEdgeAlignments() {
        launchFragment()
        val lastPosition = selectLastPosition()
        val recyclerViewBounds = getRecyclerViewBounds()

        var viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.right).isEqualTo(recyclerViewBounds.right)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertFocusPosition(lastPosition)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
        assertFocusPosition(lastPosition)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.right).isEqualTo(recyclerViewBounds.right)
        assertFocusPosition(lastPosition)
    }

    @Test
    fun testItemsAreAlignedToContainerOffset() {
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
        KeyEvents.pressRight(times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.left).isEqualTo(recyclerViewBounds.centerX() + offset)
            KeyEvents.pressRight()
        }
    }

    @Test
    fun testItemsAreAlignedToBothContainerAndItemAlignmentOffsets() {
        val containerOffset = 100
        val itemOffset = 100
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
        KeyEvents.pressRight(times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.left)
                .isEqualTo(recyclerViewBounds.left + containerOffset + itemOffset)
            KeyEvents.pressRight()
        }
    }

    @Test
    fun testItemsAreAlignedToBothContainerAndItemAlignmentOffsetPercentages() {
        val containerOffset = 100
        val itemOffset = 100
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
        KeyEvents.pressRight(times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.centerX())
                .isEqualTo(recyclerViewBounds.centerX() + containerOffset + itemOffset)
            KeyEvents.pressRight()
            waitForIdleScrollState()
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
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = parentAlignment,
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            ),
            gravity = Gravity.CENTER
        )
        launchFragment(layoutConfig)

        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        onRecyclerView("Changing gravity to BOTTOM") { recyclerView ->
            recyclerView.setGravity(Gravity.BOTTOM)
        }

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)

        onRecyclerView("Changing gravity to TOP") { recyclerView ->
            recyclerView.setGravity(Gravity.TOP)
        }

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
        assertThat(viewBounds.top).isEqualTo(recyclerViewBounds.top)
    }

}
