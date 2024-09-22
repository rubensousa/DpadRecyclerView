package com.rubensousa.dpadrecyclerview.compose

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.testfixtures.DefaultInstrumentedReportRule
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadViewAssertions
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DpadClickableIntegrationTest {

    @get:Rule(order = -1)
    val report = DefaultInstrumentedReportRule()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComposeFocusTestActivity>()

    @Before
    fun setup() {
        composeTestRule.waitForIdle()
        Espresso.onIdle()
    }

    @Test
    fun testPressingBackAfterClickingOnItemClearsFocus() {
        // given
        KeyEvents.click()
        Espresso.onIdle()

        // when
        KeyEvents.back()
        Espresso.onIdle()

        // then
        assertFocus(item = 0, isFocused = false)
        Espresso.onView(
            withId(com.rubensousa.dpadrecyclerview.compose.test.R.id.focusPlaceholder)
        ).check(DpadViewAssertions.isFocused())
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
        Espresso.onIdle()

        // then
        assertThat(clicks).isEqualTo(listOf(0))
    }

    @Test
    fun testLongClicksAreDispatched() {
        // given
        var clicks: List<Int> = emptyList()
        composeTestRule.activityRule.scenario.onActivity { activity ->
            clicks = activity.getLongClicks()
        }

        // when
        KeyEvents.longClick()

        // then
        assertThat(clicks).isEqualTo(listOf(0))
    }

    private fun assertFocus(item: Int, isFocused: Boolean) {
        composeTestRule.onNodeWithText(item.toString()).assertIsDisplayed()
            .assert(SemanticsMatcher.expectValue(TestComposable.focusedKey, isFocused))
    }

}
