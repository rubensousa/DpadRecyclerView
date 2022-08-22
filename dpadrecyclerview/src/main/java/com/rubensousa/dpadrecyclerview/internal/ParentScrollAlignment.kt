package com.rubensousa.dpadrecyclerview.internal

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import kotlin.math.max
import kotlin.math.min

// TODO: Add unit tests
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

    var reversedFlow = false

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

    fun setSize(width: Int, height: Int) {
        if (orientation == RecyclerView.HORIZONTAL) {
            size = width
        } else {
            size = height
        }
    }

    fun setPadding(
        paddingLeft: Int, paddingRight: Int,
        paddingTop: Int, paddingBottom: Int
    ) {
        if (orientation == RecyclerView.HORIZONTAL) {
            paddingMin = paddingLeft
            paddingMax = paddingRight
        } else {
            paddingMin = paddingTop
            paddingMax = paddingBottom
        }
    }

    /**
     * Update [.getMinScroll] and [.getMaxScroll]
     */
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
            val alignToMinEdge = if (!reversedFlow) {
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
            val alignToMaxEdge = if (!reversedFlow) {
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
            if (!reversedFlow) {
                if (shouldAlignToMinEdge(defaultAlignment)) {
                    if (defaultAlignment.preferKeylineOverMinEdge) {
                        // if we prefer key line, might align max child to key line for minScroll
                        minScroll = min(
                            minScroll, calculateScrollToKeyLine(maxChildViewCenter, keyLine)
                        )
                    }
                    // don't over scroll max
                    maxScroll = max(minScroll, maxScroll)
                } else if (shouldAlignToMaxEdge(defaultAlignment)) {
                    if (defaultAlignment.preferKeylineOverMaxEdge) {
                        // if we prefer key line, might align min child to key line for maxScroll
                        maxScroll = max(
                            maxScroll, calculateScrollToKeyLine(minChildViewCenter, keyLine)
                        )
                    }
                    // don't over scroll min
                    minScroll = min(minScroll, maxScroll)
                }
            } else {
                if (shouldAlignToMinEdge(defaultAlignment)) {
                    if (defaultAlignment.preferKeylineOverMinEdge) {
                        // if we prefer key line, might align min child to key line for maxScroll
                        maxScroll = max(
                            maxScroll, calculateScrollToKeyLine(minChildViewCenter, keyLine)
                        )
                    }
                    // don't over scroll min
                    minScroll = min(minScroll, maxScroll)
                } else if (shouldAlignToMaxEdge(defaultAlignment)) {
                    if (defaultAlignment.preferKeylineOverMaxEdge) {
                        // if we prefer key line, might align max child to key line for
                        // minScroll
                        minScroll = min(
                            minScroll, calculateScrollToKeyLine(maxChildViewCenter, keyLine)
                        )
                    }
                    // don't over scroll max
                    maxScroll = max(minScroll, maxScroll)
                }
            }
        }
    }

    /**
     * Get scroll distance to align an item centered around [viewCenter].
     * Item will either be aligned to the keyline position or to either min or max edges
     * according to the current [alignment].
     * The scroll distance will be capped by [minScroll] and [maxScroll]
     */
    fun calculateScrollDistance(viewCenter: Int, subPositionAlignment: ParentAlignment?): Int {
        val alignment = subPositionAlignment ?: defaultAlignment
        val keyLine = calculateKeyline(alignment)
        if (!isMinUnknown) {
            val keyLineToMinEdge = keyLine - paddingMin
            val alignToMinEdge = if (!reversedFlow) {
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
            val alignToMaxEdge = if (!reversedFlow) {
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

    private fun calculateKeyline(alignment: ParentAlignment): Int {
        var keyLine = 0
        if (!reversedFlow) {
            if (alignment.isOffsetRatioEnabled) {
                keyLine += (size * alignment.offsetStartRatio).toInt()
            }
            keyLine += alignment.offset
        } else {
            if (alignment.isOffsetRatioEnabled) {
                keyLine -= (size * (1f - alignment.offsetStartRatio)).toInt()
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
