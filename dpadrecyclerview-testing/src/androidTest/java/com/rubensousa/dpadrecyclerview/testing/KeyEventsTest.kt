package com.rubensousa.dpadrecyclerview.testing

import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class KeyEventsTest : DpadFragmentTest() {

    @get:Rule
    val disableIdleTimeoutRule = DisableIdleTimeoutRule()

    @Test
    fun testFastHorizontalScroll() {
        KeyEvents.pressRight(times = 20)
        assert(DpadRecyclerViewAssertions.isFocused(position = 20))

        KeyEvents.pressLeft(times = 20)
        assert(DpadRecyclerViewAssertions.isFocused(position = 0))
    }

    @Test
    fun testFastVerticalScroll() {
        KeyEvents.pressDown(times = 20)
        assert(DpadRecyclerViewAssertions.isFocused(position = 20 * 5))

        KeyEvents.pressUp(times = 20)
        assert(DpadRecyclerViewAssertions.isFocused(position = 0))
    }
}
