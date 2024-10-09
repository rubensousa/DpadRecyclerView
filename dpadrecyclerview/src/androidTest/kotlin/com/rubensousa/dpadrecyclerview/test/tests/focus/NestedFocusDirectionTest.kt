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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.test.TestAdapter
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.AbstractTestAdapter
import com.rubensousa.dpadrecyclerview.testfixtures.DefaultInstrumentedReportRule
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NestedFocusDirectionTest {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    @get:Rule
    val report = DefaultInstrumentedReportRule()

    private lateinit var fragmentScenario: FragmentScenario<NestedFocusDirectionFragment>

    @Before
    fun setup() {
        fragmentScenario = launchFragment()
    }

    @Test
    fun testNestedRecyclerViewIsNotSkippedWhenScrollingDownFromAnEdge() = report {
        var lastPosition = 0
        step("Scroll to second list") {
            KeyEvents.pressDown()
            waitForIdleScrollState()
        }
        step("Scroll second list to the end") {
            Espresso.onView(
                allOf(
                    withId(
                        com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
                    ),
                    withTagValue(Matchers.`is`(1))
                )
            ).perform(DpadRecyclerViewActions.selectLastPosition(smooth = false) {
                lastPosition = it
            })
        }

        step("Scroll back up to the first list") {
            KeyEvents.pressUp()
            waitForIdleScrollState()
        }

        step("Scroll down") {
            KeyEvents.pressDown()
            waitForIdleScrollState()
        }

        step("Assert that second list is focused") {
            Espresso.onView(
                allOf(
                    withId(
                        com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
                    ),
                    withTagValue(Matchers.`is`(1))
                )
            ).check(
                DpadRecyclerViewAssertions.isFocused(
                    position = lastPosition
                )
            )
        }
    }

    @Test
    fun testFocusStillLeavesRecyclerViewWhenFocusSearchIsDisabled() = report {
        step("Disable focus searches for the first recyclerview") {
            Espresso.onView(
                allOf(
                    withId(
                        com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
                    ),
                    withTagValue(Matchers.`is`(0))
                )
            ).perform(DpadRecyclerViewActions.execute("Disable focus searches") { recyclerView ->
                recyclerView.setFocusSearchDisabled(true)
            })
        }

        step("Press down") {
            KeyEvents.pressDown()
            waitForIdleScrollState()
        }

        step("Focus should be in second list") {
            Espresso.onView(
                allOf(
                    withId(
                        com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
                    ),
                    withTagValue(Matchers.`is`(1))
                )
            ).check(
                DpadRecyclerViewAssertions.isFocused(position = 0)
            )
        }
    }

    private fun launchFragment(): FragmentScenario<NestedFocusDirectionFragment> {
        return launchFragmentInContainer<NestedFocusDirectionFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
            waitForCondition("Waiting for layout pass") { recyclerView ->
                !recyclerView.isLayoutRequested
            }
        }
    }

    class NestedFocusDirectionFragment : Fragment(
        com.rubensousa.dpadrecyclerview.test.R.layout.dpadrecyclerview_test_container
    ) {

        private val adapter = FocusDirectionTestAdapter()

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val recyclerView =
                view.findViewById<DpadRecyclerView>(com.rubensousa.dpadrecyclerview.test.R.id.recyclerView)
            recyclerView.adapter = adapter
            recyclerView.requestFocus()
        }

    }

    class FocusDirectionTestAdapter(
    ) : AbstractTestAdapter<NestedListViewHolder>(
        numberOfItems = 20
    ) {

        private val smallConfiguration = TestAdapterConfiguration(
            itemLayoutId = com.rubensousa.dpadrecyclerview.test.R.layout.dpadrecyclerview_item_horizontal_small,
            numberOfItems = 40
        )

        private val mediumConfiguration = TestAdapterConfiguration(
            itemLayoutId = com.rubensousa.dpadrecyclerview.test.R.layout.dpadrecyclerview_item_horizontal,
            numberOfItems = 40
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedListViewHolder {
            return NestedListViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(
                        com.rubensousa.dpadrecyclerview.test.R.layout.dpadrecyclerview_nested_list,
                        parent,
                        false
                    ),
            )
        }

        override fun onBindViewHolder(holder: NestedListViewHolder, position: Int) {
            holder.bind(
                configuration = if (position % 2 == 0) {
                    mediumConfiguration
                } else {
                    smallConfiguration
                },
                position = position
            )
        }

        override fun onViewRecycled(holder: NestedListViewHolder) {
            holder.recycle()
        }
    }

    class NestedListViewHolder(
        val view: View,
    ) : RecyclerView.ViewHolder(view) {

        private val textView =
            view.findViewById<TextView>(com.rubensousa.dpadrecyclerview.test.R.id.textView)

        val recyclerView =
            view.findViewById<DpadRecyclerView>(com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView)

        fun bind(configuration: TestAdapterConfiguration, position: Int) {
            recyclerView.adapter = TestAdapter(
                adapterConfiguration = configuration,
                onViewHolderSelected = { position -> },
                onViewHolderDeselected = { position -> }
            )
            recyclerView.tag = position
            textView.text = "List $position"
            textView.freezesText = true
        }

        fun recycle() {
            recyclerView.adapter = null
        }
    }

}
