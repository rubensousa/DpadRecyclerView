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

package com.rubensousa.dpadrecyclerview.state

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.R

/**
 * Holds the scroll state of nested [DpadRecyclerView]. Use it to save and restore
 * the scroll state of all RecyclerViews in a single screen.
 */
class DpadScrollState internal constructor() {

    private val states = mutableMapOf<String, Parcelable>()
    private val selectionListener = object : OnViewHolderSelectedListener {
        override fun onViewHolderSelectedAndAligned(
            parent: RecyclerView,
            child: RecyclerView.ViewHolder?,
            position: Int,
            subPosition: Int
        ) {
            super.onViewHolderSelectedAndAligned(parent, child, position, subPosition)
            getScrollStateKey(parent)?.let { scrollStateKey ->
                saveOrClear(scrollStateKey, parent.layoutManager?.onSaveInstanceState())
            }
        }
    }

    private fun saveOrClear(key: String, state: Parcelable?) {
        if (state != null) {
            states[key] = state
        } else {
            states.remove(key)
        }
    }

    /**
     * Call this when the ViewHolder is recycled in [RecyclerView.Adapter.onViewRecycled].
     * This will clear the adapter by default to ensure that children are removed from the layout.
     * To disable this, pass false in [detachAdapter]
     * @param recyclerView RecyclerView to be saved
     * @param key unique identifier for [recyclerView]
     * @param detachAdapter true to detach the [RecyclerView.Adapter]
     * or false to skip that behavior. Default: true
     */
    fun save(
        recyclerView: DpadRecyclerView,
        key: String,
        detachAdapter: Boolean = true,
    ) {
        setScrollStateKey(recyclerView, key)
        saveOrClear(key, recyclerView.layoutManager?.onSaveInstanceState())
        recyclerView.removeOnViewHolderSelectedListener(selectionListener)
        if (detachAdapter) {
            recyclerView.adapter = null
        }
    }

    /**
     * Call this when the ViewHolder is bound and after adapter contents
     * are updated in [RecyclerView.Adapter.onBindViewHolder].
     *
     * Ensure that [adapter] contains the dataset before calling this method
     *
     * @param recyclerView RecyclerView to be restored
     * @param key unique identifier for [recyclerView]
     * @param adapter adapter to be bound to this RecyclerView
     */
    fun restore(
        recyclerView: DpadRecyclerView,
        key: String,
        adapter: RecyclerView.Adapter<*>
    ) {
        setScrollStateKey(recyclerView, key)
        recyclerView.adapter = adapter
        states[key]?.let { previousState ->
            recyclerView.layoutManager?.onRestoreInstanceState(previousState)
        }
        // Prevent duplicate registration
        recyclerView.removeOnViewHolderSelectedListener(selectionListener)
        recyclerView.addOnViewHolderSelectedListener(selectionListener)
    }

    /**
     * @param key RecyclerView identifier of which state should be removed
     */
    fun clear(key: String) {
        states.remove(key)
    }

    /**
     * Clears all scroll states to prevent them from being restored later
     */
    fun clear() {
        states.clear()
    }

    internal fun saveState(): Bundle {
        val bundle = Bundle()
        states.entries.forEach { entry ->
            bundle.putParcelable(entry.key, entry.value)
        }
        return bundle
    }

    internal fun restoreState(bundle: Bundle) {
        bundle.keySet().forEach { key ->
            bundle.getParcelableCompat(key)?.let { state ->
                states[key] = state
            }
        }
    }

    private fun setScrollStateKey(recyclerView: RecyclerView, key: String?) {
        recyclerView.setTag(R.id.dpadrecyclerview_scroll_state_key, key)
    }

    private fun getScrollStateKey(recyclerView: RecyclerView): String? {
        return recyclerView.getTag(R.id.dpadrecyclerview_scroll_state_key) as? String?
    }

    private fun Bundle.getParcelableCompat(key: String): Parcelable? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelable(key, Parcelable::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelable(key)
        }
    }

}
