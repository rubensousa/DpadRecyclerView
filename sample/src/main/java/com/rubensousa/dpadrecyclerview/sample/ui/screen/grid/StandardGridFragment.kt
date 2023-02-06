/*
 * Copyright 2022 Rúben Sousa
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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.leanback.widget.OnChildViewHolderSelectedListener
import androidx.leanback.widget.VerticalGridView
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.ColumnProvider
import com.rubensousa.decorator.GridMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenStandardGridBinding
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.RecyclerViewLogger
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.PlaceholderAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import timber.log.Timber

class StandardGridFragment : Fragment(R.layout.screen_standard_grid) {

    private val spanCount = 5
    private val binding by viewBinding(ScreenStandardGridBinding::bind)
    private val viewModel by viewModels<GridViewModel>()
    private val args by navArgs<StandardGridFragmentArgs>()
    private lateinit var placeholderAdapter: PlaceholderAdapter
    private lateinit var itemAdapter: MutableGridAdapter
    private lateinit var concatAdapter: ConcatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        concatAdapter = buildAdapter(spanCount)
        setupVerticalGridView(binding.verticalGridView)
        setupDpadRecyclerView(binding.dpadRecyclerView)
        viewModel.listState.observe(viewLifecycleOwner) { state ->
            itemAdapter.submitList(state)
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            placeholderAdapter.show(isLoading)
            binding.verticalGridView.invalidateItemDecorations()
        }
        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (checkedId == R.id.dpadButton && isChecked) {
                showDpadRecyclerView()
            } else if (checkedId == R.id.leanbackButton && isChecked) {
                showVerticalGridView()
            }
        }
        binding.dpadRecyclerView.requestFocus()
        binding.dpadRecyclerView.adapter = concatAdapter
    }

    private fun showDpadRecyclerView() {
        binding.verticalGridView.adapter = null
        binding.dpadRecyclerView.adapter = concatAdapter
        binding.verticalGridView.isVisible = false
        binding.dpadRecyclerView.isVisible = true
        binding.dpadRecyclerView.requestFocus()
    }

    private fun showVerticalGridView() {
        binding.dpadRecyclerView.adapter = null
        binding.verticalGridView.adapter = concatAdapter
        binding.dpadRecyclerView.isVisible = false
        binding.verticalGridView.isVisible = true
        binding.verticalGridView.requestFocus()
    }

    private fun buildAdapter(spanCount: Int): ConcatAdapter {
        placeholderAdapter = PlaceholderAdapter(items = spanCount)
        itemAdapter = MutableGridAdapter(object : ItemViewHolder.ItemClickListener {
            override fun onViewHolderClicked() {

            }
        })
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(false)
                .build()
        )
        concatAdapter.addAdapter(itemAdapter)
        concatAdapter.addAdapter(placeholderAdapter)
        return concatAdapter
    }

    private fun setupVerticalGridView(recyclerView: VerticalGridView) {
        recyclerView.apply {
            addItemDecoration(
                GridMarginDecoration(
                    horizontalMargin = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing),
                    verticalMargin = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing),
                    columnProvider = object : ColumnProvider {
                        override fun getNumberOfColumns(): Int = spanCount
                    }
                )
            )
            recyclerView.setNumColumns(spanCount)
            addOnChildViewHolderSelectedListener(object : OnChildViewHolderSelectedListener() {
                override fun onChildViewHolderSelected(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int
                ) {
                    Timber.i("Selected: $position")
                    viewModel.loadMore(position, spanCount)
                }

                override fun onChildViewHolderSelectedAndPositioned(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subposition: Int
                ) {
                    Timber.i("Aligned: $position")
                }
            })
        }
    }

    private fun setupDpadRecyclerView(recyclerView: DpadRecyclerView) {
        recyclerView.apply {
            RecyclerViewLogger.logChildrenWhenIdle(this)
            setReverseLayout(args.reverseLayout)
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
                    Timber.i("Selected: $position")
                    viewModel.loadMore(position, spanCount)
                }

                override fun onViewHolderSelectedAndAligned(
                    parent: RecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subPosition: Int
                ) {
                    Timber.i("Aligned: $position")
                }
            })
        }
    }

}