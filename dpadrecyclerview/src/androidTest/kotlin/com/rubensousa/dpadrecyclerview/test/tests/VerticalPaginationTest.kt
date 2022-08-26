package com.rubensousa.dpadrecyclerview.test.tests

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.*
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
    fun testLastSelectedViewStaysAlignedWhenAdapterInsertsNewViews() {
        launchPaginationFragment()
        repeat(19) {
            pressDown()
        }
        assertSelectedPosition(position = 19)

        var viewBounds = getItemViewBounds(position = 19)
        val recyclerViewBounds = getRecyclerViewBounds()
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        waitForIdleScrollState()

        viewBounds = getItemViewBounds(position = 19)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
    }

    private fun launchPaginationFragment() {
        launchPaginationFragment(
            getDefaultLayoutConfiguration(),
            getDefaultAdapterConfiguration()
        )
    }

    private fun launchPaginationFragment(
        layoutConfiguration: TestLayoutConfiguration,
        adapterConfiguration: TestAdapterConfiguration
    ): FragmentScenario<TestPaginationFragment> {
        return launchFragmentInContainer<TestPaginationFragment>(
            fragmentArgs = TestGridFragment.getArgs(
                layoutConfiguration,
                adapterConfiguration
            ),
            themeResId = R.style.TestTheme
        ).also {
            fragmentScenario = it
        }
    }

}