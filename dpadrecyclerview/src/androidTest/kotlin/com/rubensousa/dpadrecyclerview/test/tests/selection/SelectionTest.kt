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

package com.rubensousa.dpadrecyclerview.test.tests.selection

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.assertions.ViewHolderAlignmentCountAssertion
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertIsFocused
import com.rubensousa.dpadrecyclerview.test.helpers.assertSelectedPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertViewHolderSelected
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.helpers.waitForLayout
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testfixtures.DpadDeselectionEvent
import com.rubensousa.dpadrecyclerview.testfixtures.DpadSelectionEvent
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class SelectionTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    private val defaultConfig = TestLayoutConfiguration(
        spans = 1,
        orientation = RecyclerView.VERTICAL,
        parentAlignment = ParentAlignment(
            edge = Edge.MIN_MAX
        ),
        childAlignment = ChildAlignment(offset = 0)
    )

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return defaultConfig
    }

    @Test
    fun testNoSelectionEventsAreDispatchedForEmptyAdapter() {
        launchFragment(TestAdapterConfiguration(numberOfItems = 0))

        assertSelectedPosition(position = RecyclerView.NO_POSITION)
        assertIsFocused()

        assertThat(getSelectionEvents()).isEmpty()
        assertThat(getSelectionAndAlignedEvents()).isEmpty()

        recreateFragment()

        assertSelectedPosition(position = RecyclerView.NO_POSITION)
        assertIsFocused()

        assertThat(getSelectionEvents()).isEmpty()
        assertThat(getSelectionAndAlignedEvents()).isEmpty()
    }

    @Test
    fun testNoPositionIsDispatchedWhenThereIsNoSelectedPosition() {
        launchFragment(TestAdapterConfiguration(numberOfItems = 1))

        assertFocusAndSelection(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )

        executeOnFragment { fragment ->
            fragment.clearEvents()
            fragment.clearAdapter()
        }

        waitForCondition("Waiting for 0 children") { recyclerView ->
            recyclerView.childCount == 0
        }
        assertSelectedPosition(RecyclerView.NO_POSITION)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = RecyclerView.NO_POSITION))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = RecyclerView.NO_POSITION))
        )
    }

    @Test
    fun testSelectionEventAreDispatchedForInitialState() {
        launchFragment()

        assertSelectedPosition(position = 0)
        assertFocusPosition(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
    }

    @Test
    fun testSelectionEventsAreDispatchedAfterRecreation() {
        launchFragment()

        assertFocusAndSelection(position = 0)

        recreateFragment()

        assertFocusAndSelection(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
    }

    @Test
    fun testViewHoldersReceiveSelectionChanges() {
        launchFragment()

        assertSelectedPosition(position = 0)
        assertViewHolderSelected(position = 0, isSelected = true)

        repeat(5) { index ->
            KeyEvents.pressDown()
            assertViewHolderSelected(position = index, isSelected = false)
            assertSelectedPosition(position = index + 1)
            assertViewHolderSelected(position = index + 1, isSelected = true)
        }

    }

    @Test
    fun testViewHoldersAlreadyAlignedStillDispatchAlignedEvent() {
        launchFragment(getDefaultLayoutConfiguration().copy(spans = 5))

        val expectedEvents = ArrayList<DpadSelectionEvent>()
        expectedEvents.add(DpadSelectionEvent(position = 0))

        repeat(4) { iteration ->
            KeyEvents.pressRight()
            assertFocusAndSelection(position = iteration + 1)
            expectedEvents.add(DpadSelectionEvent(position = iteration + 1))
        }

        assertThat(getSelectionEvents()).isEqualTo(expectedEvents)
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(expectedEvents)
    }

    @Test
    fun testTaskIsExecutedAfterViewHolderIsSelected() {
        launchFragment()

        selectWithTask(position = 1, smooth = true, executeWhenAligned = false)

        assertThat(getTasksExecuted()).isEqualTo(listOf(DpadSelectionEvent(position = 1)))
        assertSelectedPosition(position = 1)
        assertFocusPosition(position = 1)
    }

    @Test
    fun testTaskIsExecutedAfterViewHolderIsSelectedAndAligned() {
        launchFragment()
        val targetPosition = 5

        selectWithTask(position = targetPosition, smooth = true, executeWhenAligned = true)

        assertSelectedPosition(position = targetPosition)

        waitForIdleScrollState()

        assertThat(getTasksExecuted()).isEqualTo(listOf(DpadSelectionEvent(position = targetPosition)))
        assertFocusPosition(position = targetPosition)
    }

    @Test
    fun testViewHolderReceivesDeselectionWhenItIsRecycled() {
        launchFragment()

        val viewHolderSelections = getViewHolderSelections()
        assertThat(viewHolderSelections).isEqualTo(listOf(0))

        executeOnFragment { fragment -> fragment.clearAdapter() }

        val viewHolderDeselections = getViewHolderDeselections()
        assertThat(viewHolderDeselections).isEqualTo(listOf(0))
    }

    @Test
    fun testViewHolderReceivesAlignmentCallback() {
        launchFragment()

        Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
            .check(
                ViewHolderAlignmentCountAssertion(
                    count = 1,
                    position = 0
                )
            )

        KeyEvents.pressDown()
        KeyEvents.pressUp()

        Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
            .check(
                ViewHolderAlignmentCountAssertion(
                    count = 2,
                    position = 0
                )
            )
    }

    @Test
    fun testViewHolderReceivesAlignmentCallbackAfterSmoothScrolling() = report {
        Given("Launch Fragment") {
            launchFragment()
        }

        When("Smooth scroll to position 1") {
            selectPosition(5, smooth = true)
            waitForIdleScrollState()
        }

        Then("ViewHolder at position 1 received alignment callback") {
            Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
                .check(
                    ViewHolderAlignmentCountAssertion(
                        count = 1,
                        position = 5
                    )
                )
        }
    }

    @Test
    fun testSelectingPositionOutOfBoundsDoesNotCrash() {
        val numberOfItems = 500
        launchFragment(TestAdapterConfiguration(numberOfItems = numberOfItems))

        selectPosition(numberOfItems + 100, smooth = true, waitForIdle = false)

        Thread.sleep(1000L)

        mutateAdapter { adapter ->
            adapter.setList(MutableList(numberOfItems + 10) { it })
            adapter.notifyItemRangeInserted(0, 100)
        }

        onRecyclerView("Request layout") { recyclerView ->
            recyclerView.requestLayout()
        }

        assertFocusAndSelection(numberOfItems + 9)
    }

    @Test
    fun testDeselectionEventIsSent() {
        // given
        launchFragment()

        // when
        KeyEvents.pressDown()
        waitForIdleScrollState()

        // then
        var receivedEvents: List<DpadDeselectionEvent> = emptyList()
        executeOnFragment { fragment ->
            receivedEvents = fragment.getDeselectionEvents()
        }
        assertThat(receivedEvents.size).isEqualTo(1)
        assertThat(receivedEvents.first().viewHolder.layoutPosition).isEqualTo(0)
    }

    @Test
    fun testSelectionIsPostponedUntilLayoutExists() = report {
        val position = 4
        Given("Setup clean layout") {
            step("Launch fragment") {
                launchFragment()
            }
            step("Clear layout") {
                mutateAdapter { adapter ->
                    adapter.setList(mutableListOf())
                    adapter.notifyDataSetChanged()
                }
            }
            step("Wait for layout completed") {
                waitForLayout()
            }
        }
        When("Set async adapter content, followed by position update") {
            var currentRecyclerView: DpadRecyclerView? = null
            onRecyclerView("Get recyclerView") { recyclerView ->
                currentRecyclerView = recyclerView
            }
            mutateAdapter { adapter ->
                adapter.addAll(listOf(0, 1, 2, 3, 4, 5))
                currentRecyclerView?.setSelectedPosition(position)
            }
        }

        Then("Assert position $position is selected") {
            assertSelectedPosition(position)
        }
    }

    @Test
    fun testSelectionListenerThatRemovesItself() = report {
        val listeners = 5
        var events = 0
        Given("Attach listeners") {
            launchFragment()
            onRecyclerView("Get recyclerView") { recyclerView ->
                repeat(listeners) {
                    recyclerView.addOnViewHolderSelectedListener(object :
                        OnViewHolderSelectedListener {
                        override fun onViewHolderSelected(
                            parent: DpadRecyclerView,
                            child: RecyclerView.ViewHolder?,
                            position: Int,
                            subPosition: Int
                        ) {
                            super.onViewHolderSelected(parent, child, position, subPosition)
                            recyclerView.removeOnViewHolderSelectedListener(this)
                            events++
                        }
                    })
                }
            }
        }
        When("Select another position") {
            selectPosition(10)
            waitForLayout()
        }

        Then("Events are received") {
            assertThat(events).isEqualTo(listeners)
        }
    }

}
