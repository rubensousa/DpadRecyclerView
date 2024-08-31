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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.fading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.FadingAdapterListBinding
import com.rubensousa.dpadrecyclerview.sample.databinding.HorizontalAdapterAnimatedItemBinding
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenFadingEdgesBinding
import com.rubensousa.dpadrecyclerview.sample.ui.dpToPx
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder

class FadingEdgeFragment : Fragment(R.layout.screen_fading_edges) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ScreenFadingEdgesBinding.bind(view)
        val adapter = Adapter()
        adapter.submitList(
            listOf(
                Configuration(
                    title = "Fade start",
                    minEdgeLength = 60.dp,
                ),
                Configuration(
                    title = "Fade both sides with end offset",
                    minEdgeLength = 60.dp,
                    maxEdgeLength = 60.dp,
                    maxEdgeOffset = 16.dp
                ),
                Configuration(
                    title = "Fade both sides with large end fading",
                    minEdgeLength = 60.dp,
                    maxEdgeLength = 120.dp,
                    maxEdgeOffset = 24.dp
                ),
            )
        )
        binding.dpadRecyclerView.adapter = adapter
        binding.dpadRecyclerView.requestFocus()
    }

    data class Configuration(
        val title: String,
        val minEdgeLength: Dp = 0.dp,
        val minEdgeOffset: Dp = 0.dp,
        val maxEdgeLength: Dp = 0.dp,
        val maxEdgeOffset: Dp = 0.dp
    )

    class Adapter : ListAdapter<Configuration, RowViewHolder>(
        object : DiffUtil.ItemCallback<Configuration?>() {
            override fun areItemsTheSame(oldItem: Configuration, newItem: Configuration): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItem: Configuration,
                newItem: Configuration
            ): Boolean {
                return oldItem == newItem
            }
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
            return RowViewHolder(
                FadingAdapterListBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }


    class RowViewHolder(val binding: FadingAdapterListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val adapter = RowAdapter()

        init {
            binding.recyclerView.adapter = adapter
        }

        fun bind(configuration: Configuration) {
            binding.textView.text = configuration.title
            binding.recyclerView.apply {
                setItemSpacing(dpToPx(16.dp))
                enableMinEdgeFading(configuration.minEdgeLength > 0.dp)
                setMinEdgeFadingLength(dpToPx(configuration.minEdgeLength))
                setMinEdgeFadingOffset(dpToPx(configuration.minEdgeOffset))

                enableMaxEdgeFading(configuration.maxEdgeLength > 0.dp)
                setMaxEdgeFadingLength(dpToPx(configuration.maxEdgeLength))
                setMaxEdgeFadingOffset(dpToPx(configuration.maxEdgeOffset))
            }
        }

    }

    class RowAdapter : RecyclerView.Adapter<ItemViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            val binding = HorizontalAdapterAnimatedItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ItemViewHolder(
                binding.root, binding.textView, animateFocusChanges = false
            )
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            holder.bind(position, null)
            holder.itemView.isFocusable = true
            holder.itemView.isFocusableInTouchMode = true
        }

        override fun getItemCount(): Int = 25

    }


}
