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

import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.sample.R

class DpadStateHolder {

    private val states = LinkedHashMap<String, Parcelable?>()
    private val selectionListener = object: OnViewHolderSelectedListener {
        override fun onViewHolderSelectedAndAligned(
            parent: RecyclerView,
            child: RecyclerView.ViewHolder?,
            position: Int,
            subPosition: Int
        ) {
            super.onViewHolderSelectedAndAligned(parent, child, position, subPosition)
            getKey(parent)?.let { scrollStateKey ->
                states[scrollStateKey] = parent.layoutManager?.onSaveInstanceState()
            }
        }
    }

    fun restore(
        recyclerView: DpadRecyclerView,
        key: String,
        adapter: RecyclerView.Adapter<*>
    ) {
        recyclerView.adapter = adapter
        states[key]?.let { savedState ->
            recyclerView.layoutManager?.onRestoreInstanceState(savedState)
        }
        setKey(recyclerView, key)
        recyclerView.addOnViewHolderSelectedListener(selectionListener)
    }

    fun save(recyclerView: DpadRecyclerView) {
        recyclerView.removeOnViewHolderSelectedListener(selectionListener)
        recyclerView.setSelectedPosition(0)
        recyclerView.adapter = null
    }

    private fun setKey(recyclerView: RecyclerView, key: String?) {
        recyclerView.setTag(R.id.dpadrecyclerview_state_key, key)
    }

    private fun getKey(recyclerView: RecyclerView) : String? {
        return recyclerView.getTag(R.id.dpadrecyclerview_state_key) as? String?
    }

}
