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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.rubensousa.dpadrecyclerview.sample.databinding.HorizontalAdapterListBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListModel
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.DpadStateHolder

class HorizontalListAdapter(
    private val stateHolder: DpadStateHolder,
    private val config: HorizontalListConfig
) : MutableListAdapter<ListModel, HorizontalListViewHolder>(DIFF_CALLBACK) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorizontalListViewHolder {
        return HorizontalListViewHolder(
            HorizontalAdapterListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            ),
            config
        )
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

    override fun onBindViewHolder(holder: HorizontalListViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        stateHolder.restore(holder.recyclerView, item.title, holder.adapter)
    }

    override fun onViewRecycled(holder: HorizontalListViewHolder) {
        val item = holder.item ?: return
        stateHolder.save(holder.recyclerView, item.title)
        holder.recycle()
    }

}