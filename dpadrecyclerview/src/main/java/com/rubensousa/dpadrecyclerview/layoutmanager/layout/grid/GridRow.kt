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
    private val numberOfSpans: Int,
    // This is the primary space, which would be height for horizontal grids
    var width: Int
) {

    var startIndex = RecyclerView.NO_POSITION
        private set

    var endIndex = RecyclerView.NO_POSITION
        private set

    // This is the secondary space, which would be width for horizontal grids
    var height = 0
        private set

    // For horizontal grids, this would be the start
    var top = 0
        private set

    private val heights = IntArray(numberOfSpans)

    constructor(row: GridRow) : this(row.numberOfSpans, row.width) {
        heights.fill(0)
        height = 0
        startIndex = row.startIndex
        endIndex = row.endIndex
        height = row.height
        top = row.top
        val spanSize = endIndex - startIndex + 1
        updateHeight(height, startIndex, spanSize)
    }

    fun init(newTop: Int, viewSize: Int, spanIndex: Int, spanSize: Int) {
        heights.fill(0)
        height = 0
        top = newTop
        startIndex = spanIndex
        endIndex = startIndex + spanSize - 1
        updateHeight(viewSize, startIndex, spanSize)
    }

    fun offsetBy(offset: Int) {
        top += offset
    }

    fun fitsEnd(spanSize: Int): Boolean {
        return endIndex + spanSize < numberOfSpans
    }

    fun isEndComplete(): Boolean {
        return endIndex == numberOfSpans - 1
    }

    fun isStartComplete(): Boolean {
        return startIndex == 0
    }

    fun fitsStart(spanSize: Int): Boolean {
        return startIndex - spanSize >= 0
    }

    fun getSpanSpace(): Int {
        return width / numberOfSpans
    }

    fun getHeightAt(spanIndex: Int): Int {
        return heights[spanIndex]
    }

    fun getStartOffset(): Int {
        return startIndex * getSpanSpace()
    }

    fun getEndOffset(): Int {
        return (endIndex + 1) * getSpanSpace()
    }

    fun append(viewSize: Int, spanSize: Int): Int {
        if (!fitsEnd(spanSize)) {
            return -1
        }
        val viewSpanIndex = endIndex + 1
        val viewStart = getSpanSpace() * viewSpanIndex
        updateHeight(viewSize, viewSpanIndex, spanSize)
        endIndex += spanSize
        return viewStart
    }

    fun prepend(viewSize: Int, spanSize: Int): Int {
        if (!fitsStart(spanSize)) {
            return -1
        }
        startIndex -= spanSize
        updateHeight(viewSize, startIndex, spanSize)
        return getSpanSpace() * startIndex
    }

    fun next(viewSize: Int, spanSize: Int, newTop: Int) {
        heights.fill(0)
        height = 0
        startIndex = 0
        endIndex = spanSize - 1
        top = newTop
        updateHeight(viewSize, startIndex, spanSize)
    }

    fun previous(viewSize: Int, spanSize: Int, newTop: Int) {
        heights.fill(0)
        height = 0
        endIndex = numberOfSpans - 1
        startIndex = endIndex + 1 - spanSize
        top = newTop
        updateHeight(viewSize, startIndex, spanSize)
    }

    private fun updateHeight(viewSize: Int, spanIndex: Int, spanSize: Int) {
        height = max(viewSize, height)
        for (i in spanIndex until spanIndex + spanSize) {
            heights[i] = viewSize
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
            if (getHeightAt(i) != other.getHeightAt(i)) {
                return false
            }
        }
        return startIndex == other.startIndex
                && endIndex == other.endIndex
                && top == other.top
                && height == other.height
    }

    override fun hashCode(): Int {
        var result = numberOfSpans
        result = 31 * result + width
        result = 31 * result + startIndex
        result = 31 * result + endIndex
        result = 31 * result + height
        result = 31 * result + top
        result = 31 * result + heights.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "GridRow(startIndex=$startIndex, " +
                "endIndex=$endIndex, " +
                "height=$height, " +
                "top=$top, " +
                "heights=${heights.contentToString()})"
    }


}
