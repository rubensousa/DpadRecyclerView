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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.dynamic

import android.view.LayoutInflater
import android.view.ViewGroup
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListMediumBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.RecyclerViewItem
import com.rubensousa.dpadrecyclerview.sample.ui.model.ViewHolderDelegate
import com.rubensousa.dpadrecyclerview.state.DpadScrollState

class MediumListDelegate(
    private val viewPool: UnboundViewPool,
    private val scrollState: DpadScrollState,
) : ViewHolderDelegate<MediumList, MediumListViewHolder> {

    override fun onCreateViewHolder(parent: ViewGroup): MediumListViewHolder {
        return MediumListViewHolder(
            viewPool,
            AdapterListMediumBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: MediumListViewHolder, item: MediumList) {
        super.onBindViewHolder(holder, item)
        scrollState.restore(
            recyclerView = holder.recyclerView,
            key = item.getDiffId(),
            adapter = holder.adapter
        )
    }

    override fun onViewRecycled(holder: MediumListViewHolder) {
        super.onViewRecycled(holder)
        holder.item?.let { item ->
            scrollState.save(holder.recyclerView, item.getDiffId())
        }
        holder.onRecycled()
    }

    override fun matches(item: RecyclerViewItem): Boolean = item is MediumList

}
