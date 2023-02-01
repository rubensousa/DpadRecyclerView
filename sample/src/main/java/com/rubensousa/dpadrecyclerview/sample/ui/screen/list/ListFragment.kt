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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenTvNestedListsBinding

class ListFragment : Fragment(R.layout.screen_tv_nested_lists) {

    private var _binding: ScreenTvNestedListsBinding? = null
    private val binding: ScreenTvNestedListsBinding get() = _binding!!
    private val viewModel by viewModels<ListViewModel>()
    private val args by navArgs<ListFragmentArgs>()
    private lateinit var listController : ListController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listController = ListController(this, args)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ScreenTvNestedListsBinding.bind(view)
        listController.setup(binding.recyclerView, viewLifecycleOwner, onSelected = { position ->
            viewModel.loadMore(position)
        })
        viewModel.listState.observe(viewLifecycleOwner) { list ->
            listController.submitList(list)
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            listController.showLoading(isLoading)
        }
        binding.add.setOnClickListener {
            listController.addItem()
        }
        binding.swap.setOnClickListener {
            listController.swapCurrentItemWithNext()
        }
        binding.delete.setOnClickListener {
            listController.deleteCurrentItem()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}