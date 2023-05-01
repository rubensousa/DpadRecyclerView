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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.SubPositionAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.NestedListsHeaderBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter

class HeaderAdapter : ListAdapter<Int, HeaderAdapter.ViewHolder>(MutableGridAdapter.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            NestedListsHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 1
    }

    class ViewHolder(private val binding: NestedListsHeaderBinding)
        : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

        private val subPositions = listOf(
            SubPositionAlignment(
                focusViewId = R.id.watchButton
            ),
            SubPositionAlignment(
                focusViewId = R.id.bookmarkButton
            )
        )

        override fun getSubPositionAlignments(): List<SubPositionAlignment> = subPositions

        override fun onViewHolderDeselected() {
            super.onViewHolderDeselected()
            binding.bookmarkButton.isFocusable = false
            binding.bookmarkButton.isFocusableInTouchMode = false
        }

        override fun onViewHolderSelected() {
            super.onViewHolderSelected()
            binding.bookmarkButton.isFocusable = true
            binding.bookmarkButton.isFocusableInTouchMode = true
        }
    }

}
