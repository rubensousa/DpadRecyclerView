/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.test.tests

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerViewHelper
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestGridFragment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.testing.DpadSelectionEvent
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.After

abstract class GridTest {

    companion object {
        const val DEFAULT_ITEM_COUNT = 200
    }

    abstract fun getDefaultLayoutConfiguration(): TestLayoutConfiguration

    open fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return TestAdapterConfiguration(
            itemLayoutId = R.layout.dpadrecyclerview_test_item_grid,
            numberOfItems = DEFAULT_ITEM_COUNT
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
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
        }
    }

    fun launchFragment(adapterConfiguration: TestAdapterConfiguration)
            : FragmentScenario<TestGridFragment> {
        return launchFragment(getDefaultLayoutConfiguration(), adapterConfiguration)
    }

    fun getSelectionEvents(): List<DpadSelectionEvent> {
        var events = listOf<DpadSelectionEvent>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getSelectionEvents()
        }
        return events
    }

    fun getSelectionAndAlignedEvents(): List<DpadSelectionEvent> {
        var events = listOf<DpadSelectionEvent>()
        fragmentScenario.onFragment { fragment ->
            events = fragment.getSelectedAndAlignedEvents()
        }
        return events
    }

    fun getTasksExecuted(): List<DpadSelectionEvent> {
        var events = listOf<DpadSelectionEvent>()
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
        // Reset this for every test
        DpadRecyclerViewHelper.enableNewPivotLayoutManager(false)
    }

}
