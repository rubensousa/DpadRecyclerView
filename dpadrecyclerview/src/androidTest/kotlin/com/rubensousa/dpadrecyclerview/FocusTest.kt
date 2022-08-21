package com.rubensousa.dpadrecyclerview

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.helpers.FastUiAutomatorRule
import com.rubensousa.dpadrecyclerview.helpers.UiAutomatorHelper
import com.rubensousa.dpadrecyclerview.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.helpers.assertSelectedPosition
import org.junit.Rule
import org.junit.Test

class FocusTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

    override fun getDefaultLayoutConfiguration(): TestGridFragment.LayoutConfiguration {
        return TestGridFragment.LayoutConfiguration(
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
