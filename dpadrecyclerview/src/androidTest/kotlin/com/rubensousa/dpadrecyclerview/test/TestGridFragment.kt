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
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ViewHolderTask
import com.rubensousa.dpadrecyclerview.testing.DpadSelectionEvent
import com.rubensousa.dpadrecyclerview.testing.R

open class TestGridFragment : Fragment(R.layout.dpadrecyclerview_test_container),
    OnViewHolderSelectedListener {

    companion object {

        private const val ARG_LAYOUT_CONFIG = "layout_config"
        private const val ARG_ADAPTER_CONFIG = "adapter_config"

        fun getArgs(
            layoutConfig: TestLayoutConfiguration,
            adapterConfig: TestAdapterConfiguration = TestAdapterConfiguration()
        ): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(ARG_LAYOUT_CONFIG, layoutConfig)
            bundle.putParcelable(ARG_ADAPTER_CONFIG, adapterConfig)
            return bundle
        }
    }

    private val selectionEvents = ArrayList<DpadSelectionEvent>()
    private val tasks = ArrayList<DpadSelectionEvent>()
    private val alignedEvents = ArrayList<DpadSelectionEvent>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val layoutConfig = args.getParcelable<TestLayoutConfiguration>(ARG_LAYOUT_CONFIG)!!
        val adapterConfig = args.getParcelable<TestAdapterConfiguration>(ARG_ADAPTER_CONFIG)!!
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        recyclerView.addOnViewHolderSelectedListener(this)
        recyclerView.setFocusableDirection(layoutConfig.focusableDirection)
        recyclerView.setGravity(layoutConfig.gravity)
        recyclerView.setSpanCount(layoutConfig.spans)
        recyclerView.setOrientation(layoutConfig.orientation)
        recyclerView.setParentAlignment(layoutConfig.parentAlignment)
        recyclerView.setChildAlignment(layoutConfig.childAlignment)
        recyclerView.adapter = createAdapter(recyclerView, adapterConfig)
        recyclerView.requestFocus()
    }

    open fun createAdapter(
        recyclerView: DpadRecyclerView,
        adapterConfig: TestAdapterConfiguration
    ): RecyclerView.Adapter<*> {
        val adapter = TestAdapter(adapterConfig.itemLayoutId, adapterConfig.alternateFocus)
        adapter.submitList(ArrayList<Int>().apply {
            repeat(adapterConfig.numberOfItems) {
                add(it)
            }
        })
        return adapter
    }

    override fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        super.onViewHolderSelected(parent, child, position, subPosition)
        selectionEvents.add(DpadSelectionEvent(position, subPosition))
    }

    override fun onViewHolderSelectedAndAligned(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        super.onViewHolderSelectedAndAligned(parent, child, position, subPosition)
        alignedEvents.add(DpadSelectionEvent(position, subPosition))
    }

    fun selectWithTask(position: Int, smooth: Boolean, executeWhenAligned: Boolean = false) {
        val recyclerView = requireView().findViewById<DpadRecyclerView>(R.id.recyclerView)
        val task = object : ViewHolderTask(executeWhenAligned) {
            override fun execute(viewHolder: RecyclerView.ViewHolder) {
                tasks.add(DpadSelectionEvent(position = position))
            }
        }
        if (smooth) {
            recyclerView.setSelectedPositionSmooth(position, task)
        } else {
            recyclerView.setSelectedPosition(position, task)
        }
    }

    fun getTasksExecuted(): List<DpadSelectionEvent> = tasks

    fun getSelectionEvents(): List<DpadSelectionEvent> = selectionEvents

    fun getSelectedAndAlignedEvents(): List<DpadSelectionEvent> = alignedEvents

}
