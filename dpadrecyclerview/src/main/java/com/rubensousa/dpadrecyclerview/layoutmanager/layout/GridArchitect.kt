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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.recycling.GridRecycler
import kotlin.math.max

// TODO Add a second pass to adjust view heights based on row size
internal class GridArchitect(
    layoutManager: LayoutManager,
    layoutInfo: LayoutInfo,
    gridRecycler: GridRecycler,
    onChildLayoutListener: OnChildLayoutListener,
    private val layoutAlignment: LayoutAlignment
) : StructureArchitect(layoutManager, layoutInfo, gridRecycler, onChildLayoutListener) {

    companion object {
        const val TAG = "GridArchitect"
    }

    private val numberOfSpans = layoutInfo.getSpanCount()
    private val startRow = Row(numberOfSpans, layoutInfo.getSecondaryTotalSpace())
    private val endRow = Row(numberOfSpans, layoutInfo.getSecondaryTotalSpace())

    override fun updateConfiguration() {
        startRow.width = layoutInfo.getSecondaryTotalSpace()
        endRow.width = layoutInfo.getSecondaryTotalSpace()
    }

    override fun offsetBy(offset: Int, layoutState: LayoutState) {
        super.offsetBy(offset, layoutState)
        startRow.offsetBy(-offset)
        endRow.offsetBy(-offset)
    }

    override fun addPivot(view: View, position: Int, bounds: Rect, layoutState: LayoutState) {
        startRow.reset()
        endRow.reset()
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view)
        val head = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tail = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)
        val decoratedSize = tail - head
        val spanSize = layoutInfo.getSpanSize(position)

        startRow.startSpanIndex = layoutInfo.getStartColumnIndex(position)
        startRow.endSpanIndex = startRow.startSpanIndex + spanSize - 1

        if (layoutInfo.isVertical()) {
            bounds.top = head
            bounds.bottom = tail
            bounds.left = startRow.getSpanSpace() * startRow.startSpanIndex
            bounds.right = bounds.left + startRow.getSpanSpace()
        } else {
            bounds.left = head
            bounds.right = tail
            bounds.top = startRow.getSpanSpace() * startRow.startSpanIndex
            bounds.bottom = bounds.top + startRow.getSpanSpace()
        }

        layoutState.updateWindow(head, head)

        // At this stage, both start and end rows are the same
        endRow.startSpanIndex = startRow.startSpanIndex
        endRow.endSpanIndex = startRow.endSpanIndex

        // Place both rows at the correct top position
        startRow.offsetBy(head)
        endRow.offsetBy(head)

        // Set the default height of both rows to the size of the pivot for now
        startRow.updateHeight(decoratedSize, startRow.startSpanIndex, spanSize)
        endRow.updateHeight(decoratedSize, startRow.startSpanIndex, spanSize)
    }

    override fun appendView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            appendToHorizontalRow(decoratedSize, position, bounds, layoutState)
        } else {
            // TODO
            0
        }
        layoutState.appendWindow(consumedSpace)
        return consumedSpace
    }

    private fun appendToHorizontalRow(
        viewSize: Int,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        var consumedSpace = 0

        val spanSize = layoutInfo.getSpanSize(position)

        // Check if we can place the element in the current row
        if (endRow.fitsEnd(spanSize)) {
            bounds.top = endRow.top
            bounds.left = endRow.append(viewSize, spanSize)
            // If this is the last span, consume the total space
            if (endRow.isEndComplete()) {
                consumedSpace = endRow.consume()
            }
        } else {
            // Otherwise place it in the next row
            endRow.moveToNextRow(viewSize, spanSize, newTop = layoutState.checkpoint)
            bounds.top = layoutState.checkpoint
            bounds.left = layoutInfo.getSecondaryStartAfterPadding()
        }

        bounds.bottom = bounds.top + viewSize
        bounds.right = bounds.left + endRow.getSpanSpace() * spanSize

        return consumedSpace
    }

    override fun prependView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            prependToHorizontalRow(decoratedSize, position, bounds, layoutState)
        } else {
            // TODO
            0
        }
        layoutState.prependWindow(consumedSpace)
        return consumedSpace
    }

    private fun prependToHorizontalRow(
        viewSize: Int,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState,
    ): Int {
        var consumedSpace = 0

        val spanSize = layoutInfo.getSpanSize(position)

        // Check if we can place the element in the current row
        if (startRow.fitsStart(spanSize)) {
            bounds.left = startRow.prepend(viewSize, spanSize)
            bounds.bottom = startRow.top + startRow.height

            // If this is the first span, consume the total space
            if (startRow.isStartComplete()) {
                consumedSpace = startRow.consume()
            }
        } else {
            // Otherwise move it to the previous row
            startRow.moveToPreviousRow(
                viewSize, spanSize,
                newTop = layoutState.checkpoint - viewSize
            )
            bounds.bottom = layoutState.checkpoint
            bounds.left = startRow.getSpanSpace() * startRow.startSpanIndex
        }

        bounds.top = bounds.bottom - viewSize
        bounds.right = bounds.left + startRow.getSpanSpace() * spanSize

        return consumedSpace
    }

    class Row(
        private val numberOfSpans: Int,
        var width: Int
    ) {

        var startSpanIndex = RecyclerView.NO_POSITION
        var endSpanIndex = RecyclerView.NO_POSITION

        var height = 0
            private set

        var top = 0
            private set

        private val heights = IntArray(numberOfSpans)

        fun offsetBy(offset: Int) {
            top += offset
        }

        fun fitsEnd(spanSize: Int): Boolean {
            return endSpanIndex + spanSize < numberOfSpans
        }

        fun isEndComplete(): Boolean {
            return endSpanIndex == numberOfSpans - 1
        }

        fun isStartComplete(): Boolean {
            return startSpanIndex == 0
        }

        fun fitsStart(spanSize: Int): Boolean {
            return startSpanIndex - spanSize >= 0
        }

        fun getSpanSpace(): Int {
            return width / numberOfSpans
        }

        fun append(viewSize: Int, spanSize: Int): Int {
            val viewSpanIndex = endSpanIndex + 1
            val viewStart = getSpanSpace() * viewSpanIndex
            updateHeight(viewSize, viewSpanIndex, spanSize)
            endSpanIndex += spanSize
            return viewStart
        }

        fun prepend(viewSize: Int, spanSize: Int): Int {
            startSpanIndex -= spanSize
            updateHeight(viewSize, startSpanIndex, spanSize)
            return getSpanSpace() * startSpanIndex
        }

        fun moveToNextRow(viewSize: Int, spanSize: Int, newTop: Int) {
            consume()
            startSpanIndex = 0
            endSpanIndex = spanSize - 1
            top = newTop
            updateHeight(viewSize, startSpanIndex, spanSize)
        }

        fun moveToPreviousRow(viewSize: Int, spanSize: Int, newTop: Int) {
            consume()
            endSpanIndex = numberOfSpans - 1
            startSpanIndex = endSpanIndex + 1 - spanSize
            top = newTop
            updateHeight(viewSize, startSpanIndex, spanSize)
        }

        fun consume(): Int {
            heights.fill(0)
            val previousHeight = height
            height = 0
            return previousHeight
        }

        fun reset() {
            consume()
            top = 0
        }

        fun updateHeight(viewSize: Int, spanIndex: Int, spanSize: Int) {
            height = max(viewSize, height)
            for (i in spanIndex until spanIndex + spanSize) {
                heights[i] = viewSize
            }
        }

    }


}
