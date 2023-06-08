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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.MainAdapterListFeatureBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.ItemModel
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ListAnimator
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.DpadStateHolder
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class FeatureListAdapter(
    private val stateHolder: DpadStateHolder,
    private val recycledViewPool: UnboundViewPool
) : ListAdapter<FeatureList, FeatureListAdapter.ViewHolder>(ItemModel.buildDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            recycledViewPool,
            MainAdapterListFeatureBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
        stateHolder.restore(holder.recyclerView, item.diffId, holder.adapter)
    }

    override fun onViewRecycled(holder: ViewHolder) {
        stateHolder.save(holder.recyclerView)
        holder.onRecycled()
    }

    class ViewHolder(
        recycledViewPool: UnboundViewPool,
        private val binding: MainAdapterListFeatureBinding
    ) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

        val recyclerView = binding.featureRecyclerView
        val adapter = ScreenDestinationAdapter()

        var item: FeatureList? = null
            private set

        private val animator = ListAnimator(recyclerView, binding.featureGroupTextView)

        init {
            recyclerView.setHasFixedSize(false)
            recyclerView.addItemDecoration(
                DpadLinearSpacingDecoration.create(
                    itemSpacing = itemView.resources.getDimensionPixelOffset(
                        R.dimen.feature_item_spacing
                    ),
                    edgeSpacing = itemView.resources.getDimensionPixelOffset(
                        R.dimen.feature_edge_spacing
                    ),
                )
            )
            recyclerView.setRecycledViewPool(recycledViewPool)
            recyclerView.setRecycleChildrenOnDetach(true)
            recyclerView.adapter = adapter
        }

        fun bind(item: FeatureList) {
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

}
