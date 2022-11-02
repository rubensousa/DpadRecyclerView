package com.rubensousa.dpadrecyclerview.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val list = ArrayList<Int>()
    private val listLiveData = MutableLiveData<List<Int>>()
    private val loadingStateLiveData = MutableLiveData<Boolean>()
    private val pageSize = 25
    val loadingState: LiveData<Boolean> = loadingStateLiveData
    val listState: LiveData<List<Int>> = listLiveData

    init {
        repeat(pageSize) {
            list.add(list.size)
        }
        listLiveData.postValue(ArrayList(list))
    }

    fun loadMore(selectedPosition: Int) {
        if (loadingState.value == true) {
            return
        }
        val diffToEnd = list.size - selectedPosition
        if (diffToEnd >= 10) {
            return
        }

        loadingStateLiveData.postValue(true)
        viewModelScope.launch(Dispatchers.Default) {
            repeat(pageSize) {
                list.add(list.size)
            }
            delay(1000L)
            listLiveData.postValue(ArrayList(list))
            loadingStateLiveData.postValue(false)
        }

    }

}
