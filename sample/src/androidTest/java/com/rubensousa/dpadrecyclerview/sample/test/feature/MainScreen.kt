/*
 * Copyright 2023 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.sample.test.feature

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf

class MainScreen {

    private companion object {
        val ROOT = R.id.mainScreenRoot
        val RECYCLERVIEW = R.id.recyclerView
        val FEATURE_RECYCLERVIEW = R.id.featureRecyclerView
        val FEATURE_GROUP_TEXTVIEW = R.id.featureGroupTextView
        val FEATURE_LIST_LAYOUT = R.id.featureListLayout
        val FEATURE_CARD = R.id.featureCardView
    }

    fun assertIsDisplayed() {
        onView(withId(ROOT)).check(matches(isDisplayed()))
    }

    fun assertIsNotDisplayed() {
        onView(withId(ROOT)).check(doesNotExist())
    }

    fun scrollTo(group: FeatureGroup, item: FeatureItem) {
        onView(withId(RECYCLERVIEW)).perform(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(group.title))
            )
        )
        onView(getFeatureRecyclerViewMatcher(group.title)).perform(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(item.text))
            )
        )
        assertIsFocused(group, item)
    }

    fun assertIsFocused(group: FeatureGroup, item: FeatureItem) {
        onView(
            allOf(
                withId(FEATURE_CARD),
                isDescendantOfA(getFeatureRecyclerViewMatcher(group.title)),
                hasDescendant(withText(item.text)),
                hasFocus()
            )
        ).check(matches(isDisplayed()))
    }

    private fun getFeatureRecyclerViewMatcher(title: String): Matcher<View> {
        return allOf(
            withId(FEATURE_RECYCLERVIEW),
            isDescendantOfA(
                allOf(
                    withId(FEATURE_LIST_LAYOUT),
                    hasDescendant(
                        allOf(
                            withId(FEATURE_GROUP_TEXTVIEW),
                            withText(title)
                        )
                    )
                )
            )
        )
    }

}
