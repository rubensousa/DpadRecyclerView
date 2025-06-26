package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListModel
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
    private var enableLooping = false

    fun load(enableLooping: Boolean = false) {
        this.enableLooping = enableLooping
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
            delay(3000L)
            listLiveData.postValue(ArrayList(list))
        }.invokeOnCompletion { loadingStateLiveData.postValue(false) }

    }

    private fun createPage(): List<ListModel> {
        val titlePrefix = "DpadRecyclerView"
        return List(pageSize) { index ->
            generateList("$titlePrefix ${list.size + index}")
        }
    }

    private fun generateList(title: String): ListModel {
        val items = ArrayList<Int>()
        val itemCount = if (enableLooping) {
            8
        } else {
            20
        }
        repeat(itemCount) {
            items.add(it)
        }
        return ListModel(
            title, items,
            centerAligned = false,
            isLeanback = true,
            enableLooping = enableLooping
        )
    }


}
