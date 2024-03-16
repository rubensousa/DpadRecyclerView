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

package com.rubensousa.dpadrecyclerview.sample.test.list

import android.view.View
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAny
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemComposable
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.core.AllOf.allOf

class ComposeListScreen(private val composeTestRule: ComposeTestRule) {

    private companion object {
        val ROOT = R.id.listScreenRoot
        val RECYCLERVIEW = R.id.recyclerView
        val CARD_RECYCLERVIEW = R.id.cardRecyclerView
        val CARD_LIST_TEXTVIEW = R.id.cardListTextView
        val CARD_LIST_LAYOUT = R.id.cardListLayout
    }

    fun assertIsDisplayed() {
        onView(withId(ROOT)).check(matches(isDisplayed()))
    }

    fun assertIsNotDisplayed() {
        onView(withId(ROOT)).check(ViewAssertions.doesNotExist())
    }

    fun scrollTo(list: CardList, item: CardItem) {
        onView(withId(RECYCLERVIEW)).perform(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText(list.title))
            )
        )
        onView(getCardRecyclerViewMatcher(list.title)).perform(
            DpadRecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                withContentDescription(item.text)
            )
        )
        assertIsFocused(list, item)
    }

    fun assertIsFocused(list: CardList, item: CardItem) {
        onView(
            allOf(
                withContentDescription(item.text),
                isDescendantOfA(getCardRecyclerViewMatcher(list.title)),
                hasFocus()
            )
        ).check(matches(isDisplayed()))
        composeTestRule.onAllNodesWithText(item.text)
            .assertAny(SemanticsMatcher.expectValue(ItemComposable.focusedKey, true))
    }

    fun assertIsNotFocused(list: CardList, item: CardItem) {
        onView(
            allOf(
                withContentDescription(item.text),
                isDescendantOfA(getCardRecyclerViewMatcher(list.title)),
                Matchers.not(hasFocus())
            )
        ).check(matches(isDisplayed()))
        composeTestRule.onAllNodesWithText(item.text)
            .assertAny(SemanticsMatcher.expectValue(ItemComposable.focusedKey, false))
    }

    private fun getCardRecyclerViewMatcher(title: String): Matcher<View> {
        return allOf(
            withId(CARD_RECYCLERVIEW),
            isDescendantOfA(
                allOf(
                    withId(CARD_LIST_LAYOUT),
                    hasDescendant(
                        allOf(
                            withId(CARD_LIST_TEXTVIEW),
                            withText(title)
                        )
                    )
                )
            )
        )
    }

}
