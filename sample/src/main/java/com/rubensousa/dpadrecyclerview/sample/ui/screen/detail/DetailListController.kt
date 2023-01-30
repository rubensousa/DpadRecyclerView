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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.detail

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemGridAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.ListHeaderAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.PlaceholderAdapter

class DetailListController {

    private val loadingAdapter = PlaceholderAdapter(
        items = 5,
        layoutId = R.layout.adapter_grid_placeholder,
        focusPlaceholders = false
    )
    private val itemAdapter = ItemGridAdapter(object : ItemViewHolder.ItemClickListener {
        override fun onViewHolderClicked() {
        }
    })
    private var dpadRecyclerView: DpadRecyclerView? = null
    private val alignment = DetailListAlignment()
    private val headerAdapter = ListHeaderAdapter()

    init {
        headerAdapter.submitList(listOf("Header"))
    }

    fun setup(
        recyclerView: DpadRecyclerView,
        lifecycleOwner: LifecycleOwner,
        onSelected: (position: Int) -> Unit
    ) {
        dpadRecyclerView = recyclerView
        setupLayout(recyclerView)
        setupLifecycle(lifecycleOwner)
        setupAdapter(recyclerView)
        setupAlignment(recyclerView)
        setupPagination(recyclerView, onSelected)
        alignment.alignToTop(recyclerView)
        recyclerView.requestFocus()
    }

    fun showLoading(isLoading: Boolean) {
        loadingAdapter.show(isLoading)
    }

    fun submitList(list: List<Int>) {
        itemAdapter.submitList(list)
    }

    fun scrollToNext() {
        dpadRecyclerView?.let { recyclerView ->
            val subPosition = recyclerView.getSelectedSubPosition()
            val subPositionCount = recyclerView.getCurrentSubPositions()
            if (subPosition < subPositionCount - 1) {
                recyclerView.setSelectedSubPositionSmooth(subPosition + 1)
            } else {
                recyclerView.setSelectedPositionSmooth(recyclerView.getSelectedPosition() + 1)
            }
        }
    }

    fun scrollToPrevious() {
        dpadRecyclerView?.let { recyclerView ->
            val subPosition = recyclerView.getSelectedSubPosition()
            if (subPosition > 0) {
                recyclerView.setSelectedSubPositionSmooth(subPosition - 1)
            } else {
                if (recyclerView.getSelectedPosition() == headerAdapter.itemCount) {
                    recyclerView.setSelectedSubPositionSmooth(
                        headerAdapter.itemCount - 1, headerAdapter.itemCount
                    )
                } else {
                    recyclerView.setSelectedPositionSmooth(recyclerView.getSelectedPosition() - 1)
                }
            }
        }
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

    private fun setupLayout(recyclerView: DpadRecyclerView) {
        recyclerView.setSpanSizeLookup(object : DpadSpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < headerAdapter.itemCount) {
                    recyclerView.getSpanCount()
                } else {
                    1
                }
            }
        })
    }

    private fun setupPagination(
        recyclerView: DpadRecyclerView,
        onSelected: (position: Int) -> Unit
    ) {
        recyclerView.addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                onSelected(position)
            }
        })
    }

    private fun setupAdapter(recyclerView: DpadRecyclerView) {
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(false)
                .build()
        )
        concatAdapter.addAdapter(headerAdapter)
        concatAdapter.addAdapter(itemAdapter)
        concatAdapter.addAdapter(loadingAdapter)
        recyclerView.adapter = concatAdapter
    }

    private fun setupAlignment(recyclerView: DpadRecyclerView) {
        recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                if (position >= headerAdapter.itemCount + recyclerView.getSpanCount()) {
                    alignment.alignToCenter(recyclerView)
                } else {
                    alignment.alignToTop(recyclerView)
                }
            }
        })
    }

}
