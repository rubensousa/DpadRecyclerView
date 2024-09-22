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
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.rubensousa.dpadrecyclerview.testing.DpadGridFragment
import com.rubensousa.dpadrecyclerview.testing.DpadVerticalFragment
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class ScrollToActionsTest : RecyclerViewTest() {

    @get:Rule
    val disableIdleTimeoutRule = DisableIdleTimeoutRule()

    @Test
    fun testScrollDownToItem() {
        launchVerticalFragment()

        val position = 5
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            )
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    @Test
    fun testScrollUpToItem() {
        launchVerticalFragment()

        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText("5"))
            )
        )

        val position = 0
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            ),
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    @Test
    fun testScrollDownToGridItemInSameSpanIndex() {
        launchGridFragment()

        val position = 15
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            )
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    @Test
    fun testScrollUpToGridItemInSameSpanIndex() {
        launchGridFragment()

        performActions(DpadRecyclerViewActions.selectPosition(15, smooth = false))

        val position = 0
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            )
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    @Test
    fun testScrollDownToGridItemInLastSpanIndex() {
        launchGridFragment()

        val position = DpadGridFragment.SPAN_COUNT * 3 - 1
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            )
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    @Test
    fun testScrollUpToGridItemInFirstSpanIndex() {
        launchGridFragment()

        performActions(
            DpadRecyclerViewActions.selectPosition(
                DpadGridFragment.SPAN_COUNT * 3 - 1, smooth = false
            )
        )
        val position = 0
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            )
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    @Test
    fun testScrollForwardToGridItemInSameSpanGroup() {
        launchGridFragment()

        val position = DpadGridFragment.SPAN_COUNT - 1
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            )
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    @Test
    fun testScrollBackwardToGridItemInSameSpanGroup() {
        launchGridFragment()

        performActions(
            DpadRecyclerViewActions.selectPosition(
                DpadGridFragment.SPAN_COUNT - 1,
                smooth = false
            )
        )

        val position = 0
        performActions(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(position.toString()))
            )
        )

        assert(DpadRecyclerViewAssertions.isSelected(position))
        assert(DpadRecyclerViewAssertions.isFocused(position))
    }

    private fun launchVerticalFragment(): FragmentScenario<DpadVerticalFragment> {
        return launchFragmentInContainer(
            themeResId = R.style.DpadRecyclerViewTestTheme
        )
    }
}
