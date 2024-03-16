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

package com.rubensousa.dpadrecyclerview.test.tests.layout

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LayoutWhileScrollingTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                fraction = 0.5f
            ),
            childAlignment = ChildAlignment(
                fraction = 0.5f
            )
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(numberOfItems = 100)
    }

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testRequestingLayoutDuringSmoothScrollIsIgnored() {
        // given
        var layoutCompleted = 0
        onRecyclerView("Disable layout during scroll") { recyclerView ->
            recyclerView.setLayoutWhileScrollingEnabled(false)
            recyclerView.addOnLayoutCompletedListener(
                object : DpadRecyclerView.OnLayoutCompletedListener {
                    override fun onLayoutCompleted(state: RecyclerView.State) {
                        layoutCompleted++
                    }
                })
        }

        // when
        selectLastPosition(smooth = true)
        repeat(10) {
            onRecyclerView("Request layout") { recyclerView ->
                if (recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.requestLayout()
                }
            }
        }
        waitForIdleScrollState()

        // then
        assertThat(layoutCompleted).isEqualTo(1)
    }

    @Test
    fun testRequestingLayoutDuringKeyEventsIsIgnored() {
        // given
        var layoutCompleted = 0
        onRecyclerView("Disable layout during scroll") { recyclerView ->
            recyclerView.setLayoutWhileScrollingEnabled(false)
            recyclerView.addOnLayoutCompletedListener(
                object : DpadRecyclerView.OnLayoutCompletedListener {
                    override fun onLayoutCompleted(state: RecyclerView.State) {
                        layoutCompleted++
                    }
                })
        }

        // when
        KeyEvents.pressDown(times = 20)
        repeat(10) {
            onRecyclerView("Request layout") { recyclerView ->
                if (recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE) {
                    recyclerView.requestLayout()
                }
            }
        }
        waitForIdleScrollState()

        // then
        assertThat(layoutCompleted).isEqualTo(1)
    }

    @Test
    fun testRequestingLayoutDuringScrollIsNotIgnored() {
        // given
        var layoutCompleted = 0
        val layoutRequests = 2
        onRecyclerView("Enable layout during scroll") { recyclerView ->
            recyclerView.setLayoutWhileScrollingEnabled(true)
            recyclerView.addOnLayoutCompletedListener(
                object : DpadRecyclerView.OnLayoutCompletedListener {
                    override fun onLayoutCompleted(state: RecyclerView.State) {
                        layoutCompleted++
                    }
                })
        }

        // when
        selectLastPosition(smooth = true)
        repeat(layoutRequests) {
            onRecyclerView("Request layout") { recyclerView ->
                recyclerView.requestLayout()
            }
        }
        waitForIdleScrollState()

        // then
        assertThat(layoutCompleted).isEqualTo(layoutRequests)
    }

}

