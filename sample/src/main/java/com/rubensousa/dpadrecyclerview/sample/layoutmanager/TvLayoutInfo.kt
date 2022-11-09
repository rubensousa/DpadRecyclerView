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

package com.rubensousa.dpadrecyclerview.sample.layoutmanager

import android.annotation.SuppressLint
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

@SuppressLint("LogNotTimber")
class TvLayoutInfo(private val configuration: TvLayoutConfiguration) {

    private var spanSizeLookup: TvSpanSizeLookup = TvSpanSizeLookup.default()
    private var dpadRecyclerView: DpadRecyclerView? = null

    fun setRecyclerView(recyclerView: DpadRecyclerView?) {
        dpadRecyclerView = recyclerView
    }

    fun getColumnIndex(position: Int): Int {
        return spanSizeLookup.getSpanIndex(position, configuration.spanCount)
    }

    fun getEndColumnIndex(position: Int): Int {
        return getColumnIndex(position) + spanSizeLookup.getSpanSize(position) - 1
    }

    fun getRowIndex(position: Int): Int {
        return spanSizeLookup.getSpanGroupIndex(position, configuration.spanCount)
    }

    fun setSpanSizeLookup(lookup: TvSpanSizeLookup) {
        spanSizeLookup = lookup
    }

    fun getSpanGroupIndex(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        viewPosition: Int
    ): Int {
        if (!state.isPreLayout) {
            return spanSizeLookup.getCachedSpanGroupIndex(viewPosition, configuration.spanCount)
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(viewPosition)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            Log.w(
                DpadRecyclerView.TAG, "Cannot find span size for pre layout position. $viewPosition"
            )
            return 0
        }
        return spanSizeLookup.getCachedSpanGroupIndex(adapterPosition, configuration.spanCount)
    }

    fun isItemFullyVisible(position: Int): Boolean {
        val recyclerView = dpadRecyclerView ?: return false
        val itemView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
            ?: return false
        return itemView.left >= 0
                && itemView.right <= recyclerView.width
                && itemView.top >= 0
                && itemView.bottom <= recyclerView.height
    }


}
