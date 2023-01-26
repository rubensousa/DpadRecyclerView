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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.list

import androidx.leanback.widget.HorizontalGridView
import androidx.leanback.widget.OnChildViewHolderSelectedListener
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener

class DpadStateHolder {

    private val positions = LinkedHashMap<String, Int>()
    private val listeners = LinkedHashMap<String, OnViewHolderSelectedListener>()
    private val leanbackListeners = LinkedHashMap<String, OnChildViewHolderSelectedListener>()

    fun register(recyclerView: DpadRecyclerView, key: String, adapter: RecyclerView.Adapter<*>) {
        recyclerView.adapter = adapter
        val restoredPosition = positions[key] ?: 0
        recyclerView.setSelectedPosition(restoredPosition)
        val listener = object : OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                super.onViewHolderSelected(parent, child, position, subPosition)
                positions[key] = position
            }
        }
        recyclerView.addOnViewHolderSelectedListener(listener)
        listeners[key] = listener
    }

    fun register(recyclerView: HorizontalGridView, key: String, adapter: RecyclerView.Adapter<*>) {
        recyclerView.adapter = adapter
        val restoredPosition = positions[key] ?: 0
        recyclerView.selectedPosition = restoredPosition
        val listener = object : OnChildViewHolderSelectedListener() {
            override fun onChildViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subposition: Int
            ) {
                positions[key] = position
            }
        }
        recyclerView.addOnChildViewHolderSelectedListener(listener)
        leanbackListeners[key] = listener
    }

    fun unregister(recyclerView: HorizontalGridView, key: String) {
        leanbackListeners.remove(key)?.let { listener ->
            recyclerView.removeOnChildViewHolderSelectedListener(listener)
            recyclerView.selectedPosition = 0
        }
        recyclerView.adapter = null
    }

    fun unregister(recyclerView: DpadRecyclerView, key: String) {
        listeners.remove(key)?.let { listener ->
            recyclerView.removeOnViewHolderSelectedListener(listener)
            recyclerView.setSelectedPosition(position = 0)
        }
        recyclerView.adapter = null
    }


}
