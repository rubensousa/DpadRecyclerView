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

package com.rubensousa.dpadrecyclerview.test.tests

import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DiffUtil.DiffResult
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadDragHelper
import java.util.Collections
import java.util.concurrent.Executors

abstract class AbstractTestAdapter<VH : RecyclerView.ViewHolder>(
    numberOfItems: Int
) : RecyclerView.Adapter<VH>(), DpadDragHelper.DragAdapter<Int> {

    companion object {
        private val EMPTY_LIST = ArrayList<Int>(0)
        private val BACKGROUND_EXECUTOR = Executors.newFixedThreadPool(4)
        private val MAIN_THREAD_HANDLER = Handler(Looper.getMainLooper())
    }

    private val itemCallback = object : DiffUtil.ItemCallback<Int>() {
        override fun areItemsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
        override fun areContentsTheSame(oldItem: Int, newItem: Int) = oldItem == newItem
    }
    private var items = MutableList(numberOfItems) { it }
    private var currentVersion = 0
    private var id = items.size

    override fun getMutableItems(): MutableList<Int> = items

    fun submitList(newList: MutableList<Int>, commitCallback: Runnable? = null) {
        val version = ++currentVersion
        if (items === newList) {
            commitCallback?.run()
            return
        }

        if (newList.isEmpty()) {
            val removed = items.size
            items = EMPTY_LIST
            notifyItemRangeRemoved(0, removed)
            return
        }

        if (items.isEmpty()) {
            items = newList
            notifyItemRangeInserted(0, newList.size)
            return
        }

        val oldList = items
        BACKGROUND_EXECUTOR.execute {
            val diffResult = calculateDiff(oldList, newList)
            MAIN_THREAD_HANDLER.post {
                if (version == currentVersion) {
                    latchList(newList, diffResult, commitCallback)
                }
            }
        }
    }

    fun setList(newList: MutableList<Int>) {
        currentVersion++
        items = newList
    }

    private fun calculateDiff(
        oldList: List<Int>,
        newList: List<Int>,
    ): DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return itemCallback.areItemsTheSame(oldItem, newItem)
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int,
            ): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return itemCallback.areContentsTheSame(oldItem, newItem)
            }
        })
    }

    private fun latchList(
        newList: MutableList<Int>,
        result: DiffResult,
        commitCallback: Runnable?,
    ) {
        items = newList
        result.dispatchUpdatesTo(this)
        commitCallback?.run()
    }

    fun removeAt(index: Int) {
        currentVersion++
        items.removeAt(index)
        notifyItemRemoved(index)
    }

    fun removeFrom(index: Int, count: Int) {
        currentVersion++
        repeat(count) {
            items.removeAt(index)
        }
        notifyItemRangeRemoved(index, count)
    }


    fun move(from: Int, to: Int) {
        currentVersion++
        Collections.swap(items, from, to)
        notifyItemMoved(from, to)
    }

    fun addAt(index: Int, item: Int) {
        currentVersion++
        items.add(index, item)
        notifyItemInserted(index)
    }

    fun add() {
        currentVersion++
        items.add(id++)
        notifyItemInserted(items.size - 1)
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int) = items[position]

}
