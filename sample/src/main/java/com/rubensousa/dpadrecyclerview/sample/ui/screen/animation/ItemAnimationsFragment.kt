package com.rubensousa.dpadrecyclerview.sample.ui.screen.animation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.screen.compose.ComposeItemAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class ItemAnimationsFragment : Fragment(R.layout.screen_recyclerview) {

    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)
    private val viewModel by viewModels<ItemAnimationsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemAdapter = ComposeItemAdapter()
        binding.recyclerView.apply {
            adapter = itemAdapter
        }
        viewModel.getItems().observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items)
        }
    }

    class ItemAnimationsViewModel : ViewModel() {

        private val totalItems = 25
        private val liveData = MutableLiveData<MutableList<Int>>()
        private val random = Random.Default
        private val itemsDispatched = mutableSetOf<Int>()

        init {
            viewModelScope.launch(Dispatchers.Default) {
                while (isActive) {
                    val newListSize = random.nextInt(totalItems)
                    repeat(newListSize) {
                        val item = random.nextInt(totalItems - 1)
                        itemsDispatched.add(item)
                    }
                    delay(250L)
                    liveData.postValue(itemsDispatched.toMutableList())
                    itemsDispatched.clear()
                }
            }
        }

        fun getItems(): LiveData<MutableList<Int>> = liveData
    }

}

