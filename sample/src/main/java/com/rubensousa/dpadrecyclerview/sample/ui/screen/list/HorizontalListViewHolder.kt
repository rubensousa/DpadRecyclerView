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

import android.view.Gravity
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.HorizontalAdapterListBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListModel
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ListAnimator
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemNestedAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class HorizontalListViewHolder(
    val binding: HorizontalAdapterListBinding
) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

    var item: ListModel? = null
    val recyclerView = binding.recyclerView
    val adapter = ItemNestedAdapter(R.layout.horizontal_adapter_item, animateFocusChanges = false)

    private val selectionView = binding.selectionOverlayView
    private val animator = ListAnimator(recyclerView, binding.textView)

    init {
        recyclerView.addItemDecoration(
            DpadLinearSpacingDecoration.create(
                itemSpacing = itemView.resources.getDimensionPixelOffset(
                    R.dimen.horizontal_item_spacing
                )
            )
        )
        selectionView.isActivated = false
    }

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        selectionView.isActivated = true
        animator.startSelectionAnimation()
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        selectionView.isActivated = false
        animator.startDeselectionAnimation()
    }

    fun bind(item: ListModel) {
        this.item = item
        binding.textView.text = item.title
        adapter.replaceList(item.items)
    }

    fun recycle() {
        animator.cancel()
        item = null
    }

    private fun setupReversedLayout() {
        val layoutParams = selectionView.layoutParams as FrameLayout.LayoutParams
        layoutParams.marginEnd = layoutParams.marginStart
        layoutParams.gravity = Gravity.CENTER_VERTICAL.or(Gravity.END)
        selectionView.layoutParams = layoutParams
        recyclerView.setReverseLayout(true)
    }

    private fun setupSlowScrollingBehavior() {
        LimitedScrollBehavior().setup(
            recyclerView = recyclerView,
            extraLayoutSpaceStart = { recyclerView.width / 2 },
            extraLayoutSpaceEnd = { 0 }
        )
    }

}
