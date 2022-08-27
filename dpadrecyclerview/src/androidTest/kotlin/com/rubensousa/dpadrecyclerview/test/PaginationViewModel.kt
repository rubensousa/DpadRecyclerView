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
