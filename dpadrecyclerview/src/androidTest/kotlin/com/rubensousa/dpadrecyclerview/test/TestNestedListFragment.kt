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

package com.rubensousa.dpadrecyclerview.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewFocusedListener
import com.rubensousa.dpadrecyclerview.state.DpadScrollState
import com.rubensousa.dpadrecyclerview.state.DpadStateRegistry
import com.rubensousa.dpadrecyclerview.test.tests.AbstractTestAdapter
import com.rubensousa.dpadrecyclerview.testfixtures.DpadFocusEvent

class TestNestedListFragment : Fragment(R.layout.dpadrecyclerview_test_container) {

    private val configuration = TestAdapterConfiguration(
        itemLayoutId = R.layout.dpadrecyclerview_item_horizontal,
        numberOfItems = 200
    )
    private val stateRegistry = DpadStateRegistry(this)
    private val parentFocusEvents = arrayListOf<DpadFocusEvent>()
    private val childFocusEvents = arrayListOf<DpadFocusEvent>()
    private val parentFocusListener = object : OnViewFocusedListener {
        override fun onViewFocused(
            parent: RecyclerView.ViewHolder,
            child: View,
        ) {
            parentFocusEvents.add(DpadFocusEvent(parent, child, parent.layoutPosition))
        }
    }
    private val childFocusListener = object : OnViewFocusedListener {
        override fun onViewFocused(
            parent: RecyclerView.ViewHolder,
            child: View,
        ) {
            childFocusEvents.add(DpadFocusEvent(parent, child, parent.layoutPosition))
        }
    }
    private val nestedAdapter = NestedAdapter(
        configuration = configuration,
        onViewFocusedListener = childFocusListener,
        scrollState = stateRegistry.getScrollState()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        recyclerView.adapter = nestedAdapter
        recyclerView.addOnViewFocusedListener(parentFocusListener)
        recyclerView.requestFocus()
    }

    fun getChildFocusEvents() = childFocusEvents.toList()

    fun getParentFocusEvents() = parentFocusEvents.toList()

    fun getRecyclerView(): DpadRecyclerView = requireView().findViewById(R.id.recyclerView)

    class NestedAdapter(
        private val configuration: TestAdapterConfiguration,
        private val onViewFocusedListener: OnViewFocusedListener,
        private val scrollState: DpadScrollState,
    ) : AbstractTestAdapter<ListViewHolder>(configuration.numberOfItems) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
            return ListViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.dpadrecyclerview_nested_list, parent, false),
                configuration,
                onViewFocusedListener,
            )
        }

        override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
            holder.bind(position)
            scrollState.restore(
                recyclerView = holder.recyclerView,
                key = position.toString(),
                adapter = holder.adapter
            )
        }

        override fun onViewRecycled(holder: ListViewHolder) {
            super.onViewRecycled(holder)
            scrollState.save(
                recyclerView = holder.recyclerView,
                key = holder.absoluteAdapterPosition.toString(),
                detachAdapter = true
            )
        }

    }

    class ListViewHolder(
        val view: View,
        val configuration: TestAdapterConfiguration,
        onViewFocusedListener: OnViewFocusedListener,
    ) : RecyclerView.ViewHolder(view) {

        val adapter = TestAdapter(
            adapterConfiguration = configuration,
            onViewHolderSelected = { position -> },
            onViewHolderDeselected = { position -> }
        )
        private val textView = view.findViewById<TextView>(R.id.textView)

        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.nestedRecyclerView)

        init {
            recyclerView.addOnViewFocusedListener(onViewFocusedListener)
        }

        fun bind(position: Int) {
            recyclerView.tag = position
            textView.text = "List $position"
            textView.freezesText = true
        }
    }

}
