package com.rubensousa.dpadrecyclerview.sample.ui.screen.animation

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.compose.ComposeViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.PlaceholderComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PredictiveAnimationFragment : Fragment(R.layout.screen_recyclerview) {

    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)
    private val viewModel by viewModels<PredictiveAnimationViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemAdapter = PredictiveItemAdapter()
        binding.recyclerView.apply {
            adapter = itemAdapter
            addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
                override fun onViewHolderSelected(
                    parent: DpadRecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subPosition: Int,
                ) {
                    viewModel.loadMore(position)
                }
            })
            requestFocus()
        }
        viewModel.getItems().observe(viewLifecycleOwner) { items ->
            itemAdapter.submitList(items)
        }
    }

    class PredictiveItemAdapter(
    ) : MutableListAdapter<Int, ComposeViewHolder<Int>>(MutableGridAdapter.DIFF_CALLBACK) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int,
        ): ComposeViewHolder<Int> {
            return when (viewType) {
                ListTypes.ITEM -> {
                    ComposeViewHolder(parent) { item ->
                        ItemComposable(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            item = item,
                        )
                    }
                }

                else -> {
                    ComposeViewHolder(
                        parent,
                        isFocusable = false
                    ) {
                        PlaceholderComposable(
                            Modifier.fillMaxWidth().height(300.dp),
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: ComposeViewHolder<Int>, position: Int) {
            val item = getItem(position)
            holder.setItemState(item)
            holder.itemView.contentDescription = item.toString()
        }

        override fun getItemViewType(position: Int): Int {
            val item = getItem(position)
            return if (item >= 0) {
                ListTypes.ITEM
            } else {
                ListTypes.LOADING
            }
        }

    }


    class PredictiveAnimationViewModel : ViewModel() {

        private val totalItems = 10
        private var offset = 0
        private var isLoadingMore = false
        private val liveData = MutableLiveData<MutableList<Int>>()
        private val dispatcher = Dispatchers.Default

        init {
            loadFirstPage()
        }

        fun getItems(): LiveData<MutableList<Int>> = liveData

        private fun loadFirstPage() {
            viewModelScope.launch(dispatcher) {
                val newList = mutableListOf<Int>()
                repeat(totalItems) {
                    newList.add(it)
                }
                liveData.postValue(newList.toMutableList())
                offset = totalItems
            }
        }

        fun loadMore(selectedPosition: Int) {
            if (isLoadingMore) {
                return
            }
            if (selectedPosition < offset - 2) {
                return
            }
            isLoadingMore = true
            viewModelScope.launch(dispatcher) {
                val currentList = liveData.value!!
                val loadingList = currentList + mutableListOf(-1)
                liveData.postValue(loadingList.toMutableList())
                delay(5000L)

                val newList = MutableList(totalItems + offset) { it }
                liveData.postValue(newList)
                offset = newList.size
                isLoadingMore = false
            }
        }
    }

}
