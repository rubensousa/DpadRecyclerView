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

    private val destinations = MutableLiveData<List<ScreenDestination>>()

    fun getDestinations(): LiveData<List<ScreenDestination>> = destinations

    fun load() {
        viewModelScope.launch(Dispatchers.Default) {
            destinations.postValue(buildDestinations())
        }
    }

    private fun buildDestinations(): List<ScreenDestination> {
        return listOf(
            ScreenDestination(
                direction = MainFragmentDirections.openList(),
                title = "Nested List -> Detail"
            ),
            ScreenDestination(
                direction = MainFragmentDirections.openStandardGrid(),
                title = "Standard grid"
            ),
            ScreenDestination(
                direction = MainFragmentDirections.openList().apply { slowScroll = true },
                title = "Nested List Slow Scroll"
            ),
            ScreenDestination(
                direction = MainFragmentDirections.openList().apply { reverseLayout = true },
                title = "Nested Reversed list"
            ),
            ScreenDestination(
                direction = MainFragmentDirections.openStandardGrid()
                    .apply { reverseLayout = true },
                title = "Reversed grid"
            ),
            ScreenDestination(
                direction = MainFragmentDirections.openHorizontalLeanback(),
                title = "Horizontal Leanback comparison"
            ),
            ScreenDestination(
                direction = MainFragmentDirections.openComposeGrid(),
                title = "Compose grid"
            ),
        )
    }

}
