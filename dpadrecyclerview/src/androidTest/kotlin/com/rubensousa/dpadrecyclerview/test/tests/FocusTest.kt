package com.rubensousa.dpadrecyclerview.test.tests

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.FastUiAutomatorRule
import com.rubensousa.dpadrecyclerview.test.helpers.UiAutomatorHelper
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertSelectedPosition
import org.junit.Rule
import org.junit.Test

class FocusTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

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
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Test
    fun testViewHoldersNotFocusableDoNotReceiveFocus() {
        launchFragment(getDefaultAdapterConfiguration().copy(alternateFocus = true))

        repeat(5) { index ->
            assertFocusPosition(position = 2 * index)
            assertSelectedPosition(position = 2 * index)
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }
}
