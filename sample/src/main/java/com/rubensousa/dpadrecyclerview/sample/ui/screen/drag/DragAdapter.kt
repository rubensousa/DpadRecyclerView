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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.drag

import android.view.ViewGroup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadDragHelper
import com.rubensousa.dpadrecyclerview.compose.DpadComposeFocusViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter
import kotlinx.coroutines.flow.StateFlow

class DragAdapter(
    private val dragState: StateFlow<Int?>,
    private val onDragStart: (viewHolder: RecyclerView.ViewHolder) -> Unit,
    private val gridLayout: Boolean = false
) : MutableListAdapter<Int, DpadComposeFocusViewHolder<Int>>(MutableGridAdapter.DIFF_CALLBACK),
    DpadDragHelper.DragAdapter<Int> {

    override fun getMutableItems(): MutableList<Int> = items

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DpadComposeFocusViewHolder<Int> {
        val viewHolder = DpadComposeFocusViewHolder<Int>(parent)
        viewHolder.setContent { item ->
            if (gridLayout) {
                DraggableGridItem(
                    item = item,
                    isDragging = dragState.collectAsStateWithLifecycle().value == item,
                    onClick = {
                        onDragStart(viewHolder)
                    }
                )
            } else {
                DraggableItem(
                    item = item,
                    isDragging = dragState.collectAsStateWithLifecycle().value == item,
                    onClick = {
                        onDragStart(viewHolder)
                    }
                )
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: DpadComposeFocusViewHolder<Int>, position: Int) {
        val item = getItem(position)
        holder.setItemState(item)
        holder.itemView.contentDescription = item.toString()
    }


    override fun getItemViewType(position: Int): Int {
        return ListTypes.ITEM
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("[")
        for (i in 0 until itemCount) {
            builder.append(getItem(i))
            if (i < itemCount - 1) {
                builder.append(", ")
            }
        }
        builder.append("]")
        return builder.toString()
    }

}