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
import android.util.SparseArray
import androidx.recyclerview.widget.RecyclerView

/**
 * Holds the view hierarchy state of some [RecyclerView.ViewHolder].
 * Use [saveState] to save a [RecyclerView.ViewHolder] and [restoreState] to restore its state.
 *
 * Consider using this when you need to persist some View state (e.g text input)
 * inside your ViewHolders.
 */
class DpadViewHolderState internal constructor() {

    private val states = mutableMapOf<String, SparseArray<Parcelable>>()

    /**
     * @param holder ViewHolder to be saved
     * @param key unique identifier for [holder]
     */
    fun saveState(holder: RecyclerView.ViewHolder, key: String) {
        val container = SparseArray<Parcelable>()
        holder.itemView.saveHierarchyState(container)
        states[key] = container
    }

    /**
     * @param holder ViewHolder to be restored
     * @param key unique identifier for [holder]
     * @param consume true to prevent this state from being restored multiple times,
     * or false to still keep it. Default: false
     */
    fun restoreState(
        holder: RecyclerView.ViewHolder,
        key: String,
        consume: Boolean = false
    ) {
        val container = if (consume) {
            states.remove(key)
        } else {
            states[key]
        }
        container?.let {
            holder.itemView.restoreHierarchyState(it)
        }
    }

    /**
     * @param key ViewHolder identifier of which state should be removed
     */
    fun clear(key: String) {
        states.remove(key)
    }

    /**
     * Clears all ViewHolder states to prevent them from being restored later
     */
    fun clear() {
        states.clear()
    }

    internal fun saveState(): Bundle {
        val bundle = Bundle()
        states.entries.forEach { entry ->
            bundle.putSparseParcelableArray(entry.key, entry.value)
        }
        return bundle
    }

    internal fun restoreState(bundle: Bundle) {
        bundle.keySet().forEach { key ->
            bundle.getSparseParcelableArrayCompat(key)?.let { state ->
                states[key] = state
            }
        }
    }

    private fun Bundle.getSparseParcelableArrayCompat(key: String): SparseArray<Parcelable>? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSparseParcelableArray(key, Parcelable::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSparseParcelableArray(key)
        }
    }

}
