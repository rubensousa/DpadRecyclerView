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

package com.rubensousa.dpadrecyclerview.test.tests.mutation

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertItemAtPosition
import com.rubensousa.dpadrecyclerview.test.helpers.getRelativeItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.AbstractTestAdapter
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testfixtures.DpadSelectionEvent
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class AdapterMutationTest : DpadRecyclerViewTest() {

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

    private var itemWidth: Int = 0
    private var itemHeight: Int = 0

    @Before
    fun setup() {
        launchFragment()
        val itemViewBounds = getRelativeItemViewBounds(position = 0)
        itemWidth = itemViewBounds.width()
        itemHeight = itemViewBounds.height()
    }

    @Test
    fun testRemovalOfPivotMovesFocusToNextPosition() {
        val oldViewBounds = getRelativeItemViewBounds(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )

        mutateAdapter { adapter ->
            adapter.removeAt(0)
        }
        assertFocusAndSelection(0)
        assertItemAtPosition(position = 0, item = 1)

        val newViewBounds = getRelativeItemViewBounds(position = 0)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0), DpadSelectionEvent(position = 0))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0), DpadSelectionEvent(position = 0))
        )
    }

    @Test
    fun testRemovalOfNonPivotViewDoesNotChangeFocus() {
        val oldViewBounds = getRelativeItemViewBounds(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )

        mutateAdapter { adapter ->
            adapter.removeAt(1)
        }
        assertFocusAndSelection(0)
        assertItemAtPosition(position = 0, item = 0)

        val newViewBounds = getRelativeItemViewBounds(position = 0)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = 0))
        )
    }

    @Test
    fun testRemovalOfPivotAsLastViewShouldSelectPreviousView() {
        val lastPosition = selectLastPosition()

        assertFocusAndSelection(lastPosition)

        executeOnFragment { fragment ->
            fragment.clearEvents()
        }

        mutateAdapter { adapter ->
            adapter.removeAt(lastPosition)
        }
        assertFocusAndSelection(lastPosition - 1)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = lastPosition - 1))
        )

        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(DpadSelectionEvent(position = lastPosition - 1))
        )
    }

    @Test
    fun testMovePivotWillKeepItsAlignmentAndFocus() {
        val oldViewBounds = getRelativeItemViewBounds(position = 0)
        mutateAdapter { adapter ->
            adapter.move(from = 0, to = 1)
        }
        assertItemAtPosition(position = 1, item = 0)

        val newViewBounds = getRelativeItemViewBounds(position = 1)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)

        // Scroll up to check that the other item was moved
        KeyEvents.pressUp()
        waitForIdleScrollState()
        assertItemAtPosition(position = 0, item = 1)
    }

    @Test
    fun testMovingNonFocusedViewDoesNotUpdatePivot() {
        val pivotPosition = 5
        KeyEvents.pressDown(times = pivotPosition)
        val oldViewBounds = getRelativeItemViewBounds(position = pivotPosition)
        mutateAdapter { adapter ->
            adapter.move(from = 3, to = 4)
        }
        assertFocusAndSelection(pivotPosition)
        assertItemAtPosition(position = 3, item = 4)
        assertItemAtPosition(position = 4, item = 3)

        val newViewBounds = getRelativeItemViewBounds(position = pivotPosition)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)
    }

    @Test
    fun testInsertingViewBeforePivot() {
        val originalPivotPosition = 5
        var currentPivotPosition = originalPivotPosition
        KeyEvents.pressDown(times = originalPivotPosition)
        val oldViewBounds = getRelativeItemViewBounds(position = originalPivotPosition)

        mutateAdapter { adapter ->
            adapter.addAt(item = -1, index = currentPivotPosition - 1)
        }
        currentPivotPosition++
        assertFocusAndSelection(currentPivotPosition)

        assertItemAtPosition(position = currentPivotPosition, item = 5)
        assertItemAtPosition(position = currentPivotPosition - 1, item = 4)
        assertItemAtPosition(position = currentPivotPosition - 2, item = -1)

        val newViewBounds = getRelativeItemViewBounds(position = currentPivotPosition)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)
    }

    @Test
    fun testInsertingViewAtPivotPosition() {
        val originalPivotPosition = 5
        var currentPivotPosition = originalPivotPosition
        KeyEvents.pressDown(times = originalPivotPosition)
        val oldViewBounds = getRelativeItemViewBounds(position = originalPivotPosition)

        mutateAdapter { adapter ->
            adapter.addAt(item = -1, index = currentPivotPosition)
        }
        currentPivotPosition++
        assertFocusAndSelection(currentPivotPosition)

        assertItemAtPosition(position = currentPivotPosition, item = 5)
        assertItemAtPosition(position = currentPivotPosition - 1, item = -1)

        val newViewBounds = getRelativeItemViewBounds(position = currentPivotPosition)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)
    }

    @Test
    fun testInsertingViewAfterPivot() {
        val originalPivotPosition = 5
        KeyEvents.pressDown(times = originalPivotPosition)
        val oldViewBounds = getRelativeItemViewBounds(position = originalPivotPosition)

        mutateAdapter { adapter ->
            adapter.addAt(item = -1, index = originalPivotPosition + 1)
        }
        assertFocusAndSelection(originalPivotPosition)

        assertItemAtPosition(position = originalPivotPosition, item = 5)
        assertItemAtPosition(position = originalPivotPosition + 1, item = -1)

        val newViewBounds = getRelativeItemViewBounds(position = originalPivotPosition)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)
    }

    @Test
    fun testQuickUpdatesDoNotCrash() {
        val totalItems = 25
        val random = Random.Default
        val newItems = mutableSetOf<Int>()
        lateinit var testAdapter: AbstractTestAdapter<*>
        mutateAdapter { adapter ->
            testAdapter = adapter
        }
        repeat(50) {
            val newListSize = random.nextInt(totalItems)
            repeat(newListSize) {
                val item = random.nextInt(totalItems - 1)
                newItems.add(item)
            }
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                testAdapter.submitList(newItems.toMutableList())
            }
            newItems.clear()
            Thread.sleep(250L)
        }
        Espresso.onIdle()

    }
}
