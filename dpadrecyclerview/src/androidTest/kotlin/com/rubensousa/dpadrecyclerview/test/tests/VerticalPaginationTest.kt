package com.rubensousa.dpadrecyclerview.test.tests

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.TestPaginationFragment
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.test.helpers.UiAutomatorHelper.pressDown
import org.junit.After
import org.junit.Rule
import org.junit.Test

class VerticalPaginationTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

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
        val delay = 3500L
        launchPaginationFragment(loadDelay = delay)
        repeat(19) {
            pressDown()
        }
        assertSelectedPosition(position = 19)

        waitForIdleScrollState()

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
            themeResId = R.style.TestTheme
        ).also {
            fragmentScenario = it
        }
    }

}