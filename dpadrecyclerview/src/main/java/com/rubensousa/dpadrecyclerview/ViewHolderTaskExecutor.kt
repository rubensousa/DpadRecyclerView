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
    private var pendingTask: ViewHolderTask? = null

    fun schedule(position: Int, task: ViewHolderTask) {
        targetPosition = position
        pendingTask = task
    }

    override fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        if (position == targetPosition
            && child != null
            && pendingTask?.executeWhenAligned == false
        ) {
            executePendingTask(child)
        }
    }

    override fun onViewHolderSelectedAndAligned(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        if (position == targetPosition
            && child != null
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