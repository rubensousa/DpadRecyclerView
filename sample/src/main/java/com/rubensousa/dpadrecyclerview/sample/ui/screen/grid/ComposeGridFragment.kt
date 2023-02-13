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
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.RecyclerViewLogger
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.DpadComposeViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.GridItemComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.PlaceholderAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import timber.log.Timber

class ComposeGridFragment : Fragment(R.layout.screen_recyclerview) {

    private val spanCount = 5
    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)
    private val viewModel by viewModels<GridViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(binding.recyclerView)
        val placeholderAdapter = PlaceholderAdapter(items = spanCount)
        val itemAdapter = ComposeGridAdapter()
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(false)
                .build()
        )
        concatAdapter.addAdapter(itemAdapter)
        concatAdapter.addAdapter(placeholderAdapter)

        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            placeholderAdapter.show(isLoading)
        }

        viewModel.listState.observe(viewLifecycleOwner) { list ->
            itemAdapter.submitList(list)
        }

        // For scaling animation
        binding.recyclerView.clipChildren = false
        binding.recyclerView.requestFocus()
        binding.recyclerView.adapter = concatAdapter
    }

    private fun setupRecyclerView(recyclerView: DpadRecyclerView) {
        recyclerView.apply {
            RecyclerViewLogger.logChildrenWhenIdle(this)
            setSpanCount(spanCount)
            addItemDecoration(
                DpadGridSpacingDecoration.create(
                    itemSpacing = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing)
                )
            )
            addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
                override fun onViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subPosition: Int
                ) {
                    viewModel.loadMore(position, spanCount)
                }
            })
        }
    }

    class ComposeGridAdapter : MutableListAdapter<Int, DpadComposeViewHolder<Int>>(
        MutableGridAdapter.DIFF_CALLBACK
    ) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DpadComposeViewHolder<Int> {
            return DpadComposeViewHolder(
                composeView = ComposeView(parent.context),
                composable = { item, isFocused, isSelected ->
                    GridItemComposable(item, isFocused)
                },
                onClick = { item ->
                    Timber.i("Clicked: $item")
                }
            )
        }

        override fun onBindViewHolder(holder: DpadComposeViewHolder<Int>, position: Int) {
            holder.bind(getItem(position))
        }

        override fun getItemViewType(position: Int): Int {
            return ListTypes.ITEM
        }
    }

}
