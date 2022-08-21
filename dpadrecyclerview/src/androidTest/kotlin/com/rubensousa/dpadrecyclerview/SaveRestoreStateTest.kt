package com.rubensousa.dpadrecyclerview

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.helpers.UiAutomatorHelper
import com.rubensousa.dpadrecyclerview.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import org.junit.Before
import org.junit.Test

class SaveRestoreStateTest : GridTest() {

    override fun getDefaultLayoutConfiguration(): TestGridFragment.LayoutConfiguration {
        return TestGridFragment.LayoutConfiguration(
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
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, 5)
        assertFocusPosition(5)

        recreateFragment()

        val recyclerViewBounds = getRecyclerViewBounds()
        val viewBounds = getItemViewBounds(position = 5)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertFocusPosition(5)
    }

}
