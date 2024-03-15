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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.compose

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.screen.list.ListViewModel
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.RecyclerViewLogger
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ComposePlaceholderAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration
import com.rubensousa.dpadrecyclerview.state.DpadStateRegistry


class ComposeListFragment : Fragment(R.layout.screen_recyclerview) {

    private val viewModel by viewModels<ListViewModel>()
    private val stateRegistry = DpadStateRegistry()
    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(binding.recyclerView)
        val placeholderAdapter = ComposePlaceholderAdapter(
            composable = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(64.dp)
                            .padding(top = 24.dp),
                        color = Color.White
                    )
                }
            }
        )
        val itemAdapter = NestedComposeListAdapter(stateRegistry.getScrollState())
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

        binding.recyclerView.apply {
            setParentAlignment(
                alignment = ParentAlignment(
                    fraction = 0.15f,
                    edge = ParentAlignment.Edge.NONE
                )
            )
            setChildAlignment(alignment = ChildAlignment(fraction = 0.0f))
            setSmoothScrollMaxPendingMoves(0)
            adapter = concatAdapter
            requestFocus()
        }
    }

    private fun setupRecyclerView(recyclerView: DpadRecyclerView) {
        recyclerView.apply {
            RecyclerViewLogger.logChildrenWhenIdle(this)
            addItemDecoration(
                DpadLinearSpacingDecoration.create(
                    itemSpacing = resources.getDimensionPixelOffset(R.dimen.vertical_item_spacing)
                )
            )
            addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
                override fun onViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subPosition: Int
                ) {
                    viewModel.loadMore(position)
                }
            })
        }
    }

}
