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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.SubPositionAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListHeaderBinding

class ListHeaderAdapter : ListAdapter<String, ListHeaderAdapter.VH>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val viewHolder = VH(
            AdapterListHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        viewHolder.itemView.isFocusableInTouchMode = true
        viewHolder.itemView.isFocusable = true
        return viewHolder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        return ListTypes.HEADER
    }

    class VH(
        binding: AdapterListHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

        private val childAlignments = ArrayList<SubPositionAlignment>()

        init {
            childAlignments.apply {
                add(
                    SubPositionAlignment(
                        offset = 0,
                        offsetRatio = 0f,
                        alignmentViewId = R.id.subPosition0TextView,
                        focusViewId = R.id.subPosition0TextView
                    )
                )
                add(
                    SubPositionAlignment(
                        offset = 0,
                        offsetRatio = 0f,
                        alignmentViewId = R.id.subPosition0TextView,
                        focusViewId = R.id.subPosition1TextView
                    )
                )
            }
        }

        override fun getSubPositionAlignments(): List<SubPositionAlignment> {
            return childAlignments
        }

    }


}
