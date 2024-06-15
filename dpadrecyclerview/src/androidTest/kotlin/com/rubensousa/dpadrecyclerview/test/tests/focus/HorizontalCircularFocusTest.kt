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

package com.rubensousa.dpadrecyclerview.test.tests.focus

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForAdapterUpdate
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HorizontalCircularFocusTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    private val numberOfItems = 3

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX
            ),
            childAlignment = ChildAlignment(offset = 0)
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(
            numberOfItems = numberOfItems,
            itemLayoutId = com.rubensousa.dpadrecyclerview.testing.R.layout.dpadrecyclerview_test_item_horizontal
        )
    }

    @Before
    fun setup() {
        launchFragment()
        onRecyclerView("Set focusable direction") { recyclerView ->
            recyclerView.setFocusableDirection(FocusableDirection.CIRCULAR)
        }
    }

    @Test
    fun testKeyUpMovesToLastPosition() {
        // when
        KeyEvents.pressLeft()
        waitForIdleScrollState()

        // then
        assertFocusAndSelection(numberOfItems - 1)
    }

    @Test
    fun testKeyDownMovesToFirstPosition() {
        // when
        KeyEvents.pressRight(numberOfItems)
        waitForIdleScrollState()

        // then
        assertFocusAndSelection(0)
    }

    @Test
    fun testCircularFocusDoesNotWorkIfLayoutIsFilled() {
        // given
        mutateAdapter { adapter ->
            adapter.submitList(MutableList(10) { it })
        }
        waitForAdapterUpdate()

        // when
        KeyEvents.pressLeft()

        // then
        assertFocusAndSelection(position = 0)
    }

}
