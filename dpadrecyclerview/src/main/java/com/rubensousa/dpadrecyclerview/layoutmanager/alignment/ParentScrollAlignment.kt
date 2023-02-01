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

import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import kotlin.math.max
import kotlin.math.min

internal class ParentScrollAlignment {

    val isStartUnknown: Boolean
        get() = isScrollLimitInvalid(startEdge)

    val isEndUnknown: Boolean
        get() = isScrollLimitInvalid(endEdge)

    var defaultAlignment = ParentAlignment(edge = Edge.MIN_MAX)

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

    fun updateLayoutInfo(layoutManager: LayoutManager, orientation: Int, reverseLayout: Boolean) {
        size = if (orientation == RecyclerView.HORIZONTAL) {
            layoutManager.width
        } else {
            layoutManager.height
        }
        this.reverseLayout = reverseLayout
        if (orientation == RecyclerView.HORIZONTAL) {
            paddingStart = layoutManager.paddingStart
            paddingEnd = layoutManager.paddingEnd
        } else {
            paddingStart = layoutManager.paddingTop
            paddingEnd = layoutManager.paddingBottom
        }
    }

    fun isScrollLimitInvalid(scroll: Int): Boolean {
        return scroll == Int.MIN_VALUE || scroll == Int.MAX_VALUE
    }

    fun invalidateScrollLimits() {
        startEdge = Int.MIN_VALUE
        startScrollLimit = Int.MIN_VALUE

        endEdge = Int.MAX_VALUE
        endScrollLimit = Int.MAX_VALUE
    }

    fun updateStartLimit(edge: Int, viewAnchor: Int) {
        startEdge = edge
        if (isStartUnknown) {
            startScrollLimit = Int.MIN_VALUE
            return
        }
        val keyLine = calculateKeyline(defaultAlignment)
        startScrollLimit = if (shouldAlignViewToStart(viewAnchor, keyLine, defaultAlignment.edge)) {
            calculateScrollOffsetToStartEdge(edge)
        } else {
            calculateScrollOffsetToKeyline(viewAnchor, keyLine)
        }
    }

    fun updateEndLimit(edge: Int, viewAnchor: Int) {
        this.endEdge = edge
        if (isEndUnknown) {
            endScrollLimit = Int.MAX_VALUE
            return
        }
        val keyLine = calculateKeyline(defaultAlignment)
        endScrollLimit = if (shouldAlignViewToEnd(viewAnchor, keyLine, defaultAlignment.edge)) {
            calculateScrollOffsetToEndEdge(edge)
        } else {
            calculateScrollOffsetToKeyline(viewAnchor, keyLine)
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
     * according to the current [defaultAlignment].
     */
    fun calculateScrollOffset(
        viewAnchor: Int, subPositionAlignment: ParentAlignment? = null
    ): Int {
        val alignment = subPositionAlignment ?: defaultAlignment
        val keyline = calculateKeyline(alignment)
        val alignToStartEdge = shouldAlignViewToStart(viewAnchor, keyline, alignment.edge)
        val alignToEndEdge = shouldAlignViewToEnd(viewAnchor, keyline, alignment.edge)
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

    fun calculateKeyline(alignment: ParentAlignment = defaultAlignment): Int {
        var keyLine = 0
        if (!reverseLayout) {
            if (alignment.isOffsetRatioEnabled) {
                keyLine = (size * alignment.offsetRatio).toInt()
            }
            keyLine += alignment.offset
        } else {
            if (alignment.isOffsetRatioEnabled) {
                keyLine = (size * (1.0f - alignment.offsetRatio)).toInt()
                keyLine -= alignment.offset
            } else {
                keyLine = size - alignment.offset
            }
        }
        return keyLine
    }

    private fun shouldAlignViewToStart(viewCenter: Int, keyline: Int, edge: Edge): Boolean {
        if (isStartUnknown || !shouldAlignToStartEdge(edge)) {
            return false
        }
        return viewCenter - startEdge <= keyline - paddingStart
    }

    private fun shouldAlignViewToEnd(viewCenter: Int, keyline: Int, edge: Edge): Boolean {
        if (isEndUnknown || !shouldAlignToEndEdge(edge)) {
            return false
        }
        if (endEdge < size - paddingEnd) {
            return true
        }
        return endEdge - viewCenter <= size - keyline - paddingEnd
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

    private fun shouldAlignToStartEdge(edge: Edge): Boolean {
        if (edge == Edge.MIN_MAX) {
            return true
        }
        return (!reverseLayout && edge == Edge.MIN) || (reverseLayout && edge == Edge.MAX)
    }

    private fun shouldAlignToEndEdge(edge: Edge): Boolean {
        if (edge == Edge.MIN_MAX) {
            return true
        }
        return (!reverseLayout && edge == Edge.MAX) || (reverseLayout && edge == Edge.MIN)
    }

}
