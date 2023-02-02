/*
 * Copyright 2023 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider

import android.view.View
import androidx.collection.SparseArrayCompat
import androidx.collection.forEach
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ItemDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest

/**
 * A [ViewProvider] that gets the next views from the scrap.
 *
 * This is used for laying out disappearing views after the main layout stage.
 */
internal class ScrapViewProvider : ViewProvider {

    private var scrap: SparseArrayCompat<RecyclerView.ViewHolder>? = null

    override fun next(layoutRequest: LayoutRequest, state: RecyclerView.State): View? {
        val currentScrap = scrap ?: return null
        val nextViewHolder = findNextViewHolder(
            layoutRequest.currentPosition, layoutRequest.currentItemDirection
        ) ?: return null

        val currentPosition = nextViewHolder.layoutPosition
        currentScrap.remove(currentPosition)
        layoutRequest.setCurrentPosition(
            findNextScrapPosition(currentPosition, layoutRequest.currentItemDirection)
        )
        return nextViewHolder.itemView
    }

    fun update(newScrap: List<RecyclerView.ViewHolder>?) {
        if (newScrap == null) {
            this.scrap = null
            return
        }
        val newViewHolders = SparseArrayCompat<RecyclerView.ViewHolder>(newScrap.size)
        newScrap.forEach { viewHolder ->
            val layoutParams = viewHolder.itemView.layoutParams as RecyclerView.LayoutParams
            if (!layoutParams.isItemRemoved) {
                newViewHolders.put(viewHolder.layoutPosition, viewHolder)
            }
        }
        scrap = newViewHolders
    }

    fun updateLayoutPosition(layoutRequest: LayoutRequest) {
        layoutRequest.setCurrentPosition(
            findNextScrapPosition(layoutRequest.currentPosition, layoutRequest.currentItemDirection)
        )
    }

    fun getScrap(): SparseArrayCompat<RecyclerView.ViewHolder>? {
        return scrap
    }

    private fun findNextScrapPosition(currentPosition: Int, itemDirection: ItemDirection): Int {
        val nextViewHolder = findNextViewHolder(currentPosition, itemDirection)
        return nextViewHolder?.layoutPosition ?: RecyclerView.NO_POSITION
    }

    private fun findNextViewHolder(
        currentPosition: Int,
        itemDirection: ItemDirection,
    ): RecyclerView.ViewHolder? {
        val currentScrap = scrap ?: return null
        val viewHolderAtCurrentPosition = currentScrap[currentPosition]
        if (viewHolderAtCurrentPosition != null) {
            return viewHolderAtCurrentPosition
        }
        var closest: RecyclerView.ViewHolder? = null
        var closestDistance = Int.MAX_VALUE
        currentScrap.forEach { layoutPosition, viewHolder ->
            val distance = (layoutPosition - currentPosition) * itemDirection.value
            if (distance in 0 until closestDistance) {
                closest = viewHolder
                closestDistance = distance
            }
        }
        return closest
    }

}
