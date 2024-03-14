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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.rubensousa.dpadrecyclerview.compose.DpadAbstractComposeViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter


class ComposeItemAdapter(
    private val onItemClick: (Int) -> Unit = {}
) : MutableListAdapter<Int, ComposeItemAdapter.ComposeItemViewHolder>(
    MutableGridAdapter.DIFF_CALLBACK
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComposeItemViewHolder {
        return ComposeItemViewHolder(parent, onItemClick)
    }

    override fun onBindViewHolder(holder: ComposeItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.setItemState(item)
        holder.itemView.contentDescription = item.toString()
    }

    override fun getItemViewType(position: Int): Int {
        return ListTypes.ITEM
    }

    class ComposeItemViewHolder(
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
            ItemComposable(
                modifier = Modifier
                    .width(dimensionResource(id = R.dimen.list_item_width))
                    .aspectRatio(3 / 4f),
                item = item,
            )
        }

        override fun onFocusChanged(hasFocus: Boolean) {

        }

    }

}
