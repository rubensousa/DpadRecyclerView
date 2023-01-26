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

import androidx.fragment.app.Fragment
import androidx.leanback.widget.BaseGridView
import androidx.leanback.widget.OnChildViewHolderSelectedListener
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ViewHolderTask
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.RecyclerViewLogger
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.DpadStateHolder
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.ListModel
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.NestedListAdapter
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.list.PlaceholderAdapter
import timber.log.Timber

class ListController(private val fragment: Fragment) {

    private var selectedPosition = RecyclerView.NO_POSITION
    private val loadingAdapter = PlaceholderAdapter()
    private val scrollStateHolder = DpadStateHolder()
    private val nestedListAdapter = NestedListAdapter(scrollStateHolder,
        object : ItemViewHolder.ItemClickListener {
            override fun onViewHolderClicked() {
                fragment.findNavController().navigate(R.id.open_detail)
            }
        })
    private var dpadRecyclerView: DpadRecyclerView? = null
    private var gridView: BaseGridView? = null

    fun setup(
        recyclerView: DpadRecyclerView,
        lifecycleOwner: LifecycleOwner,
        onSelected: (position: Int) -> Unit
    ) {
        RecyclerViewLogger.logChildrenWhenIdle(recyclerView)
        recyclerView.itemAnimator = DefaultItemAnimator().also { animator ->
            animator.supportsChangeAnimations = false
            // Just here to preview changes
            // animator.removeDuration = 500L
            // animator.addDuration = 500L
            // animator.moveDuration = 2000L
        }
        dpadRecyclerView = recyclerView
        setupAdapter(recyclerView)
        setupSpacings(recyclerView)
        setupPagination(recyclerView, onSelected)
        setupLifecycle(lifecycleOwner)

        if (selectedPosition != RecyclerView.NO_POSITION) {
            recyclerView.setSelectedPosition(
                selectedPosition, object : ViewHolderTask() {
                    override fun execute(viewHolder: RecyclerView.ViewHolder) {
                        Timber.d("Selection state restored")
                    }
                })
        }

        recyclerView.requestFocus()
    }

    fun deleteCurrentItem() {
        val recyclerView = getRecyclerView() ?: return
        nestedListAdapter.removeAt(getSelectedPosition())
        logAfterAnimation(recyclerView)
    }

    fun addItem() {
        val recyclerView = getRecyclerView() ?: return
        val currentItem = nestedListAdapter.getItem(getSelectedPosition())
        nestedListAdapter.addAt(
            getSelectedPosition() + 1,
            currentItem.copy(title = "New Inserted List")
        )
        logAfterAnimation(recyclerView)
    }

    fun swapCurrentItemWithNext() {
        val recyclerView = getRecyclerView() ?: return
        if (getSelectedPosition() == nestedListAdapter.itemCount - 1) {
            return
        }
        nestedListAdapter.move(
            getSelectedPosition(),
            getSelectedPosition() + 1
        )
        logAfterAnimation(recyclerView)
    }

    private fun getRecyclerView(): RecyclerView? {
        return dpadRecyclerView ?: gridView
    }

    private fun getSelectedPosition(): Int {
        return dpadRecyclerView?.getSelectedPosition()
            ?: gridView?.selectedPosition
            ?: RecyclerView.NO_POSITION
    }

    private fun logAfterAnimation(recyclerView: RecyclerView) {
        recyclerView.itemAnimator?.isRunning {
            RecyclerViewLogger.logChildren(recyclerView)
        }
    }

    fun setup(
        recyclerView: BaseGridView,
        lifecycleOwner: LifecycleOwner,
        onSelected: (position: Int) -> Unit
    ) {
        RecyclerViewLogger.logChildrenWhenIdle(recyclerView)
        gridView = recyclerView
        recyclerView.itemAnimator = DefaultItemAnimator().also { animator ->
            // Just here to preview changes
            animator.supportsChangeAnimations = false
            // animator.removeDuration = 2000L
            // animator.addDuration = 2000L
            //  animator.moveDuration = 2000L
        }
        setupAdapter(recyclerView)
        setupSpacings(recyclerView)
        setupPagination(recyclerView, onSelected)
        setupLifecycle(lifecycleOwner)

        if (selectedPosition != RecyclerView.NO_POSITION) {
            recyclerView.setSelectedPosition(selectedPosition) {
                Timber.d("Selection state restored")
            }
        }

        recyclerView.requestFocus()
    }


    fun submitList(list: MutableList<ListModel>) {
        nestedListAdapter.submitList(list) {
            dpadRecyclerView?.invalidateItemDecorations()
        }
    }

    fun showLoading(isLoading: Boolean) {
        loadingAdapter.show(isLoading)
    }

    private fun setupAdapter(recyclerView: RecyclerView) {
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(true)
                .build()
        )
        concatAdapter.addAdapter(nestedListAdapter)
        concatAdapter.addAdapter(loadingAdapter)
        nestedListAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        recyclerView.adapter = concatAdapter
    }

    private fun setupLifecycle(lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                dpadRecyclerView?.adapter = null
                dpadRecyclerView = null
            }
        })
    }

    private fun setupPagination(
        recyclerView: DpadRecyclerView,
        onSelected: (position: Int) -> Unit
    ) {
        recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                selectedPosition = position
                onSelected(position)
                Timber.d("Selected: $position, $subPosition")
            }

            override fun onViewHolderSelectedAndAligned(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                Timber.d("Aligned: $position, $subPosition")
                Timber.d("FirstVisibleItemPosition: ${recyclerView.findFirstVisibleItemPosition()}")
                Timber.d("FirstLastVisibleItemPosition: ${recyclerView.findLastVisibleItemPosition()}")

            }
        })
    }

    private fun setupPagination(
        recyclerView: BaseGridView,
        onSelected: (position: Int) -> Unit
    ) {
        recyclerView.addOnChildViewHolderSelectedListener(object :
            OnChildViewHolderSelectedListener() {

            override fun onChildViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subposition: Int
            ) {
                super.onChildViewHolderSelected(parent, child, position, subposition)
                selectedPosition = position
                onSelected(position)
                Timber.d("Selected: $position, $subposition")
            }

            override fun onChildViewHolderSelectedAndPositioned(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subposition: Int
            ) {
                super.onChildViewHolderSelectedAndPositioned(parent, child, position, subposition)
                Timber.d("Aligned: $position, $subposition")
            }
        })
    }

    private fun setupSpacings(recyclerView: RecyclerView) {
        recyclerView.addItemDecoration(
            LinearMarginDecoration.createVertical(
                verticalMargin = recyclerView.resources.getDimensionPixelOffset(
                    R.dimen.item_spacing
                )
            )
        )
    }

}
