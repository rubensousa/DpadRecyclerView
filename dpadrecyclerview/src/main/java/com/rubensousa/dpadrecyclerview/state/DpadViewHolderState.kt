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

import android.os.Parcelable
import android.util.SparseArray
import androidx.recyclerview.widget.RecyclerView

/**
 * Holds the view hierarchy state of some [RecyclerView.ViewHolder].
 * Use [save] to save a [RecyclerView.ViewHolder] and [restore] to restore its state.
 *
 * Consider using this when you need to persist some View state (e.g text input)
 * inside your ViewHolders.
 */
class DpadViewHolderState internal constructor(
    private val hierarchyState: MutableMap<String, SparseArray<Parcelable>>
) {

    /**
     * @param holder ViewHolder to be saved
     * @param key unique identifier for [holder]
     */
    fun save(holder: RecyclerView.ViewHolder, key: String) {
        val container = SparseArray<Parcelable>()
        holder.itemView.saveHierarchyState(container)
        hierarchyState[key] = container
    }

    /**
     * @param holder ViewHolder to be restored
     * @param key unique identifier for [holder]
     * @param consume true to prevent this state from being restored multiple times,
     * or false to still keep it. Default: false
     */
    fun restore(
        holder: RecyclerView.ViewHolder,
        key: String,
        consume: Boolean = false
    ) {
        val container = if (consume) {
            hierarchyState.remove(key)
        } else {
            hierarchyState[key]
        }
        container?.let {
            holder.itemView.restoreHierarchyState(it)
        }
    }

    /**
     * @param key ViewHolder identifier of which state should be removed
     */
    fun clear(key: String) {
        hierarchyState.remove(key)
    }

}
