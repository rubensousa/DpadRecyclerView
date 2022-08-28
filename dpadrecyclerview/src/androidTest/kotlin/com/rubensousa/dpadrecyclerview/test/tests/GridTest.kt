package com.rubensousa.dpadrecyclerview.test.tests

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.*
import org.junit.After

abstract class GridTest {

    abstract fun getDefaultLayoutConfiguration(): TestLayoutConfiguration

    open fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return TestAdapterConfiguration(
            itemLayoutId = R.layout.test_item_grid,
            numberOfItems = 200
        )
    }

    private lateinit var fragmentScenario: FragmentScenario<TestGridFragment>

    fun recreateFragment() {
        fragmentScenario.recreate()
    }

    fun launchFragment(): FragmentScenario<TestGridFragment> {
        return launchFragment(getDefaultLayoutConfiguration())
    }

    fun launchFragment(
        parentAlignment: ParentAlignment,
        childAlignment: ChildAlignment
    ): FragmentScenario<TestGridFragment> {
        val newLayoutConfiguration = getDefaultLayoutConfiguration()
            .copy(
                parentAlignment = parentAlignment,
                childAlignment = childAlignment
            )
        return launchFragment(newLayoutConfiguration)
    }

    fun launchFragment(layoutConfiguration: TestLayoutConfiguration)
            : FragmentScenario<TestGridFragment> {
        return launchFragment(layoutConfiguration, getDefaultAdapterConfiguration())
    }

    fun launchFragment(
        layoutConfiguration: TestLayoutConfiguration,
        adapterConfiguration: TestAdapterConfiguration
    ): FragmentScenario<TestGridFragment> {
        return launchFragmentInContainer<TestGridFragment>(
            fragmentArgs = TestGridFragment.getArgs(
                layoutConfiguration,
                adapterConfiguration
            ),
            themeResId = R.style.TestTheme
        ).also {
            fragmentScenario = it
        }
    }

    fun launchFragment(adapterConfiguration: TestAdapterConfiguration)
            : FragmentScenario<TestGridFragment> {
        return launchFragment(getDefaultLayoutConfiguration(), adapterConfiguration)
    }

    fun getSelectionEvents(): List<TestPosition> {
        var events = listOf<TestPosition>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getSelectionEvents()
        }
        return events
    }

    fun getSelectionAndAlignedEvents(): List<TestPosition> {
        var events = listOf<TestPosition>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getSelectedAndAlignedEvents()
        }
        return events
    }

    fun getTasksExecuted(): List<TestPosition> {
        var events = listOf<TestPosition>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getTasksExecuted()
        }
        return events
    }

    fun selectWithTask(position: Int, smooth: Boolean, executeWhenAligned: Boolean) {
        fragmentScenario.onFragment { fragment ->
           fragment.selectWithTask(position, smooth, executeWhenAligned)
        }
    }

    @After
    open fun destroy() {
        fragmentScenario.moveToState(Lifecycle.State.DESTROYED)
    }

}
