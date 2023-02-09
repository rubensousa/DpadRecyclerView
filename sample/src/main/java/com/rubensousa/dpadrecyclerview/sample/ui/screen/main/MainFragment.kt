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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterItemNavigationBinding

class MainFragment : Fragment(R.layout.screen_main) {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view as RecyclerView
        val adapter = NavigationAdapter()
        recyclerView.adapter = adapter
        recyclerView.requestFocus()
        viewModel.getDestinations().observe(viewLifecycleOwner) { destinations ->
            adapter.submitList(destinations)
        }
    }

    class NavigationAdapter : ListAdapter<ScreenDestination, NavigationViewHolder>(
        object : DiffUtil.ItemCallback<ScreenDestination>() {
            override fun areItemsTheSame(
                oldItem: ScreenDestination,
                newItem: ScreenDestination
            ): Boolean = oldItem.title == newItem.title

            override fun areContentsTheSame(
                oldItem: ScreenDestination,
                newItem: ScreenDestination
            ): Boolean = oldItem == newItem
        }) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NavigationViewHolder {
            return NavigationViewHolder(
                AdapterItemNavigationBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: NavigationViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }

    class NavigationViewHolder(
        private val binding: AdapterItemNavigationBinding
    ) : ViewHolder(binding.root) {

        private var item: ScreenDestination? = null

        init {
            itemView.isFocusableInTouchMode = true
            itemView.isFocusable = true
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
    }

}
