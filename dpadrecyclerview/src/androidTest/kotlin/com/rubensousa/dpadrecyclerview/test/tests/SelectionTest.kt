package com.rubensousa.dpadrecyclerview.test.tests

import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.*
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.TestSelectionEvent
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.test.helpers.UiAutomatorHelper.pressRight
import org.junit.Rule
import org.junit.Test

class SelectionTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

    private val defaultConfig = TestLayoutConfiguration(
        spans = 1,
        orientation = RecyclerView.VERTICAL,
        parentAlignment = ParentAlignment(
            edge = Edge.MIN_MAX
        ),
        childAlignment = ChildAlignment(offset = 0)
    )

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return defaultConfig
    }

    @Test
    fun testNoSelectionEventsAreDispatchedForEmptyAdapter() {
        launchFragment(TestAdapterConfiguration(numberOfItems = 0))

        assertSelectedPosition(position = RecyclerView.NO_POSITION)
        assertFocusPosition(position = RecyclerView.NO_POSITION)

        assertThat(getSelectionEvents()).isEmpty()
        assertThat(getSelectionAndAlignedEvents()).isEmpty()

        recreateFragment()

        assertSelectedPosition(position = RecyclerView.NO_POSITION)
        assertFocusPosition(position = RecyclerView.NO_POSITION)

        assertThat(getSelectionEvents()).isEmpty()
        assertThat(getSelectionAndAlignedEvents()).isEmpty()
    }

    @Test
    fun testSelectionEventAreDispatchedForInitialState() {
        launchFragment()

        assertSelectedPosition(position = 0)
        assertFocusPosition(position = 0)

        assertThat(getSelectionEvents()).isEqualTo(
            listOf(
                TestSelectionEvent(
                    position = 0,
                    subPosition = 0
                )
            )
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(
                TestSelectionEvent(
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
                TestSelectionEvent(
                    position = 0,
                    subPosition = 0
                )
            )
        )
        assertThat(getSelectionAndAlignedEvents()).isEqualTo(
            listOf(
                TestSelectionEvent(
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

    @Test
    fun testViewHoldersAlreadyAlignedStillDispatchAlignedEvent() {
        launchFragment(getDefaultLayoutConfiguration().copy(spans = 5))

        val expectedEvents = ArrayList<TestSelectionEvent>()
        expectedEvents.add(TestSelectionEvent(position = 0))

        repeat(4) { iteration ->
            pressRight()
            assertSelectedPosition(position = iteration + 1)
            assertFocusPosition(position = iteration + 1)
            expectedEvents.add(TestSelectionEvent(position = iteration + 1))
        }

        assertThat(getSelectionAndAlignedEvents()).isEqualTo(expectedEvents)
    }

}
