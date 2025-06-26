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
import com.rubensousa.dpadrecyclerview.test.helpers.waitForLayout
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class FocusDebounceTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                fraction = 0.5f
            ),
            childAlignment = ChildAlignment(offset = 0, fraction = 0.5f)
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration()
            .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal)
    }

    @Test
    fun testFocusDoesNotMoveWithinDebounceValue() = report {
        val debounceMs = 1000
        Given("Launch fragment with debounce configured") {
            launchFragment()
            setupRecyclerView(debounceMs = debounceMs)
        }
        When("Press 2 keys within $debounceMs ms") {
            KeyEvents.pressRight(times = 2, 0L)
            waitForIdleScrollState()
        }
        Then("Position 1 is selected") {
            assertFocusAndSelection(1)
        }
    }

    @Test
    fun testFocusMovesAfterDebounce() = report {
        val debounceMs = 1000
        Given("Launch fragment with debounce configured") {
            launchFragment()
            setupRecyclerView(debounceMs = debounceMs)
        }
        When("Press 2 keys spaced by more than $debounceMs") {
            KeyEvents.pressRight(times = 2, debounceMs + 500L)
            waitForIdleScrollState()
        }
        Then("Position 2 is selected") {
            assertFocusAndSelection(2)
        }
    }

    private fun setupRecyclerView(
        debounceMs: Int?,
        scrollDuration: Int = 2000
    ) {
        onRecyclerView("Setup") { recyclerView ->
            recyclerView.setFocusSearchDebounceMs(debounceMs)
            recyclerView.setSmoothScrollBehavior(object : DpadRecyclerView.SmoothScrollByBehavior {
                override fun configSmoothScrollByDuration(dx: Int, dy: Int): Int {
                    return scrollDuration
                }

                override fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator {
                    return LinearInterpolator()
                }
            })
        }
        waitForLayout()
    }
}
