package com.rubensousa.dpadrecyclerview.test.tests

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.KeyPresser
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.test.rules.DisableIdleTimeoutRule
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
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 5)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testFocusStaysAtBottomEdgePosition() {
        val lastPosition = selectLastPosition(smooth = false)
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        waitForIdleScrollState()
        assertFocusPosition(position = lastPosition)
    }

    @Test
    fun testContinuousScrollDown() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 100)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)
    }

    @Test
    fun testContinuousScrollUp() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 100)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 100)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testScrollWithShortBreaks() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testMultiStepScroll() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testContinuousScrollThatSettlesInSamePosition() {
        repeat(5) {
            KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 10)
            KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 10)
        }
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

}
