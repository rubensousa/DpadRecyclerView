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

import android.graphics.Rect
import kotlin.math.max

// TODO check if we need to include top and bottom paddings
internal class Grid(
    numberOfSpans: Int,
    private var width: Int,
    private var rowStart: Int,
    private var rowEnd: Int
) {

    private var topRow = GridRow(numberOfSpans, width)
    private var bottomRow = topRow

    fun init(newTop: Int, viewSize: Int, spanIndex: Int, spanSize: Int) {
        topRow.init(newTop, viewSize, spanIndex, spanSize)
        // At this stage, both top and bottom rows are the same
        bottomRow = topRow
    }

    fun updateWidth(newWidth: Int) {
        width = newWidth
        topRow.width = newWidth
        bottomRow.width = newWidth
    }

    fun offsetBy(offset: Int) {
        topRow.offsetBy(offset)
        if (topRow !== bottomRow) {
            bottomRow.offsetBy(offset)
        }
    }

    /**
     * This will insert the view in its correct span index
     * and return the new space added to the layout which can happen when:
     * 1. Row is inserted: the height of the current row will be added for the next checkpoint
     * 2. Larger item is inserted: the new height of the current row
     * will be added for the next checkpoint
     */
    fun appendHorizontally(
        viewSize: Int,
        spanSize: Int,
        bounds: Rect,
        bottomRowOffset: Int
    ): Int {
        var newSpace = 0
        if (bottomRow.fitsEnd(spanSize)) {
            bounds.top = bottomRow.top
            val previousHeight = bottomRow.height
            bounds.left = bottomRow.append(viewSize, spanSize)
            newSpace = if (bottomRow.isEndComplete()) {
                bottomRow.height
            } else {
                max(0, bottomRow.height - previousHeight)
            }
        } else {
            if (bottomRow === topRow) {
                topRow = GridRow(bottomRow)
            }
            bottomRow.next(viewSize, spanSize, newTop = bottomRowOffset)
            bounds.top = bottomRowOffset
            bounds.left = rowStart
        }

        bounds.bottom = bounds.top + viewSize
        bounds.right = bottomRow.getEndOffset()

        return newSpace
    }

    fun prependHorizontally(
        viewSize: Int,
        spanSize: Int,
        bounds: Rect,
        topRowOffset: Int
    ): Int {
        if (topRow.fitsStart(spanSize)) {
            bounds.bottom = topRow.top + topRow.height
            bounds.left = topRow.prepend(viewSize, spanSize)
        } else {
            if (topRow === bottomRow) {
                bottomRow = GridRow(topRow)
            }
            topRow.previous(viewSize, spanSize, newTop = topRowOffset - viewSize)
            bounds.bottom = topRowOffset
            bounds.left = topRow.getStartOffset()
        }
        bounds.top = bounds.bottom - viewSize
        bounds.right = bounds.left + topRow.getSpanSpace() * spanSize
        return if (topRow.isStartComplete() && topRowOffset != topRow.top) {
            topRow.height
        } else {
            0
        }
    }

    fun getTopRowStartOffset() = topRow.getStartOffset()

    fun getTopRowEndOffset() = topRow.getEndOffset()


}
