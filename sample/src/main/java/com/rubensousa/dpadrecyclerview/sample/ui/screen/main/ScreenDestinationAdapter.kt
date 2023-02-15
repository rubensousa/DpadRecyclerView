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
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.sample.databinding.MainAdapterItemFeatureBinding

class ScreenDestinationAdapter : RecyclerView.Adapter<ScreenDestinationAdapter.ViewHolder>() {

    private var items = emptyList<ScreenDestination>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            MainAdapterItemFeatureBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun replaceItems(newItems: List<ScreenDestination>) {
        items = newItems
    }

    class ViewHolder(
        private val binding: MainAdapterItemFeatureBinding
    ) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

        private var item: ScreenDestination? = null
        private val growInterpolator = AccelerateDecelerateInterpolator()
        private val shrinkInterpolator = AccelerateInterpolator()

        init {
            itemView.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    expand()
                } else {
                    shrink()
                }
            }
            itemView.setOnClickListener {
                item?.direction?.let {
                    itemView.findNavController().navigate(it)
                }
            }
        }

        fun bind(item: ScreenDestination) {
            binding.textView.text = item.title
            this.item = item
        }

        private fun shrink() {
            itemView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationZ(0.0f)
                .setInterpolator(shrinkInterpolator)
                .setDuration(200L)
        }

        private fun expand() {
            itemView.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .translationZ(8.0f)
                .setInterpolator(growInterpolator)
                .setDuration(200L)
        }
    }


}