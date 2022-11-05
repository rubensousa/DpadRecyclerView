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

package com.rubensousa.dpadrecyclerview.test

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PaginationViewModel : ViewModel() {

    private var loadOffset = 2
    private var loadDelay = 0L
    private val list = ArrayList<Int>()
    private val listLiveData = MutableLiveData<List<Int>>()
    private val loadingStateLiveData = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = loadingStateLiveData
    val listState: LiveData<List<Int>> = listLiveData
    private var items = 0

    fun setLoadDelay(delay: Long) {
        loadDelay = delay
    }

    fun initialLoad(numberOfItems: Int) {
        items = numberOfItems
        repeat(numberOfItems) {
            list.add(it)
        }
        listLiveData.postValue(ArrayList(list))
    }

    fun loadMore(selectedPosition: Int) {
        if (loadingState.value == true) {
            return
        }
        val diffToEnd = list.size - selectedPosition
        if (diffToEnd >= loadOffset) {
            return
        }
        loadingStateLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.IO) {
            repeat(items) {
                list.add(list.size)
            }
            delay(loadDelay)
            listLiveData.postValue(ArrayList(list))
        }.invokeOnCompletion { loadingStateLiveData.postValue(false) }

    }

}
