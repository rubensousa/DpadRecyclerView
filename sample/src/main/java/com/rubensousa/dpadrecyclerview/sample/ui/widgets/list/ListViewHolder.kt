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
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemNestedAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder

class ListViewHolder(view: View, itemLayoutId: Int = R.layout.adapter_nested_item_start) :
    RecyclerView.ViewHolder(view), DpadViewHolder {

    private val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
    private val textView = view.findViewById<TextView>(R.id.textView)
    private val adapter = ItemNestedAdapter(
        itemLayoutId,
        animateFocusChanges = itemLayoutId == R.layout.adapter_nested_item_start
    )
    private var key: String? = null

    init {
        itemView.isFocusable = true
        itemView.isFocusableInTouchMode = true
        itemView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                recyclerView.requestFocus()
            }
        }
        setupRecyclerView(recyclerView)
        onViewHolderDeselected()
    }

    fun bind(
        list: ListModel, stateHolder: DpadStateHolder,
        clickListener: ItemViewHolder.ItemClickListener
    ) {
        adapter.clickListener = clickListener
        key = list.title
        textView.text = list.title
        adapter.replaceList(list.items)
        recyclerView.adapter = adapter
       // stateHolder.register(recyclerView, list.title)
    }

    fun onRecycled(stateHolder: DpadStateHolder) {
        adapter.clickListener = null
        key?.let { scrollKey ->
          //  stateHolder.unregister(recyclerView, scrollKey)
        }
        recyclerView.adapter = null
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

    fun onAttachedToWindow() {}

    fun onDetachedFromWindow() {}

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
    }

}

