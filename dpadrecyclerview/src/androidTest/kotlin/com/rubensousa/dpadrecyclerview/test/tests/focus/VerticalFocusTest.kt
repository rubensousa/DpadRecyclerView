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

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertSelectedPosition
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents.pressDown
import com.rubensousa.dpadrecyclerview.testing.KeyEvents.pressUp
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class VerticalFocusTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX
            ),
            childAlignment = ChildAlignment(offset = 0)
        )
    }

    @Test
    fun testViewHoldersReceiveFocus() {
        launchFragment()

        repeat(5) { index ->
            assertFocusPosition(position = index)
            assertSelectedPosition(position = index)
            pressDown()
        }
    }

    @Test
    fun testFocusingBeforeLastPositionDoesNotChangeFocus() {
        launchFragment()

        pressUp()

        assertFocusPosition(position = 0)
        assertSelectedPosition(position = 0)
    }

    @Test
    fun testFocusingAfterLastPositionDoesNotChangeFocus() {
        launchFragment()

        val lastPosition = selectLastPosition()

        assertFocusPosition(position = lastPosition)
        assertSelectedPosition(position = lastPosition)

        pressDown()

        assertFocusPosition(position = lastPosition)
        assertSelectedPosition(position = lastPosition)
    }

    @Test
    fun testViewHoldersNotFocusableDoNotReceiveFocus() {
        val increment = 2
        launchFragment(getDefaultAdapterConfiguration().copy(focusEvery = increment))

        repeat(5) { index ->
            assertFocusPosition(position = increment * index)
            assertSelectedPosition(position = increment * index)
            pressDown()
        }
    }

    @Test
    fun testScrollingUntilPivotIsFound() {
        val increment = 15
        val steps = 10
        launchFragment(
            getDefaultAdapterConfiguration().copy(
                numberOfItems = increment * steps,
                focusEvery = increment
            )
        )

        repeat(5) { index ->
            assertFocusPosition(position = increment * index)
            assertSelectedPosition(position = increment * index)
            pressDown()
            waitForIdleScrollState()
        }

    }

    @Test
    fun testScrollingBackToPreviousSelectionWhenNextPivotIsNotFound() {
        val increment = 15
        val steps = 10
        launchFragment(
            getDefaultAdapterConfiguration().copy(
                numberOfItems = increment * steps,
                focusEvery = increment
            )
        )

        val lastFocusablePosition = increment * (steps - 1)

        selectPosition(lastFocusablePosition, 0)

        pressDown()

        waitForIdleScrollState()

        assertFocusAndSelection(position = lastFocusablePosition)
    }

}
