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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.common

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.compose.DpadComposeViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes

class ComposePlaceholderAdapter(
    private val items: Int = 1,
    private val composable: @Composable () -> Unit
) : RecyclerView.Adapter<DpadComposeViewHolder<Boolean>>() {

    companion object {

        fun grid(spanCount: Int): ComposePlaceholderAdapter {
            return ComposePlaceholderAdapter(
                items = spanCount,
                composable = { GridPlaceholderComposable() }
            )
        }
    }

    private var show = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DpadComposeViewHolder<Boolean> {
        return DpadComposeViewHolder(
            parent = parent,
            isFocusable = false
        ) { _, _, _ ->
            composable()
        }
    }

    override fun onBindViewHolder(holder: DpadComposeViewHolder<Boolean>, position: Int) {
        holder.setItemState(true)
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

}
