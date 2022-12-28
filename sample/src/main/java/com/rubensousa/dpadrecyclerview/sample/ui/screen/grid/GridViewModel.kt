package com.rubensousa.dpadrecyclerview.sample.ui.screen.grid

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GridViewModel : ViewModel() {

    private val list = ArrayList<Int>()
    private val listLiveData = MutableLiveData<MutableList<Int>>()
    private val loadingStateLiveData = MutableLiveData<Boolean>()
    private val pageSize = 5

    val loadingState: LiveData<Boolean> = loadingStateLiveData
    val listState: LiveData<MutableList<Int>> = listLiveData

    init {
        list.addAll(createPage())
        listLiveData.postValue(ArrayList(list))
    }

    fun loadMore(selectedPosition: Int, spanCount: Int) {
        if (loadingState.value == true) {
            return
        }
        val diffToEnd = list.size - selectedPosition
        if (diffToEnd > spanCount) {
            return
        }

        loadingStateLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            list.addAll(createPage())
            delay(2000L)
            listLiveData.postValue(ArrayList(list))
        }.invokeOnCompletion { loadingStateLiveData.postValue(false) }
    }

    private fun createPage(): List<Int> {
        return List(pageSize) { index -> list.size + index }
    }

}
