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

package com.rubensousa.dpadrecyclerview.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.test.tests.AbstractTestAdapter
import com.rubensousa.dpadrecyclerview.testing.R

class TestAdapter(
    private val adapterConfiguration: TestAdapterConfiguration,
) : AbstractTestAdapter<TestAdapter.ItemViewHolder>(adapterConfiguration) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(adapterConfiguration.itemLayoutId, parent, false)
        ).also { viewHolder ->
            if (adapterConfiguration.height != 0) {
                val layoutParams = viewHolder.itemView.layoutParams
                layoutParams.height = adapterConfiguration.height
                viewHolder.itemView.layoutParams = layoutParams
            }
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
        val isFocusable = position % adapterConfiguration.focusEvery == 0
        holder.itemView.isFocusable = isFocusable
        holder.itemView.isFocusableInTouchMode = isFocusable
    }

    class ItemViewHolder(view: View) : TestViewHolder(view), DpadViewHolder {

        private val textView = view.findViewById<TextView>(R.id.textView)

        fun bind(index: Int) {
            textView.text = index.toString()
        }

    }

}
