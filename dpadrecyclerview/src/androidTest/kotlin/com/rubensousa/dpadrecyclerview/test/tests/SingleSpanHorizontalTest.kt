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

package com.rubensousa.dpadrecyclerview.test.tests

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerViewHelper
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SingleSpanHorizontalTest : GridTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX
            ),
            childAlignment = ChildAlignment(offset = 0)
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration()
            .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal)
    }

    @Before
    fun setup() {
        DpadRecyclerViewHelper.enableNewPivotLayoutManager(true)
        launchFragment()
    }

    @Test
    fun testFocusStaysAtLeftEdgePosition() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 5)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testFocusStaysAtRightEdgePosition() {
        val lastPosition = selectLastPosition(smooth = false)
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)
        waitForIdleScrollState()
        assertFocusPosition(position = lastPosition)
    }

    @Test
    fun testContinuousScrollRight() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)
    }

    @Test
    fun testContinuousScrollLeft() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testScrollWithShortBreaks() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testMultiStepScroll() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 25)
        waitForIdleScrollState()
        assertFocusPosition(position = 25)
    }

    @Test
    fun testContinuousScrollThatSettlesInSamePosition() {
        repeat(5) {
            KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 10)
            assertFocusPosition(position = 10)
            KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 10)
            assertFocusPosition(position = 0)
        }
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }
}