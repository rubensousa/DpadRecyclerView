package com.rubensousa.dpadrecyclerview.test.tests

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.TestPaginationFragment
import com.rubensousa.dpadrecyclerview.test.helpers.assertSelectedPosition
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForAdapterUpdate
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.testing.KeyEvents.pressDown
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.After
import org.junit.Rule
import org.junit.Test

class VerticalPaginationTest : GridTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            )
        )
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(numberOfItems = 20)
    }

    private lateinit var fragmentScenario: FragmentScenario<TestPaginationFragment>

    @After
    override fun destroy() {
        fragmentScenario.moveToState(Lifecycle.State.DESTROYED)
    }

    @Test
    fun testLastSelectedViewStaysAlignedWhenAdapterInsertsNewViewsDuringScroll() {
        launchPaginationFragment()
        repeat(19) {
            pressDown()
        }
        assertSelectedPosition(position = 19)

        waitForIdleScrollState()

        val viewBounds = getItemViewBounds(position = 19)
        val recyclerViewBounds = getRecyclerViewBounds()
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
    }

    @Test
    fun testLastSelectedViewStaysAlignedWhenAdapterInsertsNewViewsWhenIdle() {
        val delay = 2500L
        launchPaginationFragment(loadDelay = delay)

        selectLastPosition(smooth = false)
        assertSelectedPosition(position = 19)

        var viewBounds = getItemViewBounds(position = 19)
        val recyclerViewBounds = getRecyclerViewBounds()
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        waitForAdapterUpdate()
        waitForIdleScrollState()

        onRecyclerView("Checking Adapter Item count") { recyclerView ->
            assertThat(recyclerView.adapter?.itemCount).isGreaterThan(20)
        }
        viewBounds = getItemViewBounds(position = 19)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
    }

    private fun launchPaginationFragment(loadDelay: Long = 0L) {
        launchPaginationFragment(
            loadDelay,
            getDefaultLayoutConfiguration(),
            getDefaultAdapterConfiguration()
        )
    }

    private fun launchPaginationFragment(
        loadDelay: Long,
        layoutConfiguration: TestLayoutConfiguration,
        adapterConfiguration: TestAdapterConfiguration
    ): FragmentScenario<TestPaginationFragment> {
        return launchFragmentInContainer<TestPaginationFragment>(
            fragmentArgs = TestPaginationFragment.getArgs(
                loadDelay,
                layoutConfiguration,
                adapterConfiguration
            ),
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
        }
    }

}