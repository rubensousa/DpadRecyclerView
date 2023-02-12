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
internal class SpanFocusFinder {

    private var spanCount = 1
    private var cachedSpanIndex = RecyclerView.NO_POSITION
    private var cachedSpanSize = 1

    fun setSpanCount(newSpanCount: Int) {
        spanCount = newSpanCount
        cachedSpanIndex = RecyclerView.NO_POSITION
        cachedSpanSize = 1
    }

    /**
     * Caches the new span focus if needed.
     */
    fun save(position: Int, spanSizeLookup: DpadSpanSizeLookup) {
        // Skip caching for single spans
        if (spanCount == 1 || spanSizeLookup === DpadSpanSizeLookup.DEFAULT) {
            return
        }

        val newSpanIndex = spanSizeLookup.getCachedSpanIndex(position, spanCount)
        val newSpanSize = spanSizeLookup.getSpanSize(position)

        // There's no need to cache full spans, so exit early
        if (newSpanSize == spanCount) {
            return
        }

        cachedSpanIndex = newSpanIndex
        cachedSpanSize = newSpanSize
    }

    fun findNextSpanPosition(
        focusedPosition: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        forward: Boolean,
        edgePosition: Int,
        reverseLayout: Boolean
    ): Int {
        if (spanCount == 1) {
            return RecyclerView.NO_POSITION
        }
        val positionDirection = if (forward) 1 else -1
        val spanDirection = getSpanDirection(forward, reverseLayout)
        if (spanSizeLookup === DpadSpanSizeLookup.DEFAULT) {
            return findNextEvenSpanPosition(
                spanSizeLookup, focusedPosition, edgePosition, positionDirection
            )
        }
        val focusedSpanIndex = spanSizeLookup.getCachedSpanIndex(focusedPosition, spanCount)
        val focusedSpanSize = spanSizeLookup.getSpanSize(focusedPosition)
        val currentSpanIndex = focusedSpanIndex + focusedSpanSize * spanDirection - spanDirection

        // Move position to the start of the next span group
        val firstPositionInNextSpanGroup = moveToStartOfNextSpanGroup(
            focusedPosition, currentSpanIndex, spanSizeLookup, spanDirection, positionDirection
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
            if (isPositionAtCachedSpan(currentPosition, spanSizeLookup, spanDirection)) {
                return currentPosition
            }
            currentPosition += positionDirection
        }

        return firstPositionInNextSpanGroup
    }

    fun getCachedSpanIndex(): Int {
        return cachedSpanIndex
    }

    private fun findNextEvenSpanPosition(
        spanSizeLookup: DpadSpanSizeLookup,
        focusedPosition: Int,
        edgePosition: Int,
        positionDirection: Int
    ): Int {
        val nextPosition = focusedPosition + spanCount * positionDirection
        if (nextPosition <= edgePosition && edgePosition > focusedPosition) {
            return nextPosition
        }
        if (nextPosition >= edgePosition && edgePosition < focusedPosition) {
            return nextPosition
        }
        val focusedSpanGroup = spanSizeLookup.getSpanGroupIndex(focusedPosition, spanCount)
        val edgeSpanGroup = spanSizeLookup.getSpanGroupIndex(edgePosition, spanCount)
        // There's no way to go from here
        if (focusedSpanGroup == edgeSpanGroup) {
            return RecyclerView.NO_POSITION
        }
        return edgePosition
    }

    private fun isPositionAtCachedSpan(
        position: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        spanDirection: Int
    ): Boolean {
        val spanIndex = spanSizeLookup.getCachedSpanIndex(position, spanCount)
        return if (spanDirection > 0) {
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
        positionDirection: Int,
    ): Int {
        val targetSpanIndex = getEndSpanIndex(spanDirection)
        val position = moveSpanIndexToTarget(
            currentPosition,
            currentSpanIndex,
            targetSpanIndex,
            spanSizeLookup,
            spanDirection,
            positionDirection
        )
        return position + positionDirection
    }

    private fun isPositionOutOfBounds(position: Int, edgePosition: Int, forward: Boolean): Boolean {
        return (position > edgePosition && forward) || (position < edgePosition && !forward)
    }

    private fun getStartSpanIndex(spanDirection: Int): Int {
        return if (spanDirection > 0) 0 else spanCount - 1
    }

    private fun getEndSpanIndex(spanDirection: Int): Int {
        return getStartSpanIndex(-spanDirection)
    }

    private fun getSpanDirection(forward: Boolean, reverseLayout: Boolean): Int {
        val layoutDirection = if (forward != reverseLayout) 1 else -1
        return if (!reverseLayout) {
            if (layoutDirection > 0) 1 else -1
        } else {
            if (layoutDirection > 0) -1 else 1
        }
    }

    private fun moveSpanIndexToTarget(
        position: Int,
        spanIndex: Int,
        targetSpanIndex: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        spanDirection: Int,
        positionDirection: Int
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
            currentPosition += positionDirection
        }
        return currentPosition
    }

}
