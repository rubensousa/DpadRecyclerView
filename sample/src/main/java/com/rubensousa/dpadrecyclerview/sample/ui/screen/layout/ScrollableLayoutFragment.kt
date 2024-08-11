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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.layout

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.OnViewFocusedListener
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenScrollableLayoutBinding
import com.rubensousa.dpadrecyclerview.sample.ui.screen.grid.GridItemAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.screen.grid.GridItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration

class ScrollableLayoutFragment : Fragment(R.layout.screen_scrollable_layout) {

    private val binding by viewBinding(ScreenScrollableLayoutBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val itemAdapter = GridItemAdapter(object : GridItemViewHolder.ItemClickListener {
            override fun onViewHolderClicked() {

            }
        })
        itemAdapter.submitList(List(50) { it })
        val headerLayout = binding.scrollableLayout
        val recyclerView = binding.recyclerView
        recyclerView.apply {
            setSpanCount(5)
            addItemDecoration(
                DpadGridSpacingDecoration.create(
                    itemSpacing = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing)
                )
            )
            adapter = itemAdapter
        }
        binding.header1.requestFocus()
        binding.header1.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                headerLayout.showHeader()
            }
        }
        binding.header2.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                headerLayout.showHeader()
            }
        }
        recyclerView.addOnViewFocusedListener(object : OnViewFocusedListener {
            override fun onViewFocused(parent: RecyclerView.ViewHolder, child: View) {
                recyclerView.findContainingViewHolder(child)?.let { viewHolder ->
                    val row =
                        viewHolder.absoluteAdapterPosition / binding.recyclerView.getSpanCount()
                    when (row) {
                        0 -> headerLayout.scrollHeaderTo(-headerLayout.headerHeight / 4)
                        else -> headerLayout.hideHeader()
                    }
                }
            }
        })
    }

}
