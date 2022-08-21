package com.rubensousa.dpadrecyclerview.test.tests

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SingleSpanVerticalTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

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
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 5)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testFocusStaysAtBottomEdgePosition() {
        val lastPosition = selectLastPosition(smooth = false)
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        assertFocusPosition(position = lastPosition)
    }

    @Test
    fun testContinuousScrollDown() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 100)
        assertFocusPosition(position = 100)
    }

    @Test
    fun testContinuousScrollUp() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 100)
        assertFocusPosition(position = 100)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 100)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testScrollWithShortBreaks() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        assertFocusPosition(position = 50)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        assertFocusPosition(position = 100)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        assertFocusPosition(position = 50)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testMultiStepScroll() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 50)
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 50)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testContinuousScrollThatSettlesInSamePosition() {
        repeat(5) {
            UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_DOWN, times = 10)
            UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_UP, times = 10)
        }
        assertFocusPosition(position = 0)
    }

}
