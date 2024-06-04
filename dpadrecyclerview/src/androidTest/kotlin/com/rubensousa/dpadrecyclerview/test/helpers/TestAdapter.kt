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

package com.rubensousa.dpadrecyclerview.test.helpers

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections
import java.util.concurrent.Executors

abstract class TestAdapter<VH : RecyclerView.ViewHolder>: RecyclerView.Adapter<VH>() {

    companion object {
        private val BACKGROUND_EXECUTOR = Executors.newFixedThreadPool(4)
        private val MAIN_THREAD_HANDLER = Handler(Looper.getMainLooper())
    }

    private var items: MutableList<Int> = Collections.emptyList()
    private var currentVersion = 0

    @SuppressLint("NotifyDataSetChanged")
    fun replaceList(items: MutableList<Int>) {
        this.items = items
        currentVersion++
        notifyDataSetChanged()
    }

    fun submitList(newList: MutableList<Int>, commitCallback: Runnable? = null) {
        val version = ++currentVersion
        if (items === newList) {
            commitCallback?.run()
            return
        }

        if (newList.isEmpty()) {
            val removed = items.size
            items = Collections.emptyList()
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

    private fun calculateDiff(
        oldList: List<Int>,
        newList: List<Int>
    ): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = oldList.size
            override fun getNewListSize(): Int = newList.size

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem == newItem
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                val oldItem = oldList[oldItemPosition]
                val newItem = newList[newItemPosition]
                return oldItem == newItem
            }
        })
    }

    private fun latchList(
        newList: MutableList<Int>,
        result: DiffUtil.DiffResult,
        commitCallback: Runnable?
    ) {
        items = newList
        result.dispatchUpdatesTo(this)
        commitCallback?.run()
    }

    fun removeAt(index: Int) {
        if (index >= 0 && index < items.size) {
            currentVersion++
            items.removeAt(index)
            notifyItemRemoved(index)
        }

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

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int) = items[position]
}