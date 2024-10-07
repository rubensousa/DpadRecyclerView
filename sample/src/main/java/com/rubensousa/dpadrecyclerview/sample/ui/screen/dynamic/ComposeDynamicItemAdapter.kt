/*
 * Copyright 2024 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.dynamic

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.rubensousa.dpadrecyclerview.compose.DpadComposeFocusViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.model.ComposableItem

class ComposeDynamicItemAdapter :
    ListAdapter<ComposableItem, DpadComposeFocusViewHolder<ComposableItem>>(
        object : DiffUtil.ItemCallback<ComposableItem?>() {
            override fun areItemsTheSame(
                oldItem: ComposableItem,
                newItem: ComposableItem
            ): Boolean {
                return oldItem.isItemTheSame(newItem)
            }

            override fun areContentsTheSame(
                oldItem: ComposableItem,
                newItem: ComposableItem
            ): Boolean {
                return oldItem.areContentsTheSame(newItem)
            }
        }
    ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DpadComposeFocusViewHolder<ComposableItem> {
        return DpadComposeFocusViewHolder(parent) { item ->
            item.Content()
        }
    }

    override fun onBindViewHolder(
        holder: DpadComposeFocusViewHolder<ComposableItem>,
        position: Int
    ) {
        holder.setItemState(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position)::class.java.hashCode()
    }

}
