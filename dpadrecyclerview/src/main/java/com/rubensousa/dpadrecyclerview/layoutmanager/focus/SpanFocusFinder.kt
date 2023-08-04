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
        var positionDirection = if (forward) 1 else -1
        if (spanSizeLookup === DpadSpanSizeLookup.DEFAULT) {
            return findNextEvenSpanPosition(
                spanSizeLookup, focusedPosition, edgePosition, positionDirection
            )
        }
        val focusedSpanIndex = spanSizeLookup.getCachedSpanIndex(focusedPosition, spanCount)

        val firstPositionInNextSpanGroup = moveToStartOfNextSpanGroup(
            position = focusedPosition,
            spanIndex = focusedSpanIndex,
            lookup = spanSizeLookup,
            spanDir = getSpanDirection(forward, reverseLayout),
            posDir = positionDirection,
            edgePosition = edgePosition,
            forward = forward,
            reverseLayout = reverseLayout
        )

        if (firstPositionInNextSpanGroup == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        var currentPosition = firstPositionInNextSpanGroup

        // 1. If there's no cache, just return the current position that's sitting on an edge
        // 2. If the item takes the entire size, just return it since there's no other valid option
        if (cachedSpanIndex == RecyclerView.NO_POSITION
            || spanSizeLookup.getSpanSize(currentPosition) == spanCount
        ) {
            return currentPosition
        }

        positionDirection = if (forward || reverseLayout) {
            positionDirection
        } else {
            positionDirection * -1
        }

        // Now search until we find the cached span index or we go outside the edge
        while (!isPositionOutOfBounds(currentPosition, edgePosition, forward)) {
            if (isPositionAtCachedSpan(currentPosition, spanSizeLookup, reverseLayout)) {
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
        val focusedSpanGroup = spanSizeLookup.getSpanGroupIndex(focusedPosition, spanCount)
        val edgeSpanGroup = spanSizeLookup.getSpanGroupIndex(edgePosition, spanCount)
        // There's no way to go from here
        if (focusedSpanGroup == edgeSpanGroup) {
            return RecyclerView.NO_POSITION
        }
        val nextPosition = focusedPosition + spanCount * positionDirection
        if (nextPosition >= edgePosition && positionDirection < 0) {
            return nextPosition
        }
        if (nextPosition <= edgePosition && positionDirection > 0) {
            return nextPosition
        }
        return edgePosition
    }

    private fun isPositionAtCachedSpan(
        position: Int,
        spanSizeLookup: DpadSpanSizeLookup,
        reverseLayout: Boolean,
    ): Boolean {
        val spanIndex = spanSizeLookup.getCachedSpanIndex(position, spanCount)
        return if (!reverseLayout) {
            spanIndex >= cachedSpanIndex
        } else {
            spanIndex <= cachedSpanIndex
        }
    }

    private fun moveToStartOfNextSpanGroup(
        position: Int,
        spanIndex: Int,
        lookup: DpadSpanSizeLookup,
        spanDir: Int,
        posDir: Int,
        edgePosition: Int,
        forward: Boolean,
        reverseLayout: Boolean
    ): Int {
        var currentPos = position
        var currentSpan = spanIndex
        val startSpanIndex = if (!reverseLayout) 0 else spanCount - 1

        // First step: move to edge of current span group
        while (!isPositionOutOfBounds(currentPos + posDir, edgePosition, forward)
            && fitsNextInCurrentSpanGroup(lookup, currentSpan, currentPos, spanDir, posDir)
        ) {
            currentPos += posDir
            currentSpan = getNextSpanEnd(lookup, currentSpan, currentPos, spanDir, posDir)
        }

        // Move to next span group
        currentPos += posDir

        if (isPositionOutOfBounds(currentPos, edgePosition, forward)) {
            return RecyclerView.NO_POSITION
        }

        // Second step: move to start of next span group
        currentSpan = lookup.getCachedSpanIndex(currentPos, spanCount)
        while (currentSpan != startSpanIndex
            && currentSpan > 0
            && currentSpan < spanCount
            && !isPositionOutOfBounds(currentPos + posDir, edgePosition, forward)
        ) {
            currentSpan += lookup.getSpanSize(currentPos) * spanDir
            currentPos += posDir
        }

        if (isPositionOutOfBounds(currentPos, edgePosition, forward)) {
            return RecyclerView.NO_POSITION
        }

        return currentPos
    }

    private fun fitsNextInCurrentSpanGroup(
        lookup: DpadSpanSizeLookup,
        spanIndex: Int,
        currentPos: Int,
        spanDir: Int,
        posDir: Int
    ): Boolean {
        val nextSpanEnd = getNextSpanEnd(lookup, spanIndex, currentPos, spanDir, posDir)
        return nextSpanEnd >= 0 && nextSpanEnd <= spanCount - 1
    }

    private fun getNextSpanEnd(
        spanSizeLookup: DpadSpanSizeLookup,
        spanIndex: Int,
        currentPos: Int,
        spanDir: Int,
        posDir: Int
    ): Int {
        val currentSpanEnd = spanIndex + (spanSizeLookup.getSpanSize(currentPos) - 1) * spanDir
        return currentSpanEnd + spanSizeLookup.getSpanSize(currentPos + posDir) * spanDir
    }

    private fun isPositionOutOfBounds(position: Int, edgePosition: Int, forward: Boolean): Boolean {
        return (position > edgePosition && forward) || (position < 0 && !forward)
    }

    private fun getSpanDirection(forward: Boolean, reverseLayout: Boolean): Int {
        val layoutDirection = if (forward != reverseLayout) 1 else -1
        return if (!reverseLayout) {
            if (layoutDirection > 0) 1 else -1
        } else {
            if (layoutDirection > 0) -1 else 1
        }
    }

}
