package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.ListModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ListViewModel : ViewModel() {

    private val list = ArrayList<ListModel>()
    private val listLiveData = MutableLiveData<MutableList<ListModel>>()
    private val loadingStateLiveData = MutableLiveData<Boolean>()
    private val pageSize = 10
    val loadingState: LiveData<Boolean> = loadingStateLiveData
    val listState: LiveData<MutableList<ListModel>> = listLiveData

    init {
        list.addAll(createPage())
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
            list.addAll(createPage())
            delay(2000L)
            listLiveData.postValue(ArrayList(list))
        }.invokeOnCompletion { loadingStateLiveData.postValue(false) }

    }

    private fun createPage(leanback: Boolean = false): List<ListModel> {
        val titlePrefix = if (leanback) {
            "HorizontalGridView"
        } else {
            "DpadRecyclerView"
        }
        return List(pageSize) { index ->
            generateList("$titlePrefix ${list.size + index}", leanback)
        }
    }

    private fun generateList(title: String, leanback: Boolean): ListModel {
        val items = ArrayList<Int>()
        repeat(20) {
            items.add(it)
        }
        return ListModel(title, items, centerAligned = false, isLeanback = leanback)
    }


}
