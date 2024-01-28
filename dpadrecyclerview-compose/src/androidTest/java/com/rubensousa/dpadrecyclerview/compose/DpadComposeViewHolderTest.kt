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

package com.rubensousa.dpadrecyclerview.compose

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import org.junit.Rule
import org.junit.Test

class DpadComposeViewHolderTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun testComposeItemsReceiveFocus() {
        assertFocus(item = 0, isFocused = true)
        assertSelection(item = 0, isSelected = true)

        assertFocus(item = 1, isFocused = false)
        assertSelection(item = 1, isSelected = false)

        assertFocus(item = 2, isFocused = false)
        assertSelection(item = 2, isSelected = false)

        KeyEvents.pressDown()
        waitForIdleScroll()

        assertFocus(item = 0, isFocused = false)
        assertSelection(item = 0, isSelected = false)

        assertFocus(item = 1, isFocused = true)
        assertSelection(item = 1, isSelected = true)
    }

    @Test
    fun testComposeFocusChanges() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.clearFocus()
        }

        Espresso.onIdle()
        assertFocus(item = 0, isFocused = false)
        assertSelection(item = 0, isSelected = true)

        assertFocus(item = 1, isFocused = false)
        assertSelection(item = 1, isSelected = false)

        assertFocus(item = 2, isFocused = false)
        assertSelection(item = 2, isSelected = false)

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.requestFocus()
        }

        assertFocus(item = 0, isFocused = true)
        assertSelection(item = 0, isSelected = true)

        assertFocus(item = 1, isFocused = false)
        assertSelection(item = 1, isSelected = false)

        assertFocus(item = 2, isFocused = false)
        assertSelection(item = 2, isSelected = false)
    }

    @Test
    fun testNextComposeItemsReceiveFocus() {
        KeyEvents.pressDown()
        waitForIdleScroll()

        assertFocus(item = 0, isFocused = false)
        assertFocus(item = 1, isFocused = true)
    }

    @Test
    fun testClicksAreDispatched() {
        KeyEvents.click()

        KeyEvents.pressDown()
        waitForIdleScroll()

        KeyEvents.click()

        var clicks: List<Int> = emptyList()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            clicks = activity.getClicks()
        }

        assertThat(clicks).isEqualTo(listOf(0, 1))
    }

    @Test
    fun testCompositionIsClearedWhenClearingAdapter() {
        val viewHolders = ArrayList<RecyclerView.ViewHolder>()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            viewHolders.addAll(activity.getViewsHolders())
            activity.removeAdapter()
        }

        viewHolders.forEach { viewHolder ->
            val composeView = viewHolder.itemView as ComposeView
            assertThat(composeView.hasComposition).isFalse()
        }
        composeTestRule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun testCompositionIsNotClearedWhenDetachingFromWindow() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.getRecyclerView().setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
                override fun calculateStartExtraLayoutSpace(state: RecyclerView.State): Int {
                    return 1080
                }
            })
        }
        repeat(3) {
            KeyEvents.pressDown()
            waitForIdleScroll()
        }

        composeTestRule.onNodeWithText("0").assertExists()
        composeTestRule.onNodeWithText("0").assertIsNotDisplayed()
    }

    @Test
    fun testCompositionIsClearedWhenViewHolderIsRecycled() {
        repeat(5) {
            KeyEvents.pressDown()
            waitForIdleScroll()
        }

        composeTestRule.onNodeWithText("0").assertDoesNotExist()

        var disposals: List<Int> = emptyList()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            disposals = activity.getDisposals()
        }

        assertThat(disposals).contains(0)
    }

    private fun waitForIdleScroll() {
        onView(ViewMatchers.isAssignableFrom(DpadRecyclerView::class.java))
            .perform(DpadRecyclerViewActions.waitForIdleScroll())
    }

    private fun assertFocus(item: Int, isFocused: Boolean) {
        composeTestRule.onNodeWithText(item.toString()).assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(TestComposable.focusedKey, isFocused))
    }

    private fun assertSelection(item: Int, isSelected: Boolean) {
        composeTestRule.onNodeWithText(item.toString()).assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(TestComposable.selectedKey, isSelected))
    }

}


