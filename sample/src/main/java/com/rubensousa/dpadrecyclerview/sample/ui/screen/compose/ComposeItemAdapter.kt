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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rubensousa.dpadrecyclerview.compose.DpadComposeViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter


class ComposeItemAdapter(private val onItemClick: (Int) -> Unit) :
    MutableListAdapter<Int, DpadComposeViewHolder<Int>>(MutableGridAdapter.DIFF_CALLBACK) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DpadComposeViewHolder<Int> {
        return DpadComposeViewHolder(parent,
            composable = { item, isFocused, _ ->
                ItemComposable(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(9 / 16f),
                    item = item,
                    isFocused = isFocused
                )
            },
            onClick = onItemClick
        )
    }

    override fun onBindViewHolder(holder: DpadComposeViewHolder<Int>, position: Int) {
        holder.setItemState(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return ListTypes.ITEM
    }
}
