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
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder

class NestedListAdapter(
    private val stateHolder: DpadStateHolder,
    private val onItemClickListener: ItemViewHolder.ItemClickListener) :
    ListAdapter<ListModel, ListViewHolder>(DIFF_CALLBACK) {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        return ListViewHolder(
            AdapterListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position), stateHolder, onItemClickListener)
    }

    override fun onViewRecycled(holder: ListViewHolder) {
        super.onViewRecycled(holder)
        holder.onRecycled(stateHolder)
    }

    override fun onViewAttachedToWindow(holder: ListViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: ListViewHolder) {
        super.onViewDetachedFromWindow(holder)
        holder.onDetachedFromWindow()
    }


}