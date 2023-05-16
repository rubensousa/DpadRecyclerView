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
import com.rubensousa.dpadrecyclerview.OnChildLaidOutListener
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.ViewHolderTask
import com.rubensousa.dpadrecyclerview.test.tests.AbstractTestAdapter
import com.rubensousa.dpadrecyclerview.testfixtures.DpadSelectionEvent
import com.rubensousa.dpadrecyclerview.testing.R

open class TestGridFragment : Fragment(R.layout.dpadrecyclerview_test_container),
    OnViewHolderSelectedListener, OnChildLaidOutListener {

    companion object {

        private const val ARG_LAYOUT_CONFIG = "layout_config"
        private const val ARG_ADAPTER_CONFIG = "adapter_config"

        var viewPool = UnboundViewPool()

        fun getArgs(
            layoutConfig: TestLayoutConfiguration,
            adapterConfig: TestAdapterConfiguration = TestAdapterConfiguration()
        ): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(ARG_LAYOUT_CONFIG, layoutConfig)
            bundle.putParcelable(ARG_ADAPTER_CONFIG, adapterConfig)
            return bundle
        }

        fun installViewPool(viewPool: UnboundViewPool) {
            this.viewPool = viewPool
        }

    }

    private val selectionEvents = ArrayList<DpadSelectionEvent>()
    private val viewHolderSelections = ArrayList<Int>()
    private val viewHolderDeselections = ArrayList<Int>()
    private val tasks = ArrayList<DpadSelectionEvent>()
    private val alignedEvents = ArrayList<DpadSelectionEvent>()
    private val layoutEvents = ArrayList<RecyclerView.ViewHolder>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val layoutConfig = args.getParcelable<TestLayoutConfiguration>(ARG_LAYOUT_CONFIG)!!
        val adapterConfig = args.getParcelable<TestAdapterConfiguration>(ARG_ADAPTER_CONFIG)!!
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        if (layoutConfig.useCustomViewPool) {
            recyclerView.setRecycledViewPool(viewPool)
        }
        recyclerView.addOnViewHolderSelectedListener(this)
        recyclerView.setOnChildLaidOutListener(this)

        recyclerView.apply {
            setReverseLayout(layoutConfig.reverseLayout)
            setRecycleChildrenOnDetach(layoutConfig.recycleChildrenOnDetach)
            setFocusableDirection(layoutConfig.focusableDirection)
            setGravity(layoutConfig.gravity)
            setSpanCount(layoutConfig.spans)
            setOrientation(layoutConfig.orientation)
            setParentAlignment(layoutConfig.parentAlignment)
            setChildAlignment(layoutConfig.childAlignment)
            setLoopDirection(layoutConfig.loopDirection)
            setFocusSearchEnabledDuringAnimations(layoutConfig.focusSearchEnabledDuringAnimations)
            adapter = createAdapter(recyclerView, adapterConfig)
            requestFocus()
        }

    }

    fun mutateAdapter(action: (adapter: AbstractTestAdapter<*>) -> Unit) {
        getDpadRecyclerView()?.adapter?.let { adapter ->
            if (adapter is AbstractTestAdapter<*>) {
                action(adapter)
            }
        }
    }

    fun requestFocus() {
        getDpadRecyclerView()?.requestFocus()
    }

    open fun createAdapter(
        recyclerView: DpadRecyclerView,
        adapterConfig: TestAdapterConfiguration
    ): RecyclerView.Adapter<*> {
        return TestAdapter(adapterConfig,
            onViewHolderSelected = ::addViewHolderSelected,
            onViewHolderDeselected = ::addViewHolderDeselected
        )
    }

    override fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        selectionEvents.add(DpadSelectionEvent(position, subPosition))
    }

    override fun onViewHolderSelectedAndAligned(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
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

    fun clearEvents() {
        selectionEvents.clear()
        alignedEvents.clear()
    }

    fun clearAdapter() {
        getDpadRecyclerView()?.adapter = null
    }

    fun getTasksExecuted(): List<DpadSelectionEvent> = tasks

    fun getSelectionEvents(): List<DpadSelectionEvent> = selectionEvents

    fun getViewHolderSelections(): List<Int> = viewHolderSelections

    fun getViewHolderDeselections(): List<Int> = viewHolderDeselections

    fun getSelectedAndAlignedEvents(): List<DpadSelectionEvent> = alignedEvents

    fun getLayoutEvents(): List<RecyclerView.ViewHolder> = layoutEvents

    override fun onChildLaidOut(parent: RecyclerView, child: RecyclerView.ViewHolder) {
        layoutEvents.add(child)
    }

    protected fun addViewHolderSelected(position: Int) {
        viewHolderSelections.add(position)
    }

    protected fun addViewHolderDeselected(position: Int) {
        viewHolderDeselections.add(position)
    }

    private fun getDpadRecyclerView(): DpadRecyclerView? = view?.findViewById(R.id.recyclerView)

}
