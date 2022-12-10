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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.main

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.DecorationLookup
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ViewHolderTask
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.DpadStateHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.ListModel
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.ListPlaceholderAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.NestedListAdapter
import timber.log.Timber

class MainListController(private val fragment: Fragment) {

    private var selectedPosition = RecyclerView.NO_POSITION
    private val loadingAdapter = ListPlaceholderAdapter()
    private val scrollStateHolder = DpadStateHolder()
    private val nestedListAdapter = NestedListAdapter(scrollStateHolder,
        object : ItemViewHolder.ItemClickListener {
            override fun onViewHolderClicked() {
                fragment.findNavController().navigate(R.id.open_detail)
            }
        })
    private var dpadRecyclerView: DpadRecyclerView? = null

    fun setup(
        recyclerView: DpadRecyclerView,
        lifecycleOwner: LifecycleOwner,
        onSelected: (position: Int) -> Unit
    ) {
        dpadRecyclerView = recyclerView
        setupAdapter(recyclerView)
        setupSpacings(recyclerView)
        setupPagination(recyclerView, onSelected)
        setupLifecycle(lifecycleOwner)

        if (selectedPosition != RecyclerView.NO_POSITION) {
            recyclerView.setSelectedPosition(
                selectedPosition, object : ViewHolderTask() {
                    override fun execute(viewHolder: RecyclerView.ViewHolder) {
                        Timber.d("Selection state restored")
                    }
                })
        }

        recyclerView.requestFocus()
    }

    fun submitList(list: List<ListModel>) {
        nestedListAdapter.submitList(list) {
            dpadRecyclerView?.invalidateItemDecorations()
        }
    }

    fun showLoading(isLoading: Boolean) {
        loadingAdapter.show(isLoading)
    }

    private fun setupAdapter(recyclerView: DpadRecyclerView) {
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(true)
                .build()
        )
        concatAdapter.addAdapter(nestedListAdapter)
        concatAdapter.addAdapter(loadingAdapter)
        nestedListAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        recyclerView.adapter = concatAdapter
    }

    private fun setupLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                dpadRecyclerView?.adapter = null
                dpadRecyclerView = null
            }
        })
    }

    private fun setupPagination(
        recyclerView: DpadRecyclerView,
        onSelected: (position: Int) -> Unit
    ) {
        recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                selectedPosition = position
                onSelected(position)
                Timber.d("Selected: $position, $subPosition")
            }

            override fun onViewHolderSelectedAndAligned(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                Timber.d("Aligned: $position, $subPosition")
            }
        })
    }

    private fun setupSpacings(recyclerView: DpadRecyclerView) {
        recyclerView.addItemDecoration(
            LinearMarginDecoration.createVertical(
                verticalMargin = recyclerView.resources.getDimensionPixelOffset(
                    R.dimen.item_spacing
                )
            )
        )
    }

}
