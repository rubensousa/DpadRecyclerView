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

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.RecyclerViewLogger
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemNestedAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder

abstract class AbstractListViewHolder(
    view: View,
    val recyclerView: RecyclerView,
    itemLayoutId: Int = R.layout.adapter_nested_item_start
) : RecyclerView.ViewHolder(view), DpadViewHolder {

    var item: ListModel? = null
    val textView = view.findViewById<TextView>(R.id.textView)
    val adapter = ItemNestedAdapter(
        itemLayoutId,
        animateFocusChanges = itemLayoutId == R.layout.adapter_nested_item_start
    )

    init {
        itemView.isFocusable = true
        itemView.isFocusableInTouchMode = true
        itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                recyclerView.requestFocus()
            }
        }
        setupRecyclerView(recyclerView)
    }

    fun bind(list: ListModel, clickListener: ItemViewHolder.ItemClickListener) {
        item = list
        adapter.clickListener = clickListener
        textView.text = list.title
        adapter.replaceList(list.items)
        recyclerView.adapter = adapter
    }

    fun onRecycled() {
        item = null
        adapter.clickListener = null
    }

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        recyclerView.alpha = 1.0f
        textView.alpha = 1.0f
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        recyclerView.alpha = 0.5f
        textView.alpha = 0.5f
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        recyclerView.apply {
            addItemDecoration(
                LinearMarginDecoration.createHorizontal(
                    horizontalMargin = itemView.resources.getDimensionPixelOffset(
                        R.dimen.item_spacing
                    ) / 2
                )
            )
        }
        RecyclerViewLogger.logChildrenWhenIdle(recyclerView)
    }

}

