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

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import com.rubensousa.dpadrecyclerview.compose.DpadAbstractComposeViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ItemAnimator
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.GridItemComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter

class ComposeGridAdapter : MutableListAdapter<Int, ComposeGridAdapter.ComposeGridItemViewHolder>(
    MutableGridAdapter.DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ComposeGridItemViewHolder {
        return ComposeGridItemViewHolder(parent, onItemClick = {})
    }

    override fun onBindViewHolder(holder: ComposeGridItemViewHolder, position: Int) {
        holder.setItemState(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return ListTypes.ITEM
    }

    class ComposeGridItemViewHolder(
        parent: ViewGroup,
        onItemClick: (Int) -> Unit
    ) : DpadAbstractComposeViewHolder<Int>(parent) {

        init {
            itemView.setOnClickListener {
                getItem()?.let(onItemClick)
            }
        }

        @Composable
        override fun Content(item: Int, isFocused: Boolean, isSelected: Boolean) {
            GridItemComposable(item)
        }

        override fun onFocusChanged(hasFocus: Boolean) {

        }

    }

}