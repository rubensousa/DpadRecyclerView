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

package com.rubensousa.dpadrecyclerview.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class TestPaginationFragment : TestGridFragment() {

    companion object {

        const val ARG_LOAD_DELAY = "arg_load_delay"

        fun getArgs(
            loadDelay: Long = 0L,
            layoutConfig: TestLayoutConfiguration,
            adapterConfig: TestAdapterConfiguration
        ): Bundle {
            val args = getArgs(layoutConfig, adapterConfig)
            args.putLong(ARG_LOAD_DELAY, loadDelay)
            return args
        }
    }

    private val concatAdapter = ConcatAdapter(
        ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()
    )
    private val viewModel by viewModels<PaginationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setLoadDelay(requireArguments().getLong(ARG_LOAD_DELAY))
    }

    override fun createAdapter(
        recyclerView: DpadRecyclerView,
        adapterConfig: TestAdapterConfiguration
    ): RecyclerView.Adapter<*> {
        val testAdapter = TestAdapter(
            adapterConfig,
            onViewHolderSelected = ::addViewHolderSelected,
            onViewHolderDeselected = ::addViewHolderDeselected
        )
        val loadingAdapter = PlaceholderAdapter()
        viewModel.initialLoad(adapterConfig.numberOfItems)
        viewModel.listState.observe(viewLifecycleOwner) { list ->
            testAdapter.submitList(list)
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            loadingAdapter.show(isLoading)
        }
        concatAdapter.addAdapter(testAdapter)
        return concatAdapter
    }

    override fun onViewHolderSelected(
        parent: DpadRecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        super.onViewHolderSelected(parent, child, position, subPosition)
        viewModel.loadMore(position)
    }

    class PlaceholderAdapter : RecyclerView.Adapter<PlaceholderAdapter.VH>() {

        private var show = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.test_placeholder, parent, false)
            )
        }

        override fun onBindViewHolder(holder: VH, position: Int) {

        }

        fun show(enabled: Boolean) {
            if (enabled == show) {
                return
            }
            show = enabled
            if (show) {
                notifyItemInserted(0)
            } else {
                notifyItemRemoved(0)
            }
        }

        override fun getItemCount(): Int {
            return if (show) {
                1
            } else {
                0
            }
        }

        class VH(view: View) : RecyclerView.ViewHolder(view)

    }


}