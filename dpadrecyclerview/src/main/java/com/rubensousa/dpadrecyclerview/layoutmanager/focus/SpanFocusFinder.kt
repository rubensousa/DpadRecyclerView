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

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup

/**
 * Holds information about the previous focused spanIndex on all spanGroups
 */
class SpanFocusFinder {

    private var spanCount = 1
    private var cachedSpanIndex = RecyclerView.NO_POSITION
    private var cachedSpanSize = 1
    private var focusedSpanIndex = RecyclerView.NO_POSITION
    private var focusedSpanSize = 1

    fun reset(newSpanCount: Int) {
        spanCount = newSpanCount
        cachedSpanIndex = RecyclerView.NO_POSITION
        cachedSpanSize = 1
        focusedSpanSize = 1
        focusedSpanIndex = RecyclerView.NO_POSITION
    }

    fun findNextSpanPosition(
        focusedPosition: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        forward: Boolean,
        edgePosition: Int,
        reverseLayout: Boolean
    ): Int {
        val layoutDirection = if (forward) 1 else -1
        val spanDirection = getSpanDirection(layoutDirection, reverseLayout)
        val currentSpanIndex = focusedSpanIndex + focusedSpanSize * spanDirection - spanDirection

        // Move position to the start of the next span group
        val firstPositionInNextSpanGroup = moveToStartOfNextSpanGroup(
            focusedPosition, currentSpanIndex, spanSizeLookup, spanDirection, layoutDirection
        )
        var currentPosition = firstPositionInNextSpanGroup

        if (isPositionOutOfBounds(currentPosition, edgePosition, forward)) {
            return RecyclerView.NO_POSITION
        }

        // 1. If there's no cache, just return the current position that's sitting on an edge
        // 2. If the item takes the entire size, just return it since there's no other valid option
        if (cachedSpanIndex == RecyclerView.NO_POSITION
            || spanSizeLookup.getSpanSize(currentPosition) == spanCount
        ) {
            return currentPosition
        }

        // Now search until we find the cached span index or we go outside the edge
        while (!isPositionOutOfBounds(currentPosition, edgePosition, forward)) {
            if (isPositionAtCachedSpan(
                    currentPosition,
                    spanSizeLookup,
                    spanDirection,
                    reverseLayout
                )
            ) {
                return currentPosition
            }
            currentPosition += layoutDirection
        }

        return firstPositionInNextSpanGroup
    }

    fun getCachedSpanIndex(): Int {
        return cachedSpanIndex
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
        val newSpanSize = spanSizeLookup.getSpanSize(position)

        focusedSpanIndex = newSpanIndex
        focusedSpanSize = newSpanSize

        // There's no need to cache full spans, so exit early
        if (newSpanSize == spanCount) {
            return
        }

        cachedSpanIndex = newSpanIndex
        cachedSpanSize = newSpanSize
    }

    private fun isPositionAtCachedSpan(
        position: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        spanDirection: Int,
        reverseLayout: Boolean
    ): Boolean {
        val spanIndex = spanSizeLookup.getCachedSpanIndex(position, spanCount)
        return if (spanDirection > 0 != reverseLayout) {
            spanIndex >= cachedSpanIndex
        } else {
            spanIndex <= cachedSpanIndex
        }
    }

    private fun moveToStartOfNextSpanGroup(
        currentPosition: Int,
        currentSpanIndex: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        spanDirection: Int,
        layoutDirection: Int,
    ): Int {
        val targetSpanIndex = getSpanEndEdge(spanDirection)
        val position = moveSpanIndexToTarget(
            currentPosition,
            currentSpanIndex,
            targetSpanIndex,
            spanSizeLookup,
            spanDirection,
            layoutDirection
        )
        return position + layoutDirection
    }

    private fun isPositionOutOfBounds(position: Int, edgePosition: Int, forward: Boolean): Boolean {
        return (position > edgePosition && forward) || (position < edgePosition && !forward)
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
