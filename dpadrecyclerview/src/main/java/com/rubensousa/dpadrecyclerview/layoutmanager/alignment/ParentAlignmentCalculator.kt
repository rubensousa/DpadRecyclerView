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

package com.rubensousa.dpadrecyclerview.layoutmanager.alignment

import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import kotlin.math.max
import kotlin.math.min

internal class ParentAlignmentCalculator {

    val isStartUnknown: Boolean
        get() = isScrollLimitInvalid(startEdge)

    val isEndUnknown: Boolean
        get() = isScrollLimitInvalid(endEdge)

    var startScrollLimit = Int.MIN_VALUE
        private set

    var endScrollLimit = Int.MAX_VALUE
        private set

    private var reverseLayout = false
    private var size = 0
    private var paddingStart = 0
    private var paddingEnd = 0
    private var endEdge = Int.MAX_VALUE
    private var startEdge = Int.MIN_VALUE

    fun setScrollStartLimit(limit: Int) {
        startScrollLimit = limit
    }

    fun setScrollEndLimit(limit: Int) {
        endScrollLimit = limit
    }

    fun updateLayoutInfo(
        layoutManager: LayoutManager,
        isVertical: Boolean,
        reverseLayout: Boolean,
    ) {
        size = if (isVertical) {
            layoutManager.height
        } else {
            layoutManager.width
        }
        this.reverseLayout = reverseLayout
        if (isVertical) {
            paddingStart = layoutManager.paddingTop
            paddingEnd = layoutManager.paddingBottom
        } else {
            paddingStart = layoutManager.paddingStart
            paddingEnd = layoutManager.paddingEnd
        }
    }

    fun isScrollLimitInvalid(scroll: Int): Boolean {
        return scroll == Int.MIN_VALUE || scroll == Int.MAX_VALUE
    }

    fun invalidateScrollLimits() {
        invalidateStartLimit()
        invalidateEndLimit()
    }

    fun invalidateStartLimit() {
        startEdge = Int.MIN_VALUE
        startScrollLimit = Int.MIN_VALUE
    }

    fun invalidateEndLimit() {
        endEdge = Int.MAX_VALUE
        endScrollLimit = Int.MAX_VALUE
    }

    fun updateScrollLimits(
        startEdge: Int,
        endEdge: Int,
        startViewAnchor: Int,
        endViewAnchor: Int,
        startAlignment: ParentAlignment,
        endAlignment: ParentAlignment,
    ) {
        this.startEdge = startEdge
        this.endEdge = endEdge
        val startKeyline = calculateKeyline(startAlignment)
        val endKeyline = calculateKeyline(endAlignment)
        startScrollLimit = when {
            isStartUnknown -> Int.MIN_VALUE
            shouldAlignViewToStart(startViewAnchor, startKeyline, startAlignment) -> {
                calculateScrollOffsetToStartEdge(startEdge)
            }

            shouldAlignStartToKeyline(startAlignment) -> {
                calculateScrollOffsetToKeyline(
                    anchor = startViewAnchor,
                    keyline = startKeyline
                )
            }

            else -> 0
        }
        endScrollLimit = when {
            isEndUnknown -> Int.MAX_VALUE
            shouldAlignViewToEnd(endViewAnchor, endKeyline, endAlignment) -> {
                calculateScrollOffsetToEndEdge(endEdge)
            }

            shouldAlignEndToKeyline(endAlignment) -> {
                calculateScrollOffsetToKeyline(
                    anchor = endViewAnchor,
                    keyline = endKeyline
                )
            }

            else -> 0
        }
    }

    private fun shouldAlignStartToKeyline(alignment: ParentAlignment): Boolean {
        return !shouldAlignToStartEdge(alignment.edge) || preferKeylineOverEdge(alignment)
    }

    private fun shouldAlignEndToKeyline(alignment: ParentAlignment): Boolean {
        return !shouldAlignToEndEdge(alignment.edge) || preferKeylineOverEdge(alignment)
    }

    private fun calculateScrollOffsetToEndEdge(anchor: Int): Int {
        return anchor - getLayoutAbsoluteEnd()
    }

    private fun calculateScrollOffsetToStartEdge(anchor: Int): Int {
        return anchor - getLayoutAbsoluteStart()
    }

    /**
     * Returns the scroll target position to align an item centered around [viewAnchor].
     * Item will either be aligned to the keyline position or to either min or max edges
     * according to the current [alignment].
     */
    fun calculateScrollOffset(
        viewAnchor: Int,
        alignment: ParentAlignment,
    ): Int {
        val keyline = calculateKeyline(alignment)
        val alignToStartEdge = shouldAlignViewToStart(viewAnchor, keyline, alignment)
        val alignToEndEdge = shouldAlignViewToEnd(viewAnchor, keyline, alignment)
        if (!reverseLayout) {
            if (alignToStartEdge) {
                return calculateScrollToStartEdge(viewAnchor)
            }
            if (alignToEndEdge) {
                return calculateScrollToEndEdge(viewAnchor)
            }
        } else {
            if (alignToEndEdge) {
                return calculateScrollToEndEdge(viewAnchor)
            }
            if (alignToStartEdge) {
                return calculateScrollToStartEdge(viewAnchor)
            }
        }
        return calculateScrollOffsetToKeyline(viewAnchor, keyline)
    }

    fun calculateKeylineScrollOffset(
        viewAnchor: Int,
        alignment: ParentAlignment,
    ): Int {
        val keyline = calculateKeyline(alignment)
        return calculateScrollOffsetToKeyline(viewAnchor, keyline)
    }

    private fun calculateScrollToStartEdge(anchor: Int): Int {
        return min(startScrollLimit, calculateScrollOffsetToStartEdge(anchor))
    }

    private fun calculateScrollToEndEdge(anchor: Int): Int {
        return max(endScrollLimit, calculateScrollOffsetToEndEdge(anchor))
    }

    fun calculateKeyline(alignment: ParentAlignment): Int {
        var keyLine = 0
        if (!reverseLayout) {
            if (alignment.isFractionEnabled) {
                keyLine = (size * alignment.fraction).toInt()
            }
            keyLine += alignment.offset
        } else {
            if (alignment.isFractionEnabled) {
                keyLine = (size * (1.0f - alignment.fraction)).toInt()
                keyLine -= alignment.offset
            } else {
                keyLine = size - alignment.offset
            }
        }
        return keyLine
    }

    internal fun shouldAlignViewToStart(
        viewAnchor: Int,
        keyline: Int,
        alignment: ParentAlignment,
    ): Boolean {
        if (isStartUnknown || !shouldAlignToStartEdge(alignment.edge)) {
            return false
        }
        if (alignment.edge == Edge.NONE) {
            return false
        }
        if (isLayoutComplete()) {
            return if (alignment.preferKeylineOverEdge) {
                false
            } else {
                viewAnchor + getLayoutAbsoluteStart() <= startEdge + keyline
            }
        }
        return viewAnchor < keyline
    }

    internal fun shouldAlignViewToEnd(
        viewAnchor: Int,
        keyline: Int,
        alignment: ParentAlignment,
    ): Boolean {
        if (isEndUnknown || !shouldAlignToEndEdge(alignment.edge)) {
            return false
        }
        if (alignment.edge == Edge.NONE) {
            return false
        }
        if (isLayoutComplete()) {
            return if (alignment.preferKeylineOverEdge) {
                false
            } else {
                viewAnchor + getLayoutAbsoluteEnd() >= endEdge + keyline
            }
        }
        return viewAnchor > keyline
    }

    private fun calculateScrollOffsetToKeyline(anchor: Int, keyline: Int): Int {
        return anchor - keyline
    }

    private fun getLayoutAbsoluteEnd(): Int {
        return size - paddingEnd
    }

    private fun getLayoutAbsoluteStart(): Int {
        return paddingStart
    }

    internal fun isLayoutComplete(): Boolean {
        return isStartLayoutComplete() && isEndLayoutComplete()
    }

    private fun isStartLayoutComplete(): Boolean {
        return !isStartUnknown
    }

    private fun isEndLayoutComplete(): Boolean {
        return !isEndUnknown
    }

    private fun isKeylinePreferred(alignment: ParentAlignment): Boolean {
        return alignment.preferKeylineOverEdge && isLayoutComplete()
    }

    private fun preferKeylineOverEdge(alignment: ParentAlignment): Boolean {
        return isKeylinePreferred(alignment) || alignment.edge == Edge.NONE
    }

    private fun isStartEdge(edge: Edge): Boolean {
        return (!reverseLayout && edge == Edge.MIN) || (reverseLayout && edge == Edge.MAX)
    }

    private fun isEndEdge(edge: Edge): Boolean {
        return (!reverseLayout && edge == Edge.MAX) || (reverseLayout && edge == Edge.MIN)
    }

    private fun shouldAlignToStartEdge(edge: Edge): Boolean {
        return edge == Edge.MIN_MAX || isStartEdge(edge)
    }

    private fun shouldAlignToEndEdge(edge: Edge): Boolean {
        return edge == Edge.MIN_MAX || isEndEdge(edge)
    }

}
