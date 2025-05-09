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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.compose.ComposeViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemComposable

class VerticalListFragment : Fragment(R.layout.screen_recyclerview) {

    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            setParentAlignment(
                ParentAlignment(
                    edge = ParentAlignment.Edge.MIN_MAX,
                    fraction = 0.5f
                )
            )
            adapter = Adapter(
                items = List(50) { i -> i }
            )
            requestFocus()
        }
    }

    private class Adapter(
        private val items: List<Int>,
    ) : RecyclerView.Adapter<ComposeViewHolder<Int>>() {

        override fun onCreateViewHolder(
            parent: ViewGroup, viewType: Int
        ): ComposeViewHolder<Int> {
            return ComposeViewHolder(parent) { item ->
                ItemComposable(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    item = item,
                    onClick = {

                    }
                )
            }
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ComposeViewHolder<Int>, position: Int) {
            holder.setItemState(items[position])
        }

        override fun onViewRecycled(holder: ComposeViewHolder<Int>) {
            super.onViewRecycled(holder)
            holder.setItemState(null)
        }


    }
}
