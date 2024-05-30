/*
 * Copyright 2024 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.test.tests.mutation

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertItemAtPosition
import com.rubensousa.dpadrecyclerview.test.helpers.getRelativeItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.tests.AbstractTestAdapter
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GridAdapterMutationTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 4,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX,
                fraction = 0.5f
            ),
            childAlignment = ChildAlignment(
                fraction = 0.5f
            )
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(
            numberOfItems = 40
        )
    }

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testGridRemovalWithSpanLookupDoesNotCrash() {
        onRecyclerView("Change span size") { recyclerView ->
            recyclerView.setSpanSizeLookup(object : DpadSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val adapter = recyclerView.adapter as AbstractTestAdapter<*>
                    val item = adapter.getItem(position)
                    return if (item % 9 == 0) {
                        recyclerView.getSpanCount()
                    } else {
                        1
                    }
                }
            })
        }
        KeyEvents.pressDown()
        val oldViewBounds = getRelativeItemViewBounds(position = 1)
        mutateAdapter { adapter ->
            adapter.removeAt(3)
            adapter.removeAt(13)
        }
        assertFocusAndSelection(1)
        assertItemAtPosition(position = 1, item = 1)

        val newViewBounds = getRelativeItemViewBounds(position = 1)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)
    }

}
