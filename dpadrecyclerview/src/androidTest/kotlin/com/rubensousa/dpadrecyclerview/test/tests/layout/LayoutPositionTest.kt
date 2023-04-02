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
import kotlin.math.ceil
import kotlin.math.floor

class LayoutPositionTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
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

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testFirstVisibleItemPosition() {
        assertThat(findFirstVisibleItemPosition()).isEqualTo(0)

        KeyEvents.pressDown(5)

        waitForIdleScrollState()
        assertThat(findFirstVisibleItemPosition()).isEqualTo(2)

        executeOnFragment { fragment ->
            fragment.clearAdapter()
        }

        assertThat(findFirstVisibleItemPosition()).isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun testFirstCompletelyVisibleItemPosition() {
        assertThat(findFirstCompletelyVisibleItemPosition()).isEqualTo(0)

        KeyEvents.pressDown(6)

        waitForIdleScrollState()
        assertThat(findFirstCompletelyVisibleItemPosition()).isEqualTo(4)

        executeOnFragment { fragment ->
            fragment.clearAdapter()
        }

        assertThat(findFirstCompletelyVisibleItemPosition()).isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun testLastVisibleItemPosition() {
        val itemBounds = getRelativeItemViewBounds(position = 0)
        val parentBounds = getRecyclerViewBounds()
        val numberOfItemsPerHeight = ceil(parentBounds.height().toDouble() / itemBounds.height())
            .toInt()
        assertThat(findLastVisibleItemPosition()).isEqualTo(numberOfItemsPerHeight - 1)

        KeyEvents.pressDown(times = 5)

        waitForIdleScrollState()
        assertThat(findLastVisibleItemPosition()).isEqualTo(8)

        executeOnFragment { fragment ->
            fragment.clearAdapter()
        }

        assertThat(findLastVisibleItemPosition()).isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun testLastCompletelyVisibleItemPosition() {
        val itemBounds = getRelativeItemViewBounds(position = 0)
        val parentBounds = getRecyclerViewBounds()
        val numberOfItemsPerHeight = floor(
            parentBounds.height().toDouble() / itemBounds.height().toDouble()
        ).toInt()

        assertThat(findLastCompletelyVisibleItemPosition()).isEqualTo(numberOfItemsPerHeight - 1)


        KeyEvents.pressDown(times = 5)

        waitForIdleScrollState()
        assertThat(findLastCompletelyVisibleItemPosition()).isEqualTo(7)

        executeOnFragment { fragment ->
            fragment.clearAdapter()
        }

        assertThat(findLastVisibleItemPosition()).isEqualTo(RecyclerView.NO_POSITION)
    }

    private fun findFirstVisibleItemPosition(): Int {
        return getItemPosition(first = true, completelyVisible = false)
    }

    private fun findFirstCompletelyVisibleItemPosition(): Int {
        return getItemPosition(first = true, completelyVisible = true)
    }

    private fun findLastVisibleItemPosition(): Int {
        return getItemPosition(first = false, completelyVisible = false)
    }

    private fun findLastCompletelyVisibleItemPosition(): Int {
        return getItemPosition(first = false, completelyVisible = true)
    }

    private fun getItemPosition(first: Boolean, completelyVisible: Boolean): Int {
        val description = StringBuilder("Finding ")
        description.append(if (first) "first" else "last")
        if (completelyVisible) {
            description.append("completely ")
        }
        description.append("visible position")
        var position = RecyclerView.NO_POSITION
        onRecyclerView(description.toString()) { recyclerView ->
            position = if (first) {
                if (completelyVisible) {
                    recyclerView.findFirstCompletelyVisibleItemPosition()
                } else {
                    recyclerView.findFirstVisibleItemPosition()
                }
            } else {
                if (completelyVisible) {
                    recyclerView.findLastCompletelyVisibleItemPosition()
                } else {
                    recyclerView.findLastVisibleItemPosition()
                }
            }
        }
        return position
    }

}
