package com.rubensousa.dpadrecyclerview.test.tests.scrolling

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class PendingAlignmentTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX,
                fraction = 0.0f
            ),
            childAlignment = ChildAlignment(offset = 0, fraction = 0.0f)
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration()
            .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal)
    }

    @Test
    fun testFocusDoesNotMoveIfPendingAlignmentLimitIsReached() {
        launchFragment()

        setupRecyclerView(maxPendingAlignments = 1)

        KeyEvents.pressRight(times = 5)

        assertFocusAndSelection(position = 1)
    }

    @Test
    fun testPendingAlignmentsAreResetWhenIdle() {
        launchFragment()

        setupRecyclerView(maxPendingAlignments = 2)

        KeyEvents.pressRight(times = 5)

        assertFocusAndSelection(position = 2)

        waitForIdleScrollState()

        KeyEvents.pressRight(times = 5)

        assertFocusAndSelection(position = 4)
    }

    @Test
    fun testPendingAlignmentInOppositeDirectionIsIgnored() {
        launchFragment()

        setupRecyclerView(maxPendingAlignments = 1)

        KeyEvents.pressRight()
        KeyEvents.pressLeft()

        assertFocusAndSelection(position = 0)
    }

    @Test
    fun testFocusMovesIfPendingAlignmentLimitIsNotReached() {
        launchFragment()

        setupRecyclerView(maxPendingAlignments = 3)

        KeyEvents.pressRight(times = 3)

        assertFocusAndSelection(position = 3)
    }

    private fun setupRecyclerView(
        maxPendingAlignments: Int,
        maxPendingMoves: Int = 10,
        scrollDuration: Int = 4000
    ) {
        onRecyclerView("Setup") { recyclerView ->
            recyclerView.setSmoothScrollMaxPendingMoves(maxPendingMoves)
            recyclerView.setSmoothScrollMaxPendingAlignments(maxPendingAlignments)
            recyclerView.setSmoothScrollBehavior(object : DpadRecyclerView.SmoothScrollByBehavior {
                override fun configSmoothScrollByDuration(dx: Int, dy: Int): Int {
                    return scrollDuration
                }

                override fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator {
                    return LinearInterpolator()
                }
            })
        }
    }
}
