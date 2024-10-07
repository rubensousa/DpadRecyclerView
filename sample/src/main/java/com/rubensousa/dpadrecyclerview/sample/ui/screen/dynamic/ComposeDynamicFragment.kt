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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.dynamic

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.model.DelegateAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.RecyclerViewLogger
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class ComposeDynamicFragment : Fragment(R.layout.screen_recyclerview) {

    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(binding.recyclerView)
        val adapter = DelegateAdapter()
        adapter.submitList(mutableListOf())
        binding.recyclerView.apply {
            setAdapter(adapter)
            setSmoothScrollMaxPendingMoves(0)
            requestFocus()
        }
    }

    private fun setupRecyclerView(recyclerView: DpadRecyclerView) {
        recyclerView.apply {
            RecyclerViewLogger.logChildrenWhenIdle(this)
            addItemDecoration(
                DpadLinearSpacingDecoration.create(
                    itemSpacing = resources.getDimensionPixelOffset(R.dimen.vertical_item_spacing)
                )
            )
        }
    }
}
