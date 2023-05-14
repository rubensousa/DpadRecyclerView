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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadLoopDirection
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.SubPositionAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.HorizontalAdapterListBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListModel
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ListAnimator
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemNestedAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class HorizontalListViewHolder(
    val binding: HorizontalAdapterListBinding,
    config: HorizontalListConfig
) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

    var item: ListModel? = null
    val recyclerView = binding.recyclerView
    val adapter = ItemNestedAdapter(config)

    private val animator = ListAnimator(recyclerView, binding.textView)
    private val alignments = listOf(
        SubPositionAlignment(
            alignmentViewId = recyclerView.id
        )
    )

    init {
        recyclerView.addItemDecoration(
            DpadLinearSpacingDecoration.create(
                itemSpacing = itemView.resources.getDimensionPixelOffset(
                    R.dimen.horizontal_item_spacing
                )
            )
        )
        applyConfig(config)
    }

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        animator.startSelectionAnimation()
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        animator.startDeselectionAnimation()
    }

    override fun getSubPositionAlignments(): List<SubPositionAlignment> {
        return alignments
    }

    fun bind(item: ListModel) {
        this.item = item
        binding.textView.text = item.title
        if (item.enableLooping) {
            recyclerView.setLoopDirection(DpadLoopDirection.MAX)
        } else {
            recyclerView.setLoopDirection(DpadLoopDirection.NONE)
        }
        adapter.replaceList(item.items)
        recyclerView.adapter = adapter
    }

    fun recycle() {
        animator.cancel()
        recyclerView.adapter = null
        item = null
    }

    private fun applyConfig(config: HorizontalListConfig) {
        if (config.reverseLayout) {
            recyclerView.setReverseLayout(true)
        }
        if (config.isScrollSpeedLimited) {
            setupSlowScrollingBehavior()
        }
    }

    private fun setupSlowScrollingBehavior() {
        LimitedScrollBehavior().setup(
            recyclerView = recyclerView,
            extraLayoutSpaceStart = { recyclerView.width / 2 }
        )
    }

}
