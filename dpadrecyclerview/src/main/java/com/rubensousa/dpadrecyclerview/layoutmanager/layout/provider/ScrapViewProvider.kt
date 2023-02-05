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
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest

/**
 * A [ViewProvider] that gets the next views from the scrap.
 *
 * This is used for laying out disappearing views after the main layout stage.
 */
internal class ScrapViewProvider : ViewProvider {

    private val scrap = SparseArrayCompat<RecyclerView.ViewHolder>()

    override fun hasNext(layoutRequest: LayoutRequest, state: RecyclerView.State): Boolean {
        if (scrap.isEmpty()) {
            return false
        }
        val nextViewHolder = findNextViewHolder(layoutRequest) ?: return false
        /**
         * Cache the ViewHolder in its expected position for faster lookups in [next]
         */
        scrap.remove(nextViewHolder.layoutPosition)
        scrap.put(layoutRequest.currentPosition, nextViewHolder)
        return true
    }

    override fun next(layoutRequest: LayoutRequest, state: RecyclerView.State): View {
        val nextViewHolder = findNextViewHolder(layoutRequest) ?: throw IllegalStateException()
        scrap.remove(layoutRequest.currentPosition)
        setNextLayoutPosition(layoutRequest)
        return nextViewHolder.itemView
    }

    fun updateScrap(newScrap: List<RecyclerView.ViewHolder>?) {
        scrap.clear()
        newScrap?.forEach { viewHolder ->
            val layoutParams = viewHolder.itemView.layoutParams as RecyclerView.LayoutParams
            if (!layoutParams.isItemRemoved) {
                scrap.put(viewHolder.layoutPosition, viewHolder)
            }
        }
    }

    fun setNextLayoutPosition(layoutRequest: LayoutRequest) {
        val nextViewHolder = findNextViewHolder(layoutRequest)
        if (nextViewHolder == null) {
            layoutRequest.setCurrentPosition(RecyclerView.NO_POSITION)
            return
        }
        layoutRequest.setCurrentPosition(nextViewHolder.layoutPosition)
    }

    fun getScrap(): SparseArrayCompat<RecyclerView.ViewHolder> {
        return scrap
    }

    private fun findNextViewHolder(layoutRequest: LayoutRequest): RecyclerView.ViewHolder? {
        val viewHolderAtCurrentPosition = scrap[layoutRequest.currentPosition]
        if (viewHolderAtCurrentPosition != null) {
            return viewHolderAtCurrentPosition
        }
        var closest: RecyclerView.ViewHolder? = null
        var closestDistance = Int.MAX_VALUE
        scrap.forEach { layoutPosition, viewHolder ->
            val distance = ((layoutPosition - layoutRequest.currentPosition)
                    * layoutRequest.currentItemDirection.value)
            if (distance in 0 until closestDistance) {
                closest = viewHolder
                closestDistance = distance
            }
        }
        return closest
    }

}
