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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListComposeBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListModel
import com.rubensousa.dpadrecyclerview.sample.ui.model.RecyclerViewItem
import com.rubensousa.dpadrecyclerview.sample.ui.model.ViewHolderDelegate
import com.rubensousa.dpadrecyclerview.state.DpadScrollState

class NestedComposeListDelegate(
    private val scrollState: DpadScrollState,
    private val viewPool: UnboundViewPool,
) : ViewHolderDelegate<ListModel, NestedComposeListViewHolder> {

    override fun onCreateViewHolder(parent: ViewGroup): NestedComposeListViewHolder {
        return NestedComposeListViewHolder(
            AdapterListComposeBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ),
            viewPool
        )
    }

    override fun matches(item: RecyclerViewItem): Boolean = item is ListModel

    override fun onBindViewHolder(holder: NestedComposeListViewHolder, item: ListModel) {
        holder.bind(item)
        scrollState.restore(
            recyclerView = holder.getRecyclerView(),
            key = item.title,
            adapter = holder.getAdapter()
        )
    }

    override fun onViewRecycled(holder: NestedComposeListViewHolder) {
        holder.item?.let {
            scrollState.save(holder.getRecyclerView(), it.getDiffId())
        }
    }

    override fun onViewDetached(holder: NestedComposeListViewHolder) {
        holder.cancelAnimations()
    }

}
