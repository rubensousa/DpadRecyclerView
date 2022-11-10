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

package com.rubensousa.dpadrecyclerview.internal.layoutmanager

import android.util.SparseIntArray
import androidx.recyclerview.widget.GridLayoutManager

/**
 * A helper class to provide the number of spans each item occupies.
 *
 * Default implementation sets each item to occupy exactly 1 span.
 *
 * Extracted from: [GridLayoutManager.SpanSizeLookup] to access package protected methods
 */
abstract class TvSpanSizeLookup {

    companion object {

        @JvmStatic
        fun findFirstKeyLessThan(cache: SparseIntArray, position: Int): Int {
            var lo = 0
            var hi = cache.size() - 1
            while (lo <= hi) {
                // Using unsigned shift here to divide by two because it is guaranteed to not
                // overflow.
                val mid = lo + hi ushr 1
                val midVal = cache.keyAt(mid)
                if (midVal < position) {
                    lo = mid + 1
                } else {
                    hi = mid - 1
                }
            }
            val index = lo - 1
            return if (index >= 0 && index < cache.size()) {
                cache.keyAt(index)
            } else -1
        }

        @JvmStatic
        fun default(): TvSpanSizeLookup {
            return object : TvSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int = 1
                override fun getSpanIndex(position: Int, spanCount: Int): Int {
                    return position % spanCount
                }
            }
        }
    }

    private val spanIndexCache = SparseIntArray()
    private val spanGroupIndexCache = SparseIntArray()
    private var cacheSpanIndices = false
    private var cacheSpanGroupIndices = false

    /**
     * Returns the number of span occupied by the item at `position`.
     *
     * @param position The adapter position of the item
     * @return The number of spans occupied by the item at the provided position
     */
    abstract fun getSpanSize(position: Int): Int

    fun setSpanIndexCacheEnabled(enabled: Boolean) {
        if (!enabled) {
            spanGroupIndexCache.clear()
        }
        cacheSpanIndices = enabled
    }

    fun setSpanGroupIndexCacheEnabled(enabled: Boolean) {
        if (!enabled) {
            spanGroupIndexCache.clear()
        }
        cacheSpanGroupIndices = enabled
    }


    /**
     * Clears the span index cache. GridLayoutManager automatically calls this method when
     * adapter changes occur.
     */
    fun invalidateSpanIndexCache() {
        spanIndexCache.clear()
    }

    /**
     * Clears the span group index cache. GridLayoutManager automatically calls this method
     * when adapter changes occur.
     */
    fun invalidateSpanGroupIndexCache() {
        spanGroupIndexCache.clear()
    }

    fun getCachedSpanIndex(position: Int, spanCount: Int): Int {
        if (!cacheSpanIndices) {
            return getSpanIndex(position, spanCount)
        }
        val existing = spanIndexCache[position, -1]
        if (existing != -1) {
            return existing
        }
        val value = getSpanIndex(position, spanCount)
        spanIndexCache.put(position, value)
        return value
    }

    fun getCachedSpanGroupIndex(position: Int, spanCount: Int): Int {
        if (!cacheSpanGroupIndices) {
            return getSpanGroupIndex(position, spanCount)
        }
        val existing = spanGroupIndexCache[position, -1]
        if (existing != -1) {
            return existing
        }
        val value = getSpanGroupIndex(position, spanCount)
        spanGroupIndexCache.put(position, value)
        return value
    }

    open fun getSpanIndex(position: Int, spanCount: Int): Int {
        val positionSpanSize = getSpanSize(position)
        if (positionSpanSize == spanCount) {
            return 0 // quick return for full-span items
        }
        var span = 0
        var startPos = 0
        // If caching is enabled, try to jump
        if (cacheSpanIndices) {
            val prevKey = findFirstKeyLessThan(spanIndexCache, position)
            if (prevKey >= 0) {
                span = spanIndexCache[prevKey] + getSpanSize(prevKey)
                startPos = prevKey + 1
            }
        }
        for (i in startPos until position) {
            val size = getSpanSize(i)
            span += size
            if (span == spanCount) {
                span = 0
            } else if (span > spanCount) {
                // did not fit, moving to next row / column
                span = size
            }
        }
        return if (span + positionSpanSize <= spanCount) {
            span
        } else 0
    }

    fun getSpanGroupIndex(adapterPosition: Int, spanCount: Int): Int {
        var span = 0
        var group = 0
        var start = 0
        if (cacheSpanGroupIndices) {
            // This finds the first non empty cached group cache key.
            val prevKey = findFirstKeyLessThan(spanGroupIndexCache, adapterPosition)
            if (prevKey != -1) {
                group = spanGroupIndexCache[prevKey]
                start = prevKey + 1
                span = getCachedSpanIndex(prevKey, spanCount) + getSpanSize(prevKey)
                if (span == spanCount) {
                    span = 0
                    group++
                }
            }
        }
        val positionSpanSize = getSpanSize(adapterPosition)
        for (i in start until adapterPosition) {
            val size = getSpanSize(i)
            span += size
            if (span == spanCount) {
                span = 0
                group++
            } else if (span > spanCount) {
                // did not fit, moving to next row / column
                span = size
                group++
            }
        }
        if (span + positionSpanSize > spanCount) {
            group++
        }
        return group
    }

}
