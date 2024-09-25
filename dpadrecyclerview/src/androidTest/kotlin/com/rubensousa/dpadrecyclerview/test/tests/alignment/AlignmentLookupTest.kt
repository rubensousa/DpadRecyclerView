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

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.AlignmentLookup
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.helpers.waitForLayout
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class AlignmentLookupTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX,
                offset = 0,
                fraction = 0f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                fraction = 0f
            )
        )
    }

    @Test
    fun testItemRespectsParentAlignmentLookup() {
        // given
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()

        // when
        onRecyclerView("Set alignment") { recyclerView ->
            recyclerView.setAlignmentLookup(object : AlignmentLookup {
                override fun getParentAlignment(
                    viewHolder: RecyclerView.ViewHolder,
                ): ParentAlignment? {
                    if (viewHolder.layoutPosition == 0) {
                        return ParentAlignment(offset = 0, fraction = 0.5f)
                    }
                    return null
                }
            })
        }

        // then
        waitForLayout()
        val viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.top).isEqualTo(recyclerViewBounds.height() / 2)
    }

    @Test
    fun testItemRespectsChildAlignmentLookup() {
        // given
        launchFragment()

        // when
        onRecyclerView("Set alignment") { recyclerView ->
            recyclerView.setAlignmentLookup(object : AlignmentLookup {
                override fun getChildAlignment(viewHolder: RecyclerView.ViewHolder): ChildAlignment? {
                    if (viewHolder.layoutPosition == 0) {
                        return ChildAlignment(offset = 0, fraction = 0.5f)
                    }
                    return null
                }
            })
        }

        // then
        waitForLayout()
        val viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.top).isEqualTo(-viewBounds.height() / 2)
    }

    @Test
    fun testScrollingAlignsToLookup() {
        // given
        launchFragment()
        val centerParentAlignment = ParentAlignment(fraction = 0.5f)
        val centerChildAlignment = ChildAlignment(fraction = 0.5f)
        val recyclerViewBounds = getRecyclerViewBounds()

        // when
        onRecyclerView("Set alignment") { recyclerView ->
            recyclerView.setAlignmentLookup(object : AlignmentLookup {
                override fun getParentAlignment(
                    viewHolder: RecyclerView.ViewHolder,
                ): ParentAlignment? {
                    if (viewHolder.layoutPosition % 2 == 0) {
                        return centerParentAlignment
                    }
                    return null
                }

                override fun getChildAlignment(viewHolder: RecyclerView.ViewHolder): ChildAlignment? {
                    if (viewHolder.layoutPosition % 2 == 0) {
                        return centerChildAlignment
                    }
                    return null
                }
            })
        }
        waitForLayout()

        repeat(10) { index ->
            val viewBounds = getItemViewBounds(position = index)
            // then
            if (index % 2 == 0) {
                assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.height() / 2)
            } else {
                assertThat(viewBounds.top).isEqualTo(0)
            }
            KeyEvents.pressDown()
            waitForIdleScrollState()
        }
    }

    @Test
    fun testScrollIsStillAppliedAfterFastScrolling() {
        // given
        launchFragment()
        val bottomParentAlignment = ParentAlignment(fraction = 1f)
        val bottomChildAlignment = ChildAlignment(fraction = 1f)
        val recyclerViewBounds = getRecyclerViewBounds()

        onRecyclerView("Set alignment") { recyclerView ->
            recyclerView.setAlignmentLookup(object : AlignmentLookup {
                override fun getParentAlignment(
                    viewHolder: RecyclerView.ViewHolder,
                ): ParentAlignment? {
                    if (viewHolder.layoutPosition == 0) {
                        return bottomParentAlignment
                    }
                    return null
                }

                override fun getChildAlignment(viewHolder: RecyclerView.ViewHolder): ChildAlignment? {
                    if (viewHolder.layoutPosition == 0) {
                        return bottomChildAlignment
                    }
                    return null
                }
            })
        }
        waitForLayout()

        // when
        KeyEvents.pressDown(times = 10)
        waitForIdleScrollState()
        KeyEvents.pressUp(times = 10)
        waitForIdleScrollState()

        // then
        val viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)
    }

    @Test
    fun testAlignmentLookupSmoothScrolling() = report {
        Given("Launch Fragment with top alignment") {
            launchFragment()
        }

        var scrolled = false
        When("Set AlignmentLookup for 50% of screen height") {
            onRecyclerView("Set alignment") { recyclerView ->
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState != RecyclerView.SCROLL_STATE_IDLE) {
                            scrolled = true
                        }
                    }
                })
                recyclerView.setAlignmentLookup(
                    object : AlignmentLookup {
                        override fun getParentAlignment(
                            viewHolder: RecyclerView.ViewHolder,
                        ): ParentAlignment {
                            return ParentAlignment(fraction = 0.5f)
                        }

                        override fun getChildAlignment(
                            viewHolder: RecyclerView.ViewHolder,
                        ): ChildAlignment {
                            return ChildAlignment(fraction = 0.5f)
                        }
                    },
                    smooth = true
                )
            }
        }

        Then("RecyclerView scrolled to new position") {
            waitForIdleScrollState()
            val recyclerViewBounds = getRecyclerViewBounds()
            val viewBounds = getItemViewBounds(position = 0)
            assertThat(scrolled).isTrue()
            assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.height() / 2)
        }
    }

}
