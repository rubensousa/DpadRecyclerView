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

import androidx.core.view.get
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.test.TestNestedListFragment
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.testfixtures.DpadFocusEvent
import com.rubensousa.dpadrecyclerview.testfixtures.recording.ScreenRecorderRule
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.hamcrest.Matchers
import org.hamcrest.core.AllOf.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NestedFocusListenerTest {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    @get:Rule(order = Int.MIN_VALUE)
    val recordingRule = ScreenRecorderRule()

    private lateinit var fragmentScenario: FragmentScenario<TestNestedListFragment>

    @Before
    fun setup() {
        fragmentScenario = launchFragment()
    }

    @Test
    fun testChildRecyclerViewReceivesInitialFocusEvent() {
        val events = getChildFocusEvents()
        assertThat(events).hasSize(1)

        val event = events.first()
        onRecyclerView("Assert child event properties") { parentRecyclerView ->
            val firstViewHolderView = parentRecyclerView[0]
            val childRecyclerView = firstViewHolderView.findViewById<DpadRecyclerView>(
                com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
            )
            val firstChildViewHolder = childRecyclerView.findViewHolderForLayoutPosition(0)!!
            assertThat(event.parent).isEqualTo(firstChildViewHolder)
            assertThat(event.child).isEqualTo(firstChildViewHolder.itemView)
        }
    }

    @Test
    fun testChildRecyclerViewReceivesNextFocus() {
        // when
        KeyEvents.pressDown()
        waitForIdleScrollState()

        // then
        val events = getChildFocusEvents()
        assertThat(events).hasSize(2)

        val lastEvent = events.last()
        onRecyclerView("Assert child event properties") { parentRecyclerView ->
            val viewHolderView = parentRecyclerView[1]
            val childRecyclerView = viewHolderView.findViewById<DpadRecyclerView>(
                com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
            )
            val firstChildViewHolder = childRecyclerView.findViewHolderForLayoutPosition(0)!!
            assertThat(lastEvent.parent).isEqualTo(firstChildViewHolder)
            assertThat(lastEvent.child).isEqualTo(firstChildViewHolder.itemView)
        }
    }

    @Test
    fun testParentRecyclerViewReceivesChildFocusEvent() {
        val events = getParentFocusEvents()
        assertThat(events).hasSize(1)

        val event = events.first()
        onRecyclerView("Assert parent event properties") { parentRecyclerView ->
            val firstParentViewHolder = parentRecyclerView.findViewHolderForLayoutPosition(0)!!
            val childRecyclerView = firstParentViewHolder.itemView.findViewById<DpadRecyclerView>(
                com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
            )
            val firstChildViewHolder = childRecyclerView.findViewHolderForLayoutPosition(0)!!
            assertThat(event.parent).isEqualTo(firstParentViewHolder)
            assertThat(event.child).isEqualTo(firstChildViewHolder.itemView)
        }
    }

    @Test
    fun testParentRecyclerViewReceivesNextChildFocusEvent() {
        // when
        KeyEvents.pressDown()
        waitForIdleScrollState()

        // then
        val events = getParentFocusEvents()
        assertThat(events).hasSize(2)

        val event = events.last()
        onRecyclerView("Assert parent event properties") { parentRecyclerView ->
            val parentViewHolder = parentRecyclerView.findViewHolderForLayoutPosition(1)!!
            val childRecyclerView = parentViewHolder.itemView.findViewById<DpadRecyclerView>(
                com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView
            )
            val firstChildViewHolder = childRecyclerView.findViewHolderForLayoutPosition(0)!!
            assertThat(event.parent).isEqualTo(parentViewHolder)
            assertThat(event.child).isEqualTo(firstChildViewHolder.itemView)
        }
    }

    @Test
    fun testParentRecyclerViewStillReceivesFocusIfChildHasNoListener() {
        // given
        val focusEvents = 6
        Espresso.onView(
            allOf(
                withId(com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView),
                withTagValue(Matchers.`is`(0))
            )
        ).perform(DpadRecyclerViewActions.execute("Clear listener") { recyclerView ->
            recyclerView.clearOnViewFocusedListeners()
        })

        // when
        KeyEvents.pressRight(times = focusEvents - 1)
        waitForIdleScrollState()

        // then
        assertThat(getParentFocusEvents()).hasSize(focusEvents)
    }

    private fun getChildFocusEvents(): List<DpadFocusEvent> {
        var events = listOf<DpadFocusEvent>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getChildFocusEvents()
        }
        return events
    }

    private fun getParentFocusEvents(): List<DpadFocusEvent> {
        var events = listOf<DpadFocusEvent>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getParentFocusEvents()
        }
        return events
    }

    private fun launchFragment(): FragmentScenario<TestNestedListFragment> {
        return launchFragmentInContainer<TestNestedListFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
            waitForCondition("Waiting for layout pass") { recyclerView ->
                !recyclerView.isLayoutRequested
            }
        }
    }

}
