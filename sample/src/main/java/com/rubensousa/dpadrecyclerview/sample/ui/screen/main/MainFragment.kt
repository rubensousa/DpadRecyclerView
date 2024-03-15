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
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenMainBinding
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration
import com.rubensousa.dpadrecyclerview.state.DpadStateRegistry

class MainFragment : Fragment(R.layout.screen_main) {

    private val stateRegistry = DpadStateRegistry()
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.load()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = ScreenMainBinding.bind(view)
        val recyclerView = binding.recyclerView
        val adapter = FeatureListAdapter(
            scrollState = stateRegistry.getScrollState(),
            recycledViewPool = UnboundViewPool()
        )
        recyclerView.adapter = adapter
        recyclerView.requestFocus()
        recyclerView.addItemDecoration(
            DpadLinearSpacingDecoration.create(
                itemSpacing = resources.getDimensionPixelOffset(R.dimen.vertical_item_spacing)
            )
        )
        viewModel.getFeatures().observe(viewLifecycleOwner) { features ->
            adapter.submitList(features)
        }
    }

}
