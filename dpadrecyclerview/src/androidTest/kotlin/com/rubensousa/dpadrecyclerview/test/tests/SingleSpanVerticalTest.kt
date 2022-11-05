package com.rubensousa.dpadrecyclerview.test.tests

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SingleSpanVerticalTest : GridTest() {

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
    fun testContinuousScrollUp() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 100)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 100)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testScrollWithShortBreaks() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testMultiStepScroll() {
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testContinuousScrollThatSettlesInSamePosition() {
        repeat(5) {
            KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 10)
            KeyEvents.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 10)
        }
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

}
