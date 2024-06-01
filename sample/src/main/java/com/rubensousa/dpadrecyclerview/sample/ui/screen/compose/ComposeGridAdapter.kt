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
import com.rubensousa.dpadrecyclerview.compose.DpadComposeFocusViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.GridItemComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter
import timber.log.Timber

class ComposeGridAdapter : MutableListAdapter<Int, DpadComposeFocusViewHolder<Int>>(
    MutableGridAdapter.DIFF_CALLBACK
) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DpadComposeFocusViewHolder<Int> {
        return DpadComposeFocusViewHolder(parent) { item, viewHolder ->
            GridItemComposable(
                item = item,
                onClick = {
                    Timber.i("Clicked: $item")
                }
            )
        }
    }

    override fun onBindViewHolder(holder: DpadComposeFocusViewHolder<Int>, position: Int) {
        holder.setItemState(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        return ListTypes.ITEM
    }

}