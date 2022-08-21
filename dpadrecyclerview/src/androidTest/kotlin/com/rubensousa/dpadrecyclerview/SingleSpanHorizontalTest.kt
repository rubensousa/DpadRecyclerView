package com.rubensousa.dpadrecyclerview

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.helpers.*
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SingleSpanHorizontalTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

    override fun getDefaultLayoutConfiguration(): TestGridFragment.LayoutConfiguration {
        return TestGridFragment.LayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX
            ),
            childAlignment = ChildAlignment(offset = 0)
        )
    }

    override fun getDefaultAdapterConfiguration(): TestGridFragment.AdapterConfiguration {
        return super.getDefaultAdapterConfiguration()
            .copy(itemLayoutId = R.layout.test_item_horizontal)
    }

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testFocusStaysAtLeftEdgePosition() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 5)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testFocusStaysAtRightEdgePosition() {
        val lastPosition = selectLastPosition(smooth = false)
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)
        assertFocusPosition(position = lastPosition)
    }

    @Test
    fun testContinuousScrollRight() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 100)
        assertFocusPosition(position = 100)
    }

    @Test
    fun testContinuousScrollLeft() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 100)
        assertFocusPosition(position = 100)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 100)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testScrollWithShortBreaks() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        assertFocusPosition(position = 50)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        assertFocusPosition(position = 100)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        assertFocusPosition(position = 50)

        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testMultiStepScroll() {
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 50)
        UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 50)
        assertFocusPosition(position = 0)
    }

    @Test
    fun testContinuousScrollThatSettlesInSamePosition() {
        repeat(5) {
            UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_RIGHT, times = 10)
            UiAutomatorHelper.pressKey(key = KeyEvent.KEYCODE_DPAD_LEFT, times = 10)
        }
        assertFocusPosition(position = 0)
    }
}