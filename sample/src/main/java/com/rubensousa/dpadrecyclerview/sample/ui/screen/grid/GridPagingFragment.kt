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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.grid

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.PagingDataAdapter
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterItemGridBinding
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GridPagingFragment : Fragment(R.layout.screen_recyclerview) {

    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)
    private val viewModel by viewModels<GridPagingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pagingAdapter = PagingAdapter(object : GridItemViewHolder.ItemClickListener {
            override fun onViewHolderClicked() {

            }
        })
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.items.collectLatest { items ->
                    pagingAdapter.submitData(items)
                }
            }
        }
        binding.recyclerView.apply {
            setSpanCount(5)
            itemAnimator = null
            addItemDecoration(
                DpadGridSpacingDecoration.create(
                    itemSpacing = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing)
                )
            )
            adapter = pagingAdapter
            requestFocus()
        }
    }

    class PagingAdapter(
        private val onItemClickListener: GridItemViewHolder.ItemClickListener
    ) : PagingDataAdapter<Int, GridItemViewHolder>(MutableGridAdapter.DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridItemViewHolder {
            return GridItemViewHolder(
                AdapterItemGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: GridItemViewHolder, position: Int) {
            getItem(position)?.let { item ->
                holder.bind(item, onItemClickListener)
            }
        }
    }

}

