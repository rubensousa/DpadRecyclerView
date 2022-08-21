package com.rubensousa.dpadrecyclerview

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import com.rubensousa.dpadrecyclerview.helpers.TimberRule
import com.rubensousa.dpadrecyclerview.test.R
import org.junit.After
import org.junit.Rule

abstract class GridTest {

    @get:Rule
    val timberRule = TimberRule()

    abstract fun getDefaultLayoutConfiguration(): TestGridFragment.LayoutConfiguration

    open fun getDefaultAdapterConfiguration(): TestGridFragment.AdapterConfiguration {
        return TestGridFragment.AdapterConfiguration(
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

    fun launchFragment(layoutConfiguration: TestGridFragment.LayoutConfiguration)
            : FragmentScenario<TestGridFragment> {
        return launchFragment(layoutConfiguration, getDefaultAdapterConfiguration())
    }

    fun launchFragment(
        layoutConfiguration: TestGridFragment.LayoutConfiguration,
        adapterConfiguration: TestGridFragment.AdapterConfiguration
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

    fun launchFragment(adapterConfiguration: TestGridFragment.AdapterConfiguration)
            : FragmentScenario<TestGridFragment> {
        return launchFragment(getDefaultLayoutConfiguration(), adapterConfiguration)
    }

    fun getSelectionEvents(): List<TestGridFragment.SelectionEvent> {
        var events = listOf<TestGridFragment.SelectionEvent>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getSelectionEvents()
        }
        return events
    }

    fun getSelectionAndPositionedEvents(): List<TestGridFragment.SelectionEvent> {
        var events = listOf<TestGridFragment.SelectionEvent>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getSelectedAndPositionedEvents()
        }
        return events
    }

    @After
    fun destroy() {
        fragmentScenario.moveToState(Lifecycle.State.DESTROYED)
    }

}
