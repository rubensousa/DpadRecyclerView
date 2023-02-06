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

package com.rubensousa.dpadrecyclerview.layoutmanager.focus

import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup

/**
 * Holds information about the previous focused spanIndex on all spanGroups
 */
class SpanFocusCache {

    private var spanCount = 1
    private val cache = SparseArrayCompat<Int>(initialCapacity = 0)
    private var focusedSpanSize = 1
    private var focusedSpanIndex = RecyclerView.NO_POSITION
    private var focusedSpanGroup = RecyclerView.NO_POSITION

    fun reset(newSpanCount: Int) {
        spanCount = newSpanCount
        cache.clear()
    }

    fun findNextSpanPosition(
        focusedPosition: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        forward: Boolean,
        itemCount: Int,
        reverseLayout: Boolean
    ): Int {
        val layoutDirection = if (forward) 1 else -1
        val targetSpanGroupIndex = focusedSpanGroup + layoutDirection

        val targetSpanIndex = cache[targetSpanGroupIndex] ?: return RecyclerView.NO_POSITION

        val spanDirection = getSpanDirection(layoutDirection, reverseLayout)

        var currentSpanGroupIndex = focusedSpanGroup
        var currentSpanIndex = if (spanDirection > 0) {
            focusedSpanIndex + focusedSpanSize - 1
        } else {
            focusedSpanIndex
        }
        var currentPosition = focusedPosition

        // Move to the target span group index
        while (currentSpanGroupIndex != targetSpanGroupIndex) {
            currentPosition = moveSpanIndexToTarget(
                currentPosition,
                currentSpanIndex,
                getSpanEndEdge(spanDirection),
                spanSizeLookup,
                spanDirection,
                layoutDirection
            )
            currentPosition += layoutDirection
            currentSpanGroupIndex += layoutDirection
            if (currentPosition >= itemCount || currentPosition < 0) {
                return RecyclerView.NO_POSITION
            }
            currentSpanIndex = getSpanStartEdge(spanDirection)
        }

        currentPosition = moveSpanIndexToTarget(
            currentPosition,
            currentSpanIndex,
            targetSpanIndex,
            spanSizeLookup,
            spanDirection,
            layoutDirection
        )

        if (currentPosition >= itemCount || currentPosition < 0) {
            return RecyclerView.NO_POSITION
        }

        return currentPosition
    }

    fun getSpanIndex(spanGroupIndex: Int): Int {
        return cache.get(spanGroupIndex, RecyclerView.NO_POSITION)
    }

    /**
     * Caches the new span focus if needed.
     */
    fun updateFocus(position: Int, spanSizeLookup: DpadSpanSizeLookup) {
        // Skip caching for single spans or even grids
        if (spanCount == 1 || spanSizeLookup === DpadSpanSizeLookup.DEFAULT) {
            return
        }
        val newSpanIndex = spanSizeLookup.getCachedSpanIndex(position, spanCount)
        val newSpanGroupIndex = spanSizeLookup.getCachedSpanGroupIndex(position, spanCount)
        val newSpanSize = spanSizeLookup.getSpanSize(position)

        val oldSpanIndex = focusedSpanIndex
        val oldSpanGroup = focusedSpanGroup
        val oldSpanSize = focusedSpanSize

        focusedSpanIndex = newSpanIndex
        focusedSpanGroup = newSpanGroupIndex
        focusedSpanSize = newSpanSize

        // There's no need to cache full spans, so exit early after caching the current span
        if (newSpanSize == spanCount) {
            // Cache the previous span index if it's valid
            cacheSpanFocus(oldSpanIndex, oldSpanGroup, oldSpanSize)
            return
        }

        // Skip caching for items that start at the same span index and have the same size
        if (newSpanIndex == oldSpanIndex && newSpanSize == oldSpanSize) {
            // We might have existing cache entries, so remove them
            cache.remove(oldSpanGroup)
            cache.remove(newSpanGroupIndex)
            return
        }

        // Just cache irregular items
        if (newSpanSize > 1) {
            // Cache the previous span index if it's valid
            cacheSpanFocus(oldSpanIndex, oldSpanGroup, oldSpanSize)
            save(spanGroupIndex = newSpanGroupIndex, spanIndex = newSpanIndex)
            return
        }

        cache.remove(oldSpanGroup)
        cache.remove(newSpanGroupIndex)
    }

    private fun save(spanGroupIndex: Int, spanIndex: Int) {
        cache.put(spanGroupIndex, spanIndex)
    }

    private fun cacheSpanFocus(spanIndex: Int, spanGroupIndex: Int, spanSize: Int) {
        if (spanSize == spanCount) {
            return
        }
        if (spanIndex != RecyclerView.NO_POSITION && spanGroupIndex != RecyclerView.NO_POSITION) {
            save(spanGroupIndex, spanIndex)
        }
    }

    private fun getSpanStartEdge(spanDirection: Int): Int {
        return if (spanDirection > 0) 0 else spanCount - 1
    }

    private fun getSpanEndEdge(spanDirection: Int): Int {
        return getSpanStartEdge(-spanDirection)
    }

    private fun getSpanDirection(layoutDirection: Int, reverseLayout: Boolean): Int {
        return if (!reverseLayout) {
            if (layoutDirection > 0) {
                1
            } else {
                -1
            }
        } else if (layoutDirection > 0) {
            -1
        } else {
            1
        }
    }

    private fun moveSpanIndexToTarget(
        position: Int,
        spanIndex: Int,
        targetSpanIndex: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        spanDirection: Int,
        layoutDirection: Int
    ): Int {
        if (spanIndex == targetSpanIndex) {
            return position
        }
        var currentSpanIndex = spanIndex
        var currentPosition = position
        while (currentSpanIndex != targetSpanIndex
            && currentSpanIndex >= 0
            && currentSpanIndex < spanCount
        ) {
            currentSpanIndex += spanSizeLookup.getSpanSize(position) * spanDirection
            currentPosition += layoutDirection
        }
        return currentPosition
    }

}
