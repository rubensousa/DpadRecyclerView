package com.rubensousa.dpadrecyclerview.test.tests

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.KeyPresser
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.test.rules.DisableIdleTimeoutRule
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
            .copy(itemLayoutId = R.layout.test_item_horizontal)
    }

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testFocusStaysAtLeftEdgePosition() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 5)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testFocusStaysAtRightEdgePosition() {
        val lastPosition = selectLastPosition(smooth = false)
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)
        waitForIdleScrollState()
        assertFocusPosition(position = lastPosition)
    }

    @Test
    fun testContinuousScrollRight() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)
    }

    @Test
    fun testContinuousScrollLeft() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testScrollWithShortBreaks() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 100)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 50)

        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testMultiStepScroll() {
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }

    @Test
    fun testContinuousScrollThatSettlesInSamePosition() {
        repeat(5) {
            KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 10)
            KeyPresser.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 10)
        }
        waitForIdleScrollState()
        assertFocusPosition(position = 0)
    }
}