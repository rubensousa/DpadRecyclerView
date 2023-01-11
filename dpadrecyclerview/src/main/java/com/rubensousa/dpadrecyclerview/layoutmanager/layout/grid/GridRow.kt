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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

class GridRow(
    val numberOfSpans: Int,
    // This is the primary space, which would be height for horizontal grids
    private var width: Int
) {

    var startIndex = RecyclerView.NO_POSITION
        private set

    var endIndex = RecyclerView.NO_POSITION
        private set

    // This is the secondary space, which would be width for horizontal grids
    var height = 0
        private set

    var startOffset = 0
        private set

    var endOffset = 0
        private set

    private val positions = IntArray(numberOfSpans) { RecyclerView.NO_POSITION }
    private val spanBorders = IntArray(numberOfSpans + 1) { 0 }

    constructor(row: GridRow) : this(row.numberOfSpans, row.width) {
        startIndex = row.startIndex
        endIndex = row.endIndex
        height = row.height
        startOffset = row.startOffset
        endOffset = row.endOffset
        for (i in 0 until numberOfSpans) {
            positions[i] = row.getPositionAt(i)
            spanBorders[i] = row.getSpanBorder(i)
        }
        spanBorders[numberOfSpans] = row.getSpanBorder(numberOfSpans)
    }

    init {
        setWidth(width)
    }

    fun setWidth(newWidth: Int) {
        width = newWidth
        spanBorders[0] = 0
        val sizePerSpan: Int = newWidth / numberOfSpans
        val sizePerSpanRemainder: Int = newWidth % numberOfSpans
        var consumedPixels = 0
        var additionalSize = 0
        for (i in 1..numberOfSpans) {
            var itemSize = sizePerSpan
            additionalSize += sizePerSpanRemainder
            if (additionalSize > 0 && numberOfSpans - additionalSize < sizePerSpanRemainder) {
                itemSize += 1
                additionalSize -= numberOfSpans
            }
            consumedPixels += itemSize
            spanBorders[i] = consumedPixels
        }
    }

    fun getWidth(): Int = width

    fun isEmpty(): Boolean {
        return startIndex == RecyclerView.NO_POSITION && endIndex == RecyclerView.NO_POSITION
    }

    fun getAvailableAppendSpans(): Int {
        if (endIndex == RecyclerView.NO_POSITION) {
            if (startIndex == RecyclerView.NO_POSITION) {
                return numberOfSpans
            }
            return numberOfSpans - startIndex
        }
        // Spans: 3, filled: [X, -, -],
        return numberOfSpans - endIndex - 1
    }

    fun getAvailablePrependSpans(): Int {
        if (startIndex == RecyclerView.NO_POSITION) {
            if (endIndex == RecyclerView.NO_POSITION) {
                return numberOfSpans
            }
            return endIndex
        }
        // Spans: 3, filled: [X, -, -],
        return startIndex
    }

    fun getSpanBorder(index: Int): Int {
        return spanBorders[index]
    }

    fun getSpaceForSpanRange(
        startSpan: Int,
        spanSize: Int,
        isVerticalRTL: Boolean
    ): Int {
        return if (isVerticalRTL) {
            (spanBorders[numberOfSpans - startSpan]
                    - spanBorders[numberOfSpans - startSpan - spanSize])
        } else {
            spanBorders[startSpan + spanSize] - spanBorders[startSpan]
        }
    }

    fun offsetBy(dy: Int) {
        startOffset += dy
        endOffset += dy
    }

    fun fitsEnd(spanSize: Int): Boolean {
        if (endIndex == RecyclerView.NO_POSITION) {
            return spanSize <= numberOfSpans
        }
        return endIndex + spanSize < numberOfSpans
    }

    fun fitsStart(spanSize: Int): Boolean {
        if (startIndex == RecyclerView.NO_POSITION) {
            return spanSize <= numberOfSpans
        }
        return startIndex - spanSize >= 0
    }

    fun isEndComplete(): Boolean {
        return endIndex == numberOfSpans - 1
    }

    fun isStartComplete(): Boolean {
        return startIndex == 0
    }

    fun getSpanSpace(): Int {
        return width / numberOfSpans
    }

    fun getPositionAt(spanIndex: Int): Int {
        return positions[spanIndex]
    }

    fun getFirstPosition(): Int {
        if (startIndex == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        return positions[startIndex]
    }

    fun getLastPosition(): Int {
        if (endIndex == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        return positions[endIndex]
    }

    fun getSpanStartOffset(): Int {
        if (startIndex == RecyclerView.NO_POSITION) {
            return 0
        }
        return getSpanBorder(startIndex)
    }

    fun getSpanEndOffset(): Int {
        if (endIndex == RecyclerView.NO_POSITION) {
            return 0
        }
        return getSpanBorder(endIndex + 1)
    }

    fun append(viewSize: Int, viewPosition: Int, spanSize: Int): Int {
        val viewSpanIndex = endIndex + 1
        val viewStart = getSpanSpace() * viewSpanIndex
        updateSpans(viewSize, viewPosition, viewSpanIndex, spanSize)
        endOffset = startOffset + height
        endIndex += spanSize
        if (startIndex == RecyclerView.NO_POSITION) {
            startIndex = 0
        }
        return viewStart
    }

    fun prepend(viewSize: Int, viewPosition: Int, spanSize: Int): Int {
        if (startIndex == RecyclerView.NO_POSITION) {
            startIndex = numberOfSpans - spanSize
        } else {
            startIndex -= spanSize
        }
        updateSpans(viewSize, viewPosition, startIndex, spanSize)
        startOffset = endOffset - height
        if (endIndex == RecyclerView.NO_POSITION) {
            endIndex = numberOfSpans - 1
        }
        return getSpanSpace() * startIndex
    }

    /**
     * @return next item position
     */
    fun moveToNext(): Int {
        val nextPosition = getPositionAt(numberOfSpans - 1) + 1
        startOffset += height
        endOffset = startOffset
        resetSpans()
        return nextPosition
    }

    /**
     * @return previous item position
     */
    fun moveToPrevious(): Int {
        val previousPosition = getPositionAt(0) - 1
        endOffset = startOffset
        resetSpans()
        return previousPosition
    }

    fun reset(keyline: Int) {
        startOffset = keyline
        endOffset = keyline
        resetSpans()
    }

    private fun resetSpans() {
        for (i in 0 until numberOfSpans) {
            positions[i] = RecyclerView.NO_POSITION
        }
        height = 0
        startIndex = RecyclerView.NO_POSITION
        endIndex = RecyclerView.NO_POSITION
    }

    private fun updateSpans(viewSize: Int, position: Int, spanIndex: Int, spanSize: Int) {
        height = max(viewSize, height)
        for (i in spanIndex until spanIndex + spanSize) {
            positions[i] = position
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is GridRow) {
            return false
        }
        if (numberOfSpans != other.numberOfSpans || width != other.width) {
            return false
        }
        for (i in 0 until numberOfSpans) {
            if (getPositionAt(i) != other.getPositionAt(i)) {
                return false
            }
        }
        return startIndex == other.startIndex
                && endIndex == other.endIndex
                && startOffset == other.startOffset
                && height == other.height
    }

    override fun hashCode(): Int {
        var result = numberOfSpans
        result = 31 * result + width
        result = 31 * result + startIndex
        result = 31 * result + endIndex
        result = 31 * result + height
        result = 31 * result + startOffset
        result = 31 * result + positions.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "GridRow(startIndex=$startIndex, " +
                "endIndex=$endIndex, " +
                "height=$height, " +
                "startOffset=$startOffset, " +
                "endOffset=$endOffset, " +
                "positions=${positions.contentToString()})"
    }


}
