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
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertSelectedPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertViewHolderSelected
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testfixtures.DpadSelectionEvent
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
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
        assertFocusPosition(position = RecyclerView.NO_POSITION)

        assertThat(getSelectionEvents()).isEmpty()
        assertThat(getSelectionAndAlignedEvents()).isEmpty()

        recreateFragment()

        assertSelectedPosition(position = RecyclerView.NO_POSITION)
        assertFocusPosition(position = RecyclerView.NO_POSITION)

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

        assertFocusAndSelection(position = RecyclerView.NO_POSITION)

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

        repeat(10) { index ->
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

}
