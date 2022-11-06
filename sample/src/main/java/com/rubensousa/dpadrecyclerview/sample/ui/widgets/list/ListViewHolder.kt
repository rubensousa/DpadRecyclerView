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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.list

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemNestedAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder

class ListViewHolder(private val binding: AdapterListBinding) :
    RecyclerView.ViewHolder(binding.root), DpadViewHolder {

    private val adapter = ItemNestedAdapter()
    private var key: String? = null

    init {
        itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                binding.recyclerView.requestFocus()
            }
        }
        setupRecyclerView(binding.recyclerView)
        onViewHolderDeselected()
    }

    fun bind(
        list: ListModel, stateHolder: DpadStateHolder,
        clickListener: ItemViewHolder.ItemClickListener
    ) {
        adapter.clickListener = clickListener
        key = list.title
        binding.textView.text = list.title
        adapter.replaceList(list.items)
        binding.recyclerView.adapter = adapter
        stateHolder.register(binding.recyclerView, list.title)
    }

    fun onRecycled(stateHolder: DpadStateHolder) {
        adapter.clickListener = null
        key?.let { scrollKey ->
            stateHolder.unregister(binding.recyclerView, scrollKey)
        }
        binding.recyclerView.adapter = null
    }

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        binding.recyclerView.alpha = 1.0f
        binding.textView.alpha = 1.0f
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        binding.recyclerView.alpha = 0.5f
        binding.textView.alpha = 0.5f
    }

    fun onAttachedToWindow() {}

    fun onDetachedFromWindow() {}

    private fun setupRecyclerView(recyclerView: DpadRecyclerView) {
        recyclerView.apply {
            addItemDecoration(
                LinearMarginDecoration.createHorizontal(
                    horizontalMargin = binding.root.context.resources.getDimensionPixelOffset(
                        R.dimen.item_spacing
                    ) / 2
                )
            )
        }
    }

}

