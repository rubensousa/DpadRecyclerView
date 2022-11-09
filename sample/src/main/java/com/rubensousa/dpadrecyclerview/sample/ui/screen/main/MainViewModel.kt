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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.ListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val list = ArrayList<ListModel>()
    private val listLiveData = MutableLiveData<List<ListModel>>()
    private val loadingStateLiveData = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> = loadingStateLiveData
    val listState: LiveData<List<ListModel>> = listLiveData

    init {
        appendNewPage()
        listLiveData.postValue(ArrayList(list))
    }

    fun loadMore(selectedPosition: Int) {
        if (loadingState.value == true) {
            return
        }
        val diffToEnd = list.size - selectedPosition
        if (diffToEnd >= 2) {
            return
        }

        loadingStateLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            appendNewPage()
            delay(1000L)
            listLiveData.postValue(ArrayList(list))
        }.invokeOnCompletion { loadingStateLiveData.postValue(false) }

    }

    private fun appendNewPage() {
        repeat(3) {
            list.add(generateList("List ${list.size}", centerAligned = true))
            list.add(generateList("List ${list.size}"))
            list.add(generateList("List ${list.size}"))
        }
    }

    private fun generateList(title: String, centerAligned: Boolean = false): ListModel {
        val items = ArrayList<Int>()
        repeat(100) {
            items.add(it)
        }
        return ListModel(title, items, centerAligned)
    }


}
