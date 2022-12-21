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
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertItemAtPosition
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRelativeItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForAnimation
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.test.tests.layout.LayoutColumn
import com.rubensousa.dpadrecyclerview.testing.DpadSelectionEvent
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import org.junit.Before
import org.junit.Test

class VerticalMutationTest : DpadRecyclerViewTest() {

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offsetRatio = 0.0f
            ),
            childAlignment = ChildAlignment(
                offsetRatio = 0.0f
            )
        )
    }

    private lateinit var column: LayoutColumn
    private var itemWidth: Int = 0
    private var itemHeight: Int = 0

    @Before
    fun setup() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        val itemViewBounds = getRelativeItemViewBounds(position = 0)
        itemWidth = itemViewBounds.width()
        itemHeight = itemViewBounds.height()
        column = LayoutColumn(
            width = recyclerViewBounds.width(),
            height = recyclerViewBounds.height()
        )
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
        waitForAnimation()
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
        waitForAnimation()
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
        waitForAnimation()
        waitForIdleScrollState()
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
        waitForAnimation()
        assertFocusAndSelection(1)
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
        val oldViewBounds = getRelativeItemViewBounds(position = 0)
        mutateAdapter { adapter ->
            adapter.move(from = 3, to = 4)
        }
        waitForAnimation()
        assertFocusAndSelection(0)
        assertItemAtPosition(position = 3, item = 4)
        assertItemAtPosition(position = 4, item = 3)

        val newViewBounds = getRelativeItemViewBounds(position = 0)
        assertThat(newViewBounds).isEqualTo(oldViewBounds)
    }

}
