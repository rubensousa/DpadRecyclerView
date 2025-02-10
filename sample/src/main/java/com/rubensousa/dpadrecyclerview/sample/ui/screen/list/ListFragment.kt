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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.AlignmentLookup
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.PlaceholderAdapter
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration
import com.rubensousa.dpadrecyclerview.spacing.DpadSpacingLookup
import com.rubensousa.dpadrecyclerview.state.DpadStateRegistry

class ListFragment : Fragment(R.layout.screen_recyclerview) {

    private val stateRegistry = DpadStateRegistry(this)
    private var selectedPosition: Int = RecyclerView.NO_POSITION
    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)
    private val viewModel by viewModels<ListViewModel>()
    private val args by navArgs<ListFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.load(enableLooping = args.enableLooping)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView(binding.recyclerView)
        val placeholderAdapter = PlaceholderAdapter()
        val itemAdapter = HorizontalListAdapter(
            scrollState = stateRegistry.getScrollState(),
            config = HorizontalListConfig(
                isScrollSpeedLimited = args.slowScroll,
                reverseLayout = args.reverseLayout,
                itemLayoutId = if (args.showOverlay) {
                    R.layout.horizontal_adapter_item
                } else {
                    R.layout.horizontal_adapter_animated_item
                },
                animateFocusChanges = !args.showOverlay
            )
        )
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(true)
                .build()
        )
        if (args.showHeader) {
            concatAdapter.addAdapter(HeaderAdapter())
        }
        concatAdapter.addAdapter(itemAdapter)
        concatAdapter.addAdapter(placeholderAdapter)

        viewModel.listState.observe(viewLifecycleOwner) { list ->
            itemAdapter.submitList(list)
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            placeholderAdapter.show(isLoading)
        }

        binding.selectionOverlayView.isVisible = args.showOverlay
        binding.selectionOverlayView.isActivated = true
        binding.recyclerView.apply {
            adapter = concatAdapter
            requestFocus()
        }
    }

    private fun setupRecyclerView(recyclerView: DpadRecyclerView) {
        recyclerView.apply {
            if (!args.showHeader) {
                setParentAlignment(ParentAlignment(edge = ParentAlignment.Edge.NONE))
            }
            addItemDecoration(
                DpadLinearSpacingDecoration.create(
                    itemSpacing = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing),
                ).also {
                    it.setSpacingLookup(object : DpadSpacingLookup {
                        override fun shouldApplySpacing(
                            viewHolder: RecyclerView.ViewHolder,
                            itemCount: Int
                        ): Boolean {
                            return if (args.showHeader) {
                                viewHolder.absoluteAdapterPosition > 0
                            } else {
                                true
                            }
                        }
                    })
                }
            )
            if (args.showHeader) {
                recyclerView.setAlignments(
                    parent = ParentAlignment(
                        edge = ParentAlignment.Edge.NONE,
                        fraction = 0.35f
                    ),
                    child = ChildAlignment(
                        fraction = 0f
                    ),
                    smooth = false
                )
                recyclerView.setAlignmentLookup(object : AlignmentLookup {
                    override fun getParentAlignment(viewHolder: RecyclerView.ViewHolder): ParentAlignment? {
                        return if (viewHolder.layoutPosition % 2 == 0) {
                            ParentAlignment(fraction = 0.5f)
                        } else {
                            null
                        }
                    }
                    override fun getChildAlignment(viewHolder: RecyclerView.ViewHolder): ChildAlignment? {
                        return if (viewHolder.layoutPosition % 2 == 0) {
                            ChildAlignment(fraction = 0.5f)
                        } else {
                            null
                        }
                    }
                })
            }
            if (args.showHeader) {
                addItemDecoration(
                    DpadLinearSpacingDecoration.create(
                        itemSpacing = resources.getDimensionPixelOffset(R.dimen.grid_item_spacing),
                        edgeSpacing = 0
                    ).also {
                        it.setSpacingLookup(object : DpadSpacingLookup {
                            override fun shouldApplySpacing(
                                viewHolder: RecyclerView.ViewHolder,
                                itemCount: Int
                            ): Boolean {
                                return viewHolder.absoluteAdapterPosition == 0
                            }
                        })
                    }
                )
            }
            if (selectedPosition != RecyclerView.NO_POSITION) {
                recyclerView.setSelectedPosition(selectedPosition)
            }
            addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
                override fun onViewHolderSelected(
                    parent: DpadRecyclerView,
                    child: RecyclerView.ViewHolder?,
                    position: Int,
                    subPosition: Int
                ) {
                    selectedPosition = position
                    viewModel.loadMore(position)
                }
            })
            if (args.slowScroll) {
                LimitedScrollBehavior().setup(this)
            }
        }
    }

}