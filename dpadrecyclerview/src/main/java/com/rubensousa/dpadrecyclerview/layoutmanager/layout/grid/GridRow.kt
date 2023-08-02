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

internal class GridRow(
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
        copy(row)
    }

    init {
        setWidth(width)
    }

    fun copy(row: GridRow) {
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

    fun getSpanBorder(spanIndex: Int): Int {
        return spanBorders[spanIndex]
    }

    fun getSpaceForSpanRange(
        startSpan: Int,
        spanSize: Int,
        reverseLayout: Boolean
    ): Int {
        return if (!reverseLayout) {
            spanBorders[startSpan + spanSize] - spanBorders[startSpan]
        } else {
            (spanBorders[numberOfSpans - startSpan]
                    - spanBorders[numberOfSpans - startSpan - spanSize])
        }
    }

    fun offsetBy(dy: Int) {
        startOffset += dy
        endOffset += dy
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

    fun append(viewSize: Int, viewPosition: Int, spanIndex: Int, spanSize: Int) {
        endIndex = spanIndex + spanSize - 1
        updateSpans(viewSize, viewPosition, spanIndex, spanSize)
        endOffset = startOffset + height
        if (startIndex == RecyclerView.NO_POSITION) {
            startIndex = spanIndex
        }
    }

    fun prepend(viewSize: Int, viewPosition: Int, spanIndex: Int, spanSize: Int) {
        startIndex = spanIndex
        updateSpans(viewSize, viewPosition, spanIndex, spanSize)
        startOffset = endOffset - height
        if (endIndex == RecyclerView.NO_POSITION) {
            endIndex = spanIndex + spanSize - 1
        }
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
