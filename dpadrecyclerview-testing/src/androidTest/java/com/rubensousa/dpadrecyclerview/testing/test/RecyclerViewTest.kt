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

package com.rubensousa.dpadrecyclerview.testing.test

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.matcher.ViewMatchers
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.testfixtures.DefaultInstrumentedReportRule
import com.rubensousa.dpadrecyclerview.testing.DpadGridFragment
import com.rubensousa.dpadrecyclerview.testing.DpadSubPositionFragment
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Rule

abstract class RecyclerViewTest {

    @get:Rule(order = -1)
    val report = DefaultInstrumentedReportRule()

    private lateinit var subPositionFragment: FragmentScenario<DpadSubPositionFragment>
    private lateinit var gridFragment: FragmentScenario<DpadGridFragment>

    protected fun onGridFragment(block: (fragment: DpadGridFragment) -> Unit) {
        gridFragment.onFragment { fragment ->
            block(fragment)
        }
    }

    protected fun assertGridAdapterCount(size: Int) {
        var itemCount = 0
        onGridFragment { fragment ->
            itemCount = fragment.getAdapterSize()
        }
        assertThat(itemCount).isEqualTo(size)
    }

    protected fun assert(vararg assertions: ViewAssertion) {
        val interaction = getViewInteraction()
        assertions.forEach { assertion ->
            interaction.check(assertion)
        }
    }

    protected fun performActions(vararg actions: ViewAction) {
        getViewInteraction().perform(*actions)
    }

    private fun getViewInteraction(): ViewInteraction {
        return Espresso.onView(ViewMatchers.withId(R.id.recyclerView))
    }

    protected fun launchGridFragment(): FragmentScenario<DpadGridFragment> {
        return launchFragmentInContainer<DpadGridFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            gridFragment = it
        }
    }

    protected fun launchSubPositionFragment(): FragmentScenario<DpadSubPositionFragment> {
        return launchFragmentInContainer<DpadSubPositionFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            subPositionFragment = it
        }
    }

}
