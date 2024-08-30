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

package com.rubensousa.dpadrecyclerview.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.SubPositionAlignment
import java.util.Collections

class DpadTestAdapter(
    private val showSubPositions: Boolean = false,
    private val onClick: (position: Int) -> Unit = {},
    private val onLongClick: (position: Int) -> Unit = {},
) :
    RecyclerView.Adapter<DpadTestAdapter.VH>() {

    private var items = mutableListOf<Item>()

    init {
        repeat(1000) { value ->
            items.add(Item(value))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val viewHolder = if (showSubPositions) {
            SubPositionVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.dpadrecyclerview_test_item_subposition, parent, false
                )
            )
        } else {
            SimpleVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.dpadrecyclerview_test_item_grid, parent, false
                )
            )
        }
        viewHolder.itemView.setOnClickListener {
            onClick(viewHolder.absoluteAdapterPosition)
        }
        viewHolder.itemView.setOnLongClickListener {
            onLongClick(viewHolder.absoluteAdapterPosition)
            true
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem() {
        items.add(Item(value = items.size))
        notifyItemInserted(items.size - 1)
    }

    fun removeItem() {
        items.removeLastOrNull()
        notifyItemRemoved(items.size)
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun moveLastItem() {
        Collections.swap(items, items.size - 1, 0)
        notifyItemMoved(items.size - 1, 0)
    }

    fun changeLastItem() {
        val lastItem = items.last()
        items[items.size - 1] = lastItem.copy(value = -lastItem.value)
        notifyItemChanged(items.size - 1)
    }

    data class Item(val value: Int)

    abstract class VH(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.isFocusable = true
            view.isFocusableInTouchMode = true
        }

        @CallSuper
        open fun bind(item: Item) {
            itemView.tag = item.value
        }
    }

    class SimpleVH(view: View) : VH(view) {

        private val textView: TextView = view.findViewById(R.id.textView)

        override fun bind(item: Item) {
            super.bind(item)
            textView.text = item.value.toString()
        }

    }

    class SubPositionVH(view: View) : VH(view), DpadViewHolder {

        private val alignments = ArrayList<SubPositionAlignment>()

        init {
            alignments.apply {
                add(
                    SubPositionAlignment(
                        offset = 0,
                        fraction = 0.5f,
                        alignmentViewId = R.id.subPosition0TextView,
                        focusViewId = R.id.subPosition0TextView
                    )
                )
                add(
                    SubPositionAlignment(
                        offset = 0,
                        fraction = 0.5f,
                        alignmentViewId = R.id.subPosition1TextView,
                        focusViewId = R.id.subPosition1TextView
                    )
                )
                add(
                    SubPositionAlignment(
                        offset = 0,
                        fraction = 0.5f,
                        alignmentViewId = R.id.subPosition2TextView,
                        focusViewId = R.id.subPosition2TextView
                    )
                )
            }
        }

        override fun getSubPositionAlignments(): List<SubPositionAlignment> {
            return alignments
        }

    }


}