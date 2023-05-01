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

package com.rubensousa.dpadrecyclerview.sample.test

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.rubensousa.dpadrecyclerview.sample.test.feature.FeatureGroup
import com.rubensousa.dpadrecyclerview.sample.test.feature.FeatureItem
import com.rubensousa.dpadrecyclerview.sample.test.feature.MainScreen
import com.rubensousa.dpadrecyclerview.sample.test.list.CardItem
import com.rubensousa.dpadrecyclerview.sample.test.list.CardList
import com.rubensousa.dpadrecyclerview.sample.test.list.ComposeListScreen
import com.rubensousa.dpadrecyclerview.sample.test.list.ListScreen
import com.rubensousa.dpadrecyclerview.sample.ui.MainActivity
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import org.junit.Rule
import org.junit.Test

class SampleTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private val mainScreen = MainScreen()
    private val listScreen = ListScreen()
    private val composeListScreen = ComposeListScreen(composeTestRule)

    @Test
    fun testNavigationToFeatureItem() {
        mainScreen.assertIsDisplayed()

        KeyEvents.click()

        mainScreen.assertIsNotDisplayed()
        listScreen.assertIsDisplayed()

        KeyEvents.back()

        mainScreen.assertIsDisplayed()
        listScreen.assertIsNotDisplayed()
    }

    @Test
    fun testSelectionStateIsNotLostAcrossDestinationChanges() {
        val featureGroup = FeatureGroup(title = "Grids")
        val featureItem = FeatureItem(text = "Reversed")
        mainScreen.scrollTo(featureGroup, featureItem)

        KeyEvents.click()

        listScreen.assertIsDisplayed()

        KeyEvents.back()

        mainScreen.assertIsFocused(featureGroup, featureItem)
    }

    @Test
    fun testComposeScrollingAction() {
        val featureGroup = FeatureGroup(title = "Compose")
        val featureItem = FeatureItem(text = "Nested lists")
        mainScreen.scrollTo(featureGroup, featureItem)

        KeyEvents.click()

        composeListScreen.assertIsDisplayed()

        val list = CardList("DpadRecyclerView 5")
        val firstItem = CardItem("5")
        composeListScreen.scrollTo(list, firstItem)

        composeListScreen.scrollTo(list, CardItem("4"))

        composeListScreen.assertIsNotFocused(list, firstItem)
    }

}

