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
import com.rubensousa.dpadrecyclerview.OnChildLaidOutListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.runOnMainThread
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForLayout
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LayoutCompletionTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                fraction = 0.0f
            ),
            childAlignment = ChildAlignment(
                fraction = 0.0f
            )
        )
    }

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testLayoutCompleteListenerThatRemovesItself() = report {
        var currentRecyclerView: DpadRecyclerView? = null
        var events = 0
        val listeners = 3
        Given("Attach listener that removes itself") {
            onRecyclerView("Get recyclerview") { recyclerView ->
                currentRecyclerView = recyclerView
            }
            runOnMainThread {
                // Add listeners that remove themselves
                repeat(listeners) {
                    currentRecyclerView!!.addOnLayoutCompletedListener(
                        object : DpadRecyclerView.OnLayoutCompletedListener {
                            override fun onLayoutCompleted(state: RecyclerView.State) {
                                currentRecyclerView.removeOnLayoutCompletedListener(this)
                                events++
                            }
                        }
                    )
                }
            }
        }

        When("Request layout") {
            selectPosition(10)
            waitForLayout()
        }

        Then("Listener got removed") {
            assertThat(events).isEqualTo(listeners)
        }
    }

    @Test
    fun testChildLayoutListenerThatRemovesItself() = report {
        var currentRecyclerView: DpadRecyclerView? = null
        var events = 0
        val listeners = 3
        Given("Attach listener that removes itself") {
            onRecyclerView("Get recyclerview") { recyclerView ->
                currentRecyclerView = recyclerView
            }
            runOnMainThread {
                // Add listeners that remove themselves
                repeat(listeners) {
                    currentRecyclerView!!.addOnChildLaidOutListener(object : OnChildLaidOutListener {
                        override fun onChildLaidOut(
                            parent: RecyclerView,
                            child: RecyclerView.ViewHolder
                        ) {
                            currentRecyclerView.removeOnChildLaidOutListener(this)
                            events++
                        }
                    })
                }
            }
        }

        When("Request layout") {
            selectPosition(10)
            waitForLayout()
        }

        Then("Listener got removed") {
            assertThat(events).isEqualTo(listeners)
        }
    }

}