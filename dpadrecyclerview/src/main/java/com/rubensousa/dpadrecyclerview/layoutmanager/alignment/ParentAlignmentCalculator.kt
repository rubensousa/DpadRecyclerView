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

    fun updateStartLimit(
        edge: Int,
        viewAnchor: Int,
        alignment: ParentAlignment,
    ) {
        startEdge = edge
        if (isStartUnknown) {
            startScrollLimit = Int.MIN_VALUE
            return
        }
        val keyLine = calculateKeyline(alignment)
        startScrollLimit = if (shouldAlignViewToStart(viewAnchor, keyLine, alignment)) {
            calculateScrollOffsetToStartEdge(edge)
        } else if (isLayoutComplete()
            || alignment.preferKeylineOverEdge
             || alignment.edge == Edge.NONE
        ) {
            calculateScrollOffsetToKeyline(viewAnchor, keyLine)
        } else {
            0
        }
    }

    fun updateEndLimit(
        edge: Int,
        viewAnchor: Int,
        alignment: ParentAlignment,
    ) {
        endEdge = edge
        if (isEndUnknown) {
            endScrollLimit = Int.MAX_VALUE
            return
        }
        val keyline = calculateKeyline(alignment)
        endScrollLimit = if (shouldAlignViewToEnd(viewAnchor, keyline, alignment)) {
            calculateScrollOffsetToEndEdge(edge)
        } else if (isLayoutComplete()
            || alignment.preferKeylineOverEdge
            || alignment.edge == Edge.NONE
        ) {
            calculateScrollOffsetToKeyline(viewAnchor, keyline)
        } else {
            0
        }
    }

    private fun calculateScrollOffsetToEndEdge(edge: Int): Int {
        return edge - getLayoutEndEdge()
    }

    private fun calculateScrollOffsetToStartEdge(edge: Int): Int {
        return edge - getLayoutStartEdge()
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
                return min(startScrollLimit, calculateScrollOffsetToStartEdge(viewAnchor))
            }
            if (alignToEndEdge) {
                return max(endScrollLimit, calculateScrollOffsetToEndEdge(viewAnchor))
            }
        } else {
            if (alignToEndEdge) {
                return max(endScrollLimit, calculateScrollOffsetToEndEdge(viewAnchor))
            }
            if (alignToStartEdge) {
                return min(startScrollLimit, calculateScrollOffsetToStartEdge(viewAnchor))
            }
        }
        return calculateScrollOffsetToKeyline(viewAnchor, keyline)
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

    private fun shouldAlignViewToStart(
        viewAnchor: Int,
        keyline: Int,
        alignment: ParentAlignment,
    ): Boolean {
        if (isStartUnknown || !shouldAlignToStartEdge(alignment.edge)) {
            return false
        }
        if (!isLayoutIncomplete()) {
            return viewAnchor + getLayoutStartEdge() <= startEdge + keyline
        }
        return isLayoutIncomplete() && !alignment.preferKeylineOverEdge
    }

    private fun shouldAlignViewToEnd(
        viewAnchor: Int,
        keyline: Int,
        alignment: ParentAlignment,
    ): Boolean {
        if (isEndUnknown || !shouldAlignToEndEdge(alignment.edge)) {
            return false
        }
        if (!isLayoutIncomplete()) {
            return viewAnchor + getLayoutEndEdge() >= endEdge + keyline
        }
        return isLayoutIncomplete() && !alignment.preferKeylineOverEdge
    }

    private fun calculateScrollOffsetToKeyline(anchor: Int, keyline: Int): Int {
        return anchor - keyline
    }

    private fun getLayoutEndEdge(): Int {
        return size - paddingEnd
    }

    private fun getLayoutStartEdge(): Int {
        return paddingStart
    }

    private fun isLayoutComplete(): Boolean {
        if (isEndUnknown || isStartUnknown) {
            return false
        }
        return endEdge - startEdge >= size - paddingEnd - paddingStart
                && endEdge <= size - paddingEnd
                && startEdge >= paddingStart
    }

    private fun isLayoutIncomplete(): Boolean {
        if (isEndUnknown || isStartUnknown) {
            return false
        }
        return if (!reverseLayout) {
            endEdge < size - paddingEnd
        } else {
            startEdge > paddingStart
        }
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
