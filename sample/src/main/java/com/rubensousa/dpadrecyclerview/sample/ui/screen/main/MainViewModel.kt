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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val features = MutableLiveData<List<FeatureList>>()

    fun getFeatures(): LiveData<List<FeatureList>> = features

    fun load() {
        viewModelScope.launch(Dispatchers.Default) {
            features.postValue(buildFeatureLists())
        }
    }

    private fun buildFeatureLists(): List<FeatureList> {
        return listOf(
            buildNestedFeatureList(),
            buildGridFeatureList(),
            buildComposeFeatureList(),
            buildScrollingFeatureList(),
            buildFocusFeatureList(),
            buildReverseLayoutFeatureList()
        )
    }

    private fun buildNestedFeatureList(): FeatureList {
        return FeatureList(
            title = "Nested Lists",
            destinations = listOf(
                ScreenDestination(
                    direction = MainFragmentDirections.openList(),
                    title = "Standard"
                ),
                ScreenDestination(
                    direction = MainFragmentDirections.openList().apply { slowScroll = true },
                    title = "With Header"
                )
            ),
        )
    }

    private fun buildGridFeatureList(): FeatureList {
        return FeatureList(
            title = "Grids",
            destinations = listOf(
                ScreenDestination(
                    direction = MainFragmentDirections.openStandardGrid(),
                    title = "Equal span sizes"
                ),
                ScreenDestination(
                    direction = MainFragmentDirections.openStandardGrid(),
                    title = "Different span sizes"
                )
            ),
        )
    }

    private fun buildReverseLayoutFeatureList(): FeatureList {
        return FeatureList(
            title = "Reverse layout",
            destinations = listOf(
                ScreenDestination(
                    direction = MainFragmentDirections.openList().apply { reverseLayout = true },
                    title = "Nested lists"
                ),
                ScreenDestination(
                    direction = MainFragmentDirections.openStandardGrid()
                        .apply { reverseLayout = true },
                    title = "Grid"
                ),
            ),
        )
    }

    private fun buildScrollingFeatureList(): FeatureList {
        return FeatureList(
            title = "Scrolling behavior",
            destinations = listOf(
                ScreenDestination(
                    direction = MainFragmentDirections.openList().apply { slowScroll = true },
                    title = "Slow and linear"
                )
            ),
        )
    }

    private fun buildFocusFeatureList(): FeatureList {
        return FeatureList(
            title = "Focus",
            destinations = listOf(
                ScreenDestination(
                    direction = MainFragmentDirections.openHorizontalLeanback(),
                    title = "Searching for unknown pivot"
                )
            ),
        )
    }

    private fun buildComposeFeatureList(): FeatureList {
        return FeatureList(
            title = "Compose",
            destinations = listOf(
                ScreenDestination(
                    direction = MainFragmentDirections.openComposeList(),
                    title = "Nested lists"
                ),
                ScreenDestination(
                    direction = MainFragmentDirections.openComposeGrid(),
                    title = "Grid"
                )
            ),
        )
    }

}
