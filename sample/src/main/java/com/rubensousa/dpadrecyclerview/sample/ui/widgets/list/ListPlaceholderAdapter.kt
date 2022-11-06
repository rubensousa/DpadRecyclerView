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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.sample.R

class ListPlaceholderAdapter(
    private val items: Int = 1,
    private val layoutId: Int = R.layout.adapter_list_placeholder,
    private val focusPlaceholders: Boolean = false
) : RecyclerView.Adapter<ListPlaceholderAdapter.VH>() {

    private var show = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(
            layoutId, parent, false
        )
        view.isFocusable = focusPlaceholders
        view.isFocusableInTouchMode = focusPlaceholders
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

    }

    fun show(enabled: Boolean) {
        if (enabled == show) {
            return
        }
        show = enabled
        if (show) {
            notifyItemRangeInserted(0, items)
        } else {
            notifyItemRangeRemoved(0, items)
        }
    }

    fun isShowing() = show

    override fun getItemCount(): Int {
        return if (show) {
            items
        } else {
            0
        }
    }

    override fun getItemViewType(position: Int): Int {
        return ListTypes.LOADING
    }

    class VH(view: View) : RecyclerView.ViewHolder(view)

}
