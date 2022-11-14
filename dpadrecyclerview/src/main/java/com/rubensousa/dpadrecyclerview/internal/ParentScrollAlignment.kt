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

package com.rubensousa.dpadrecyclerview.internal

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import kotlin.math.max
import kotlin.math.min

internal class ParentScrollAlignment {

    val isMinUnknown: Boolean
        get() = minEdge == Int.MIN_VALUE

    val isMaxUnknown: Boolean
        get() = maxEdge == Int.MAX_VALUE

    var defaultAlignment = ParentAlignment(edge = Edge.MIN_MAX)
    var orientation = RecyclerView.VERTICAL

    /**
     * Scroll distance to align first child, it defines limit of scroll.
     */
    var minScroll = 0
        private set

    /**
     * Scroll distance to align last child, it defines limit of scroll.
     */
    var maxScroll = 0
        private set

    var reverseLayout = false

    var size = 0
        private set

    private val sizeWithoutPaddings: Int
        get() = size - paddingMin - paddingMax

    /**
     * Padding at the min edge, it is the left or top padding.
     */
    private var paddingMin = 0

    /**
     * Padding at the max edge, it is the right or bottom padding.
     */
    private var paddingMax = 0

    /**
     * Right or bottom edge of last child.
     */
    private var maxEdge = 0

    /**
     * Left or top edge of first child
     */
    private var minEdge = 0

    fun invalidateScrollMin() {
        minEdge = Int.MIN_VALUE
        minScroll = Int.MIN_VALUE
    }

    fun invalidateScrollMax() {
        maxEdge = Int.MAX_VALUE
        maxScroll = Int.MAX_VALUE
    }

    fun setSize(width: Int, height: Int, orientation: Int) {
        size = if (orientation == RecyclerView.HORIZONTAL) {
            width
        } else {
            height
        }
    }

    fun setPadding(left: Int, right: Int, top: Int, bottom: Int, orientation: Int) {
        if (orientation == RecyclerView.HORIZONTAL) {
            paddingMin = left
            paddingMax = right
        } else {
            paddingMin = top
            paddingMax = bottom
        }
    }

    fun updateMinMax(
        minEdge: Int, maxEdge: Int,
        minChildViewCenter: Int, maxChildViewCenter: Int
    ) {
        this.minEdge = minEdge
        this.maxEdge = maxEdge
        val keyLine = calculateKeyline(defaultAlignment)
        val isMinUnknown = isMinUnknown
        val isMaxUnknown = isMaxUnknown
        if (!isMinUnknown) {
            val alignToMinEdge = if (!reverseLayout) {
                shouldAlignToMinEdge(defaultAlignment)
            } else {
                shouldAlignToMaxEdge(defaultAlignment)
            }
            if (alignToMinEdge) {
                // calculate scroll distance to move current minEdge to padding at min edge
                minScroll = minEdge - paddingMin
            } else {
                // calculate scroll distance to move min child center to key line
                minScroll = calculateScrollToKeyLine(minChildViewCenter, keyLine)
            }
        }
        if (!isMaxUnknown) {
            val alignToMaxEdge = if (!reverseLayout) {
                shouldAlignToMaxEdge(defaultAlignment)
            } else {
                shouldAlignToMinEdge(defaultAlignment)
            }
            if (alignToMaxEdge) {
                // calculate scroll distance to move current maxEdge to padding at max edge
                maxScroll = maxEdge - paddingMin - sizeWithoutPaddings
            } else {
                // calculate scroll distance to move max child center to key line
                maxScroll = calculateScrollToKeyLine(maxChildViewCenter, keyLine)
            }
        }
        if (!isMaxUnknown && !isMinUnknown) {
            if (!reverseLayout) {
                if (shouldAlignToMinEdge(defaultAlignment)) {
                    // don't over scroll max
                    maxScroll = max(minScroll, maxScroll)
                } else if (shouldAlignToMaxEdge(defaultAlignment)) {
                    // don't over scroll min
                    minScroll = min(minScroll, maxScroll)
                }
            } else {
                if (shouldAlignToMinEdge(defaultAlignment)) {
                    // don't over scroll min
                    minScroll = min(minScroll, maxScroll)
                } else if (shouldAlignToMaxEdge(defaultAlignment)) {
                    // don't over scroll max
                    maxScroll = max(minScroll, maxScroll)
                }
            }
        }
    }

    /**
     * Get scroll distance to align an item centered around [viewCenter].
     * Item will either be aligned to the keyline position or to either min or max edges
     * according to the current [defaultAlignment].
     * The scroll distance will be capped by [minScroll] and [maxScroll]
     */
    fun calculateScrollDistance(
        viewCenter: Int, subPositionAlignment: ParentAlignment? = null
    ): Int {
        val alignment = subPositionAlignment ?: defaultAlignment
        val keyLine = calculateKeyline(alignment)
        if (!isMinUnknown) {
            val keyLineToMinEdge = keyLine - paddingMin
            val alignToMinEdge = if (!reverseLayout) {
                shouldAlignToMinEdge(alignment)
            } else {
                shouldAlignToMaxEdge(alignment)
            }
            if (alignToMinEdge && viewCenter - minEdge <= keyLineToMinEdge) {
                // view center is before key line: align the min edge (first child) to padding.
                var alignToMin = minEdge - paddingMin
                // Also we need make sure don't over scroll
                if (!isMaxUnknown && alignToMin > maxScroll) {
                    alignToMin = maxScroll
                }
                return alignToMin
            }
        }
        if (!isMaxUnknown) {
            val keyLineToMaxEdge = size - keyLine - paddingMax
            val alignToMaxEdge = if (!reverseLayout) {
                shouldAlignToMaxEdge(alignment)
            } else {
                shouldAlignToMinEdge(alignment)
            }
            if (alignToMaxEdge && maxEdge - viewCenter <= keyLineToMaxEdge) {
                // view center is after key line: align the max edge (last child) to padding.
                var alignToMax = maxEdge - (size - paddingMax)
                // Also we need make sure don't over scroll
                if (!isMinUnknown && alignToMax < minScroll) {
                    alignToMax = minScroll
                }
                return alignToMax
            }
        }
        // else put view center at key line.
        return calculateScrollToKeyLine(viewCenter, keyLine)
    }

    fun reset() {
        minEdge = Int.MIN_VALUE
        maxEdge = Int.MAX_VALUE
    }

    fun calculateKeyline(alignment: ParentAlignment = defaultAlignment): Int {
        var keyLine = 0
        if (!reverseLayout) {
            if (alignment.isOffsetRatioEnabled) {
                keyLine += (size * alignment.offsetRatio).toInt()
            }
            keyLine += alignment.offset
        } else {
            if (alignment.isOffsetRatioEnabled) {
                keyLine -= (size * (1f - alignment.offsetRatio)).toInt()
            }
            keyLine -= alignment.offset
        }
        return keyLine
    }

    /**
     * Returns scroll distance to move viewCenterPosition to keyLine.
     */
    private fun calculateScrollToKeyLine(viewCenterPosition: Int, keyLine: Int): Int {
        return viewCenterPosition - keyLine
    }

    private fun shouldAlignToMinEdge(alignment: ParentAlignment): Boolean {
        return alignment.edge == Edge.MIN || alignment.edge == Edge.MIN_MAX
    }

    private fun shouldAlignToMaxEdge(alignment: ParentAlignment): Boolean {
        return alignment.edge == Edge.MAX || alignment.edge == Edge.MIN_MAX
    }

}
