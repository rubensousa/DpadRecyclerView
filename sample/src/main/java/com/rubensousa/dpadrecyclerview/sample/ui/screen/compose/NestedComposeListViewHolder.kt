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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.compose

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListComposeBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListModel
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ListAnimator
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class NestedComposeListViewHolder(
    private val binding: AdapterListComposeBinding,
    viewPool: UnboundViewPool
) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

    var item: ListModel? = null
        private set

    private val recyclerView = binding.cardRecyclerView
    private val adapter = ComposeItemAdapter(onItemClick = {})
    private val animator = ListAnimator(recyclerView, binding.cardListTextView)

    init {
        recyclerView.setRecycledViewPool(viewPool)
        recyclerView.addItemDecoration(
            DpadLinearSpacingDecoration.create(
                itemSpacing = itemView.resources.getDimensionPixelOffset(
                    R.dimen.horizontal_item_spacing
                ),
                edgeSpacing = 0
            )
        )
        recyclerView.adapter = adapter
    }

    override fun onViewHolderSelected() {
        animator.startSelectionAnimation()
    }

    override fun onViewHolderDeselected() {
        animator.startDeselectionAnimation()
    }

    fun getRecyclerView(): DpadRecyclerView = recyclerView

    fun getAdapter(): RecyclerView.Adapter<*> = adapter

    fun bind(item: ListModel) {
        this.item = item
        binding.cardListTextView.text = item.title
        adapter.replaceList(item.items)
    }

    fun cancelAnimations() {
        animator.cancel()
    }

}
