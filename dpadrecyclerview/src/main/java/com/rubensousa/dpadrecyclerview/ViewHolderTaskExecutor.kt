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

import androidx.recyclerview.widget.RecyclerView

internal class ViewHolderTaskExecutor : OnViewHolderSelectedListener {

    private var targetPosition = RecyclerView.NO_POSITION
    private var targetSubPosition = RecyclerView.NO_POSITION
    private var pendingTask: ViewHolderTask? = null

    fun schedule(position: Int, task: ViewHolderTask) {
        targetPosition = position
        targetSubPosition = RecyclerView.NO_POSITION
        pendingTask = task
    }

    fun schedule(position: Int, subPosition: Int, task: ViewHolderTask) {
        targetPosition = position
        targetSubPosition = subPosition
        pendingTask = task
    }

    override fun onViewHolderSelected(
        parent: DpadRecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        if (position == targetPosition
            && child != null
            && (targetSubPosition == RecyclerView.NO_POSITION || targetSubPosition == subPosition)
            && pendingTask?.executeWhenAligned == false
        ) {
            executePendingTask(child)
        }
    }

    override fun onViewHolderSelectedAndAligned(
        parent: DpadRecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        if (position == targetPosition
            && child != null
            && (targetSubPosition == RecyclerView.NO_POSITION || targetSubPosition == subPosition)
            && pendingTask?.executeWhenAligned == true
        ) {
            executePendingTask(child)
        }
    }

    private fun executePendingTask(viewHolder: RecyclerView.ViewHolder) {
        pendingTask?.execute(viewHolder)
        pendingTask = null
        targetPosition = RecyclerView.NO_POSITION
    }

}