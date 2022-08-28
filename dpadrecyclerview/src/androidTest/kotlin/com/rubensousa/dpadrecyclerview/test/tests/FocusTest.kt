package com.rubensousa.dpadrecyclerview.test.tests

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.FastUiAutomatorRule
import com.rubensousa.dpadrecyclerview.test.helpers.UiAutomatorHelper.pressDown
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
            pressDown()
        }
    }

    @Test
    fun testViewHoldersNotFocusableDoNotReceiveFocus() {
        launchFragment(getDefaultAdapterConfiguration().copy(alternateFocus = true))

        repeat(5) { index ->
            assertFocusPosition(position = 2 * index)
            assertSelectedPosition(position = 2 * index)
            pressDown()
        }
    }
}
