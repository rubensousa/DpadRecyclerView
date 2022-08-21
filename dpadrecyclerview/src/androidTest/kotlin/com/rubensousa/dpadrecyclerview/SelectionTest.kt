package com.rubensousa.dpadrecyclerview

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.helpers.*
import org.junit.Rule
import org.junit.Test

class SelectionTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

    private val defaultConfig = TestGridFragment.LayoutConfiguration(
        spans = 1,
        orientation = RecyclerView.VERTICAL,
        parentAlignment = ParentAlignment(
            edge = Edge.MIN_MAX
        ),
        childAlignment = ChildAlignment(offset = 0)
    )

    override fun getDefaultLayoutConfiguration(): TestGridFragment.LayoutConfiguration {
        return defaultConfig
    }

    @Test
    fun testNoSelectionEventsAreDispatchedForEmptyAdapter() {
        launchFragment(TestGridFragment.AdapterConfiguration(numberOfItems = 0))

        assertSelectedPosition(position = RecyclerView.NO_POSITION)
        assertFocusPosition(position = RecyclerView.NO_POSITION)

        assertThat(getSelectionEvents()).isEmpty()
        assertThat(getSelectionAndPositionedEvents()).isEmpty()
    }

    @Test
    fun testSelectionEventAreDispatchedForInitialState() {
        launchFragment()

        assertSelectedPosition(position = 0)
        assertFocusPosition(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(
                TestGridFragment.SelectionEvent(
                    position = 0,
                    subPosition = 0
                )
            )
        )
        assertThat(getSelectionAndPositionedEvents()).isEqualTo(
            listOf(
                TestGridFragment.SelectionEvent(
                    position = 0,
                    subPosition = 0
                )
            )
        )
    }

    @Test
    fun testSelectionEventsAreDispatchedAfterRecreation() {
        launchFragment()

        assertSelectedPosition(position = 0)
        assertFocusPosition(position = 0)

        recreateFragment()

        assertSelectedPosition(position = 0)
        assertFocusPosition(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(
                TestGridFragment.SelectionEvent(
                    position = 0,
                    subPosition = 0
                )
            )
        )
        assertThat(getSelectionAndPositionedEvents()).isEqualTo(
            listOf(
                TestGridFragment.SelectionEvent(
                    position = 0,
                    subPosition = 0
                )
            )
        )
    }

    @Test
    fun testViewHoldersReceiveSelectionChanges() {
        launchFragment()

        assertSelectedPosition(position = 0)
        assertViewHolderSelected(position = 0, isSelected = true)

        repeat(10) { index ->
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
            assertViewHolderSelected(position = index, isSelected = false)
            assertSelectedPosition(position = index + 1)
            assertViewHolderSelected(position = index + 1, isSelected = true)
        }

    }

}
