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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder

class NestedListAdapter(
    private val stateHolder: DpadStateHolder,
    private val onItemClickListener: ItemViewHolder.ItemClickListener
) : ListAdapter<ListModel, AbstractListViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListModel>() {
            override fun areItemsTheSame(oldItem: ListModel, newItem: ListModel): Boolean {
                return oldItem.title == newItem.title
            }

            override fun areContentsTheSame(oldItem: ListModel, newItem: ListModel): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractListViewHolder {
        val layoutId: Int
        val itemLayoutId: Int
        if (viewType == ListTypes.LIST_CENTER) {
            layoutId = R.layout.adapter_list_center
            itemLayoutId = R.layout.adapter_nested_item_center
        } else if (viewType == ListTypes.LIST_START) {
            layoutId = R.layout.adapter_list_start
            itemLayoutId = R.layout.adapter_nested_item_start
        } else {
            layoutId = R.layout.adapter_list_start_leanback
            itemLayoutId = R.layout.adapter_nested_item_start
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return if (viewType == ListTypes.LIST_START_LEANBACK) {
            LeanbackViewHolder(view, view.findViewById(R.id.recyclerView), itemLayoutId)
        } else {
            DpadListViewHolder(view, view.findViewById(R.id.recyclerView), itemLayoutId)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        if (item.centerAligned) {
            return ListTypes.LIST_CENTER
        }
        if (item.isLeanback) {
            return ListTypes.LIST_START_LEANBACK
        }
        return ListTypes.LIST_START
    }

    override fun onBindViewHolder(holder: AbstractListViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClickListener)
        if (holder is LeanbackViewHolder) {
            stateHolder.register(holder.horizontalGridView, item.title, holder.adapter)
        } else if (holder is DpadListViewHolder) {
            stateHolder.register(holder.dpadRecyclerView, item.title, holder.adapter)
        }
    }

    override fun onViewRecycled(holder: AbstractListViewHolder) {
        val item = holder.item ?: return
        if (holder is LeanbackViewHolder) {
            stateHolder.unregister(holder.horizontalGridView, item.title)
        } else if (holder is DpadListViewHolder) {
            stateHolder.unregister(holder.dpadRecyclerView, item.title)
        }
        holder.onRecycled()
    }

}