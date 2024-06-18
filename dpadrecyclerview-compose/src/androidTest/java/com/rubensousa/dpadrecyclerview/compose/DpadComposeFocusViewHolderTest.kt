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
import com.rubensousa.dpadrecyclerview.compose.test.ComposeFocusTestActivity
import com.rubensousa.dpadrecyclerview.testfixtures.DpadFocusEvent
import com.rubensousa.dpadrecyclerview.testfixtures.recording.ScreenRecorderRule
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import org.junit.Rule
import org.junit.Test

class DpadComposeFocusViewHolderTest {

    @get:Rule
    val screenRecorderRule = ScreenRecorderRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeFocusTestActivity>()

    @Test
    fun testFirstItemHasFocus() {
        assertFocus(item = 0, isFocused = true)
    }

    @Test
    fun testNextItemReceivesFocus() {
        KeyEvents.pressDown()
        waitForIdleScroll()

        assertFocus(item = 1, isFocused = true)
    }

    @Test
    fun testComposeFocusChanges() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.clearFocus()
        }

        Espresso.onIdle()
        assertFocus(item = 0, isFocused = false)

        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.requestFocus()
        }

        assertFocus(item = 0, isFocused = true)
    }

    @Test
    fun testClicksAreDispatched() {
        // given
        var clicks: List<Int> = emptyList()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            clicks = activity.getClicks()
        }

        // when
        KeyEvents.click()

        // then
        assertThat(clicks).isEqualTo(listOf(0))
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
            activity.getRecyclerView()
                .setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
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
        KeyEvents.pressDown(times = 10)
        waitForIdleScroll()

        composeTestRule.onNodeWithText("0").assertDoesNotExist()

        var disposals: List<Int> = emptyList()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            disposals = activity.getDisposals()
        }

        assertThat(disposals).contains(0)
    }

    @Test
    fun testFocusEventIsReceivedForFirstChild() {
        var focusEvents: List<DpadFocusEvent> = emptyList()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            focusEvents = activity.getFocusEvents()
        }

        // when
        onView(ViewMatchers.withId(R.id.recyclerView))
            .perform(
                DpadViewActions.waitForCondition<DpadRecyclerView>(
                    description = "Wait for focus event",
                    condition = { recyclerView -> focusEvents.isNotEmpty() }
                )
            )

        assertThat(focusEvents).hasSize(1)
        val event = focusEvents.first()
        assertThat(event.position).isEqualTo(0)
        assertThat(event.child).isInstanceOf(ComposeView::class.java)
    }

    @Test
    fun testAllViewHoldersAreFocusedOnKeyPress() {
        // given
        val events = 5

        // when
        repeat(events) {
            KeyEvents.pressDown()
            waitForIdleScroll()
        }

        // then
        var focusEvents: List<DpadFocusEvent> = emptyList()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            focusEvents = activity.getFocusEvents()
        }

        assertThat(focusEvents).hasSize(events + 1)
        assertThat(focusEvents.map { it.position }).isEqualTo(List(events + 1) { it })
    }

    private fun waitForIdleScroll() {
        onView(ViewMatchers.isAssignableFrom(DpadRecyclerView::class.java))
            .perform(DpadRecyclerViewActions.waitForIdleScroll())
    }

    private fun assertFocus(item: Int, isFocused: Boolean) {
        composeTestRule.onNodeWithText(item.toString()).assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(TestComposable.focusedKey, isFocused))
    }

}
