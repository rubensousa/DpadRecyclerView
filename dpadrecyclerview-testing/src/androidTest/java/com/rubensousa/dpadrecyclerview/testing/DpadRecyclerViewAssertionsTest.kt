package com.rubensousa.dpadrecyclerview.testing

import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import org.junit.Test

class DpadRecyclerViewAssertionsTest : RecyclerViewTest() {

    @Test
    fun testCurrentFocusPosition() {
        launchGridFragment()

        // First item is focused by default
        assert(DpadRecyclerViewAssertions.isFocused(position = 0))

        performActions(DpadViewActions.clearFocus())

        // Check that no position is focused
        assert(DpadRecyclerViewAssertions.isNotFocused())
    }

    @Test
    fun testCurrentPosition() {
        launchSubPositionFragment()

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 3),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 3))

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 3, subPosition = 1, smooth = false),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 3, subPosition = 1))

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 3, subPosition = 2),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 3, subPosition = 2))

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 6, subPosition = 1),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 6, subPosition = 1))
    }

}
