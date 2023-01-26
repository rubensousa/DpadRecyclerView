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

package com.rubensousa.dpadrecyclerview

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.LinkedList
import java.util.Queue

/**
 * A [RecyclerView.RecycledViewPool] that does not limit the number of ViewHolders recycled.
 *
 * This is meant to be shared across different [RecyclerView] to minimise ViewHolder inflation time
 * and memory consumption, since [RecyclerView] will only create and bind
 * the absolute minimum number of ViewHolders it really needs.
 */
class UnboundViewPool : RecyclerView.RecycledViewPool() {

    private val viewHolderQueues = SparseArrayCompat<LinkedList<RecyclerView.ViewHolder>>()

    override fun clear() {
        viewHolderQueues.clear()
    }

    override fun setMaxRecycledViews(viewType: Int, max: Int) {
        // Not supported by default
    }

    override fun getRecycledView(viewType: Int): RecyclerView.ViewHolder? {
        return viewHolderQueues[viewType]?.poll()
    }

    override fun getRecycledViewCount(viewType: Int): Int {
        return viewHolderQueues[viewType]?.size ?: 0
    }

    override fun putRecycledView(viewHolder: RecyclerView.ViewHolder) {
        getOrCreateQueue(viewHolder.itemViewType).add(viewHolder)
    }

    private fun getOrCreateQueue(viewType: Int): Queue<RecyclerView.ViewHolder> {
        var queue = viewHolderQueues[viewType]
        if (queue == null) {
            queue = LinkedList()
            viewHolderQueues.put(viewType, queue)
        }
        return queue
    }

}
