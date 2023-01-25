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

package com.rubensousa.dpadrecyclerview.test.tests.scrolling

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class VerticalSingleColumnScrollTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX
            ),
            childAlignment = ChildAlignment(offset = 0)
        )
    }

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testFocusStaysAtTopEdgePosition() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 5)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testFocusStaysAtBottomEdgePosition() {
        val lastPosition = selectLastPosition(smooth = false)
        assertThat(lastPosition).isEqualTo(DEFAULT_ITEM_COUNT - 1)
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        waitForIdleScrollState()
        assertFocusPosition(position = lastPosition)
    }

    @Test
    fun testContinuousScrollDown() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 100)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)
    }

    @Test
    fun testScrollToLastItem() {
        val distanceToEnd = 20
        selectPosition(position = DEFAULT_ITEM_COUNT - 1 - distanceToEnd, subPosition = 0)
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = distanceToEnd)
        waitForIdleScrollState()
        assertFocusPosition(position = DEFAULT_ITEM_COUNT - 1)
    }

    @Test
    fun testContinuousScrollUp() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 25)
        waitForIdleScrollState()
        assertFocusPosition(position = 25)
    }

    @Test
    fun testScrollWithShortBreaks() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 10)
        waitForIdleScrollState()
        assertFocusPosition(position = 10)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 10)
        waitForIdleScrollState()
        assertFocusPosition(position = 20)
    }

    @Test
    fun testContinuousScrollThatSettlesInSamePosition() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 5)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

}
