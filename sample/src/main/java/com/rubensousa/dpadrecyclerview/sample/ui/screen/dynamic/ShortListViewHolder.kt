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

import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.MainAdapterListFeatureBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.DelegateViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ListAnimator

class ShortListViewHolder(
    private val recycledViewPool: UnboundViewPool,
    private val binding: MainAdapterListFeatureBinding,
) : DelegateViewHolder<ShortList>(binding.root), DpadViewHolder {

    val recyclerView = binding.featureRecyclerView
    val adapter = ComposeDynamicItemAdapter()

    var item: ShortList? = null
        private set

    private val animator = ListAnimator(recyclerView, binding.featureGroupTextView)

    init {
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemSpacing(
            itemView.resources.getDimensionPixelOffset(
                R.dimen.feature_item_spacing
            ),
        )
        recyclerView.setItemEdgeSpacing(
            itemView.resources.getDimensionPixelOffset(
                R.dimen.feature_edge_spacing
            ),
        )
        recyclerView.setRecycledViewPool(recycledViewPool)
        recyclerView.adapter = adapter
    }

    override fun bind(item: ShortList) {
        this.item = item
        recyclerView.isFocusable = item.items.isNotEmpty()
        recyclerView.isFocusableInTouchMode = recyclerView.isFocusable
        adapter.replaceList(item.items)
        binding.featureGroupTextView.text = item.title
    }

    fun onRecycled() {
        animator.cancel()
        item = null
    }

    override fun onViewHolderSelected() {
        animator.startSelectionAnimation()
    }

    override fun onViewHolderDeselected() {
        animator.startDeselectionAnimation()
    }
}
