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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import android.util.SparseIntArray
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams

internal class GridState(private val layoutManager: LayoutManager) {

    private val preLayoutSpanSizeCache = SparseIntArray()
    private val preLayoutSpanIndexCache = SparseIntArray()
    private val preLayoutSpanGroupIndexCache = SparseIntArray()

    fun getSpanGroupIndex(position: Int): Int {
        return preLayoutSpanGroupIndexCache.get(position, -1)
    }

    fun getSpanIndex(position: Int): Int {
        return preLayoutSpanIndexCache.get(position, -1)
    }

    fun getSpanSize(position: Int): Int {
        return preLayoutSpanSizeCache.get(position, -1)
    }

    fun save() {
        val childCount = layoutManager.childCount
        for (i in 0 until childCount) {
            val view = layoutManager.getChildAt(i) ?: continue
            val layoutParams = view.layoutParams as DpadLayoutParams
            val viewPosition = layoutParams.viewLayoutPosition
            preLayoutSpanSizeCache.put(viewPosition, layoutParams.spanSize)
            preLayoutSpanIndexCache.put(viewPosition, layoutParams.spanIndex)
            preLayoutSpanGroupIndexCache.put(viewPosition, layoutParams.spanGroupIndex)
        }
    }

    fun clear() {
        preLayoutSpanSizeCache.clear()
        preLayoutSpanIndexCache.clear()
    }

}
