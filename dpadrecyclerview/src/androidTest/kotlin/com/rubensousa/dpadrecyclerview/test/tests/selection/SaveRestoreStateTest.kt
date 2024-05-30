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

package com.rubensousa.dpadrecyclerview.test.tests.selection

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withTagValue
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.test.TestNestedListFragment
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertOnRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.hamcrest.Matchers
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SaveRestoreStateTest {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    private lateinit var fragmentScenario: FragmentScenario<TestNestedListFragment>

    @Before
    fun setup() {
        fragmentScenario = launchFragment()
    }

    @Test
    fun testSelectionStateIsSavedAndRestored() {
        KeyEvents.pressDown(times = 5)
        assertFocusPosition(5)

        fragmentScenario.recreate()

        val recyclerViewBounds = getRecyclerViewBounds()
        val viewBounds = getItemViewBounds(position = 5)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertOnRecyclerView(ViewAssertions.matches(ViewMatchers.hasFocus()))
        assertFocusPosition(5)
    }

    @Test
    fun testSelectionStateAcrossNestedListsIsSaved() {
        // given
        KeyEvents.pressRight(times = 5)

        // when
        KeyEvents.pressDown(times = 25)
        KeyEvents.pressUp(times = 25)

        // then
        Espresso.onView(
            allOf(
                withId(com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView),
                withTagValue(Matchers.`is`(0))
            )
        ).check(DpadRecyclerViewAssertions.isSelected(position = 5))
    }

    @Test
    fun testSelectionStateAcrossNestedListsSurvivesConfigurationChanges() {
        // given
        KeyEvents.pressRight(times = 5)
        KeyEvents.pressDown(times = 25)
        KeyEvents.pressUp(times = 25)

        // when
        fragmentScenario.recreate()

        // then
        Espresso.onView(
            allOf(
                withId(com.rubensousa.dpadrecyclerview.test.R.id.nestedRecyclerView),
                withTagValue(Matchers.`is`(0))
            )
        ).check(DpadRecyclerViewAssertions.isSelected(position = 5))
    }

    private fun launchFragment(): FragmentScenario<TestNestedListFragment> {
        return launchFragmentInContainer<TestNestedListFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
            waitForCondition("Waiting for layout pass") { recyclerView ->
                !recyclerView.isLayoutRequested
            }
        }
    }

}
