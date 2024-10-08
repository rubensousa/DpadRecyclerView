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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.main

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.OnViewFocusedListener
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.MainAdapterListFeatureBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.DelegateViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ListAnimator
import timber.log.Timber

class FeatureListViewHolder(
    private val recycledViewPool: UnboundViewPool,
    private val binding: MainAdapterListFeatureBinding,
) : DelegateViewHolder<FeatureList>(binding.root), DpadViewHolder {

    val recyclerView = binding.featureRecyclerView
    val adapter = ScreenDestinationAdapter()

    var item: FeatureList? = null
        private set

    private val animator = ListAnimator(recyclerView, binding.featureGroupTextView)

    init {
        recyclerView.setHasFixedSize(false)
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
        recyclerView.addOnViewFocusedListener(object : OnViewFocusedListener {
            override fun onViewFocused(
                parent: RecyclerView.ViewHolder,
                child: View,
            ) {
                Timber.i("Feature focused: ${parent.layoutPosition}, view: $child")
            }
        })
        recyclerView.setRecycleChildrenOnDetach(true)
        recyclerView.adapter = adapter
    }

    override fun bind(item: FeatureList) {
        this.item = item
        adapter.replaceItems(item.destinations)
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
