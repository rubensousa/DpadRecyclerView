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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.grid

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.compose.DpadComposeFocusViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.dpToPx
import com.rubensousa.dpadrecyclerview.sample.ui.model.ListTypes
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.MutableListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.GridItemComposable
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.MutableGridAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import timber.log.Timber

class GridSpanHeaderFragment : Fragment(R.layout.screen_recyclerview) {

    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gridAdapter = SpanGridAdapter()

        binding.recyclerView.apply {
            adapter = gridAdapter
            addItemDecoration(
                DpadGridSpacingDecoration.create(
                    itemSpacing = dpToPx(16.dp),
                    perpendicularItemSpacing = dpToPx(16.dp),
                    edgeSpacing = dpToPx(48.dp)
                )
            )
            setSpanCount(4)
            setSpanSizeLookup(object : DpadSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemViewType = gridAdapter.getItemViewType(position)
                    return when (itemViewType) {
                        ListTypes.ITEM -> 1
                        else -> getSpanCount()
                    }
                }
            })

            val list = mutableListOf<Int>()
            val headers = 25
            repeat(headers) {
                list.add(-1)
                repeat(getSpanCount() * 2 - 1) {
                    list.add(list.size)
                }
            }
            gridAdapter.submitList(list)

            requestFocus()
        }

    }

    class SpanGridAdapter : MutableListAdapter<Int, DpadComposeFocusViewHolder<Int>>(
        MutableGridAdapter.DIFF_CALLBACK
    ) {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): DpadComposeFocusViewHolder<Int> {
            return when (viewType) {
                ListTypes.ITEM -> {
                    DpadComposeFocusViewHolder(parent) { item ->
                        GridItemComposable(
                            item = item,
                            onClick = {
                                Timber.i("Clicked: $item")
                            }
                        )
                    }
                }

                else -> {
                    DpadComposeFocusViewHolder(
                        parent,
                        isFocusable = false
                    ) { _ ->
                        Text(
                            modifier = Modifier.padding(
                                horizontal = 48.dp,
                                vertical = 24.dp
                            ),
                            text = "Header",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: DpadComposeFocusViewHolder<Int>, position: Int) {
            holder.setItemState(getItem(position))
        }

        override fun getItemViewType(position: Int): Int {
            val item = getItem(position)
            return if (item > 0) {
                ListTypes.ITEM
            } else {
                ListTypes.HEADER
            }
        }

    }
}

