package com.rubensousa.dpadrecyclerview.test.tests

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.FastUiAutomatorRule
import com.rubensousa.dpadrecyclerview.test.helpers.UiAutomatorHelper.pressDown
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SaveRestoreStateTest : GridTest() {

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
    fun testSelectionStateIsSavedAndRestored() {
        pressDown(times = 5)
        assertFocusPosition(5)

        recreateFragment()

        val recyclerViewBounds = getRecyclerViewBounds()
        val viewBounds = getItemViewBounds(position = 5)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertFocusPosition(5)
    }

}
