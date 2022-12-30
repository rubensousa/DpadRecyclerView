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
    private val rowSizes = IntArray(numberOfSpans)
    private var maxRowSize = 0
    private var startSpanIndex = RecyclerView.NO_POSITION
    private var endSpanIndex = RecyclerView.NO_POSITION

    override fun addPivot(view: View, position: Int, bounds: Rect, layoutState: LayoutState) {
        resetRowSizes()

        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view)
        val head = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tail = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)
        val decoratedSize = tail - head

        startSpanIndex = layoutInfo.getStartColumnIndex(position)
        val spanSize = layoutInfo.getSpanSize(position)
        endSpanIndex = startSpanIndex + spanSize - 1

        val spanSpace = getSpanSpace()

        if (layoutInfo.isVertical()) {
            bounds.top = head
            bounds.bottom = tail
            bounds.left = spanSpace * startSpanIndex
            bounds.right = bounds.left + spanSpace
        } else {
            bounds.left = head
            bounds.right = tail
            bounds.top = spanSpace * startSpanIndex
            bounds.bottom = bounds.left + spanSpace
        }

        layoutState.updateWindow(head, head)

        // Fill the row sizes with the pivot size
        updateRowSizes(decoratedSize, startSpanIndex, spanSize)
    }

    override fun appendView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            appendVertical(decoratedSize, position, bounds, layoutState)
        } else {
            // TODO
            0
        }
        layoutState.appendWindow(consumedSpace)
        return consumedSpace
    }

    /**
     *
     * @return vertical space consumed (if new row was inserted or row size changed)
     */
    private fun appendVertical(
        size: Int,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        var newSpace = 0

        bounds.top = layoutState.checkpoint
        bounds.bottom = bounds.top + size

        val viewSpanSize = layoutInfo.getSpanSize(position)
        val spanWidth = getSpanSpace()

        // Check if we can place the element in the current row
        if (endSpanIndex + viewSpanSize < numberOfSpans) {
            bounds.left = spanWidth * (endSpanIndex + 1)
            endSpanIndex += viewSpanSize
            // If this is the last span, consume the total space
            if (endSpanIndex == numberOfSpans - 1) {
                newSpace = max(size, maxRowSize)
                resetRowSizes()
            } else {
                newSpace = max(0, size - maxRowSize)
            }
        } else {
            // Otherwise place it in the next row
            startSpanIndex = 0
            endSpanIndex = viewSpanSize - 1
            bounds.left = layoutInfo.getSecondaryStartAfterPadding()
        }

        bounds.right = bounds.left + spanWidth * viewSpanSize

        updateRowSizes(size, endSpanIndex, viewSpanSize)

        return newSpace
    }

    override fun prependView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            prependVertical(decoratedSize, position, bounds, layoutState)
        } else {
            // TODO
            0
        }
        layoutState.prependWindow(consumedSpace)
        return consumedSpace
    }

    private fun prependVertical(
        size: Int,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        var newSpace = 0

        bounds.bottom = layoutState.checkpoint
        bounds.top = bounds.bottom - size

        val viewSpanSize = layoutInfo.getSpanSize(position)
        val spanWidth = getSpanSpace()

        // Check if we can place the element in the current row
        if (startSpanIndex - viewSpanSize >= 0) {
            bounds.right = spanWidth * startSpanIndex
            startSpanIndex -= viewSpanSize
            // If this is the first span, consume the total space
            if (startSpanIndex == 0) {
                newSpace = max(size, maxRowSize)
                resetRowSizes()
            } else {
                newSpace = max(0, size - maxRowSize)
            }
        } else {
            // Otherwise move it to the previous row
            endSpanIndex = numberOfSpans - 1
            bounds.right =  layoutInfo.getSecondaryEndAfterPadding()
            startSpanIndex = endSpanIndex + 1 - viewSpanSize
        }

        bounds.left = bounds.right - spanWidth * viewSpanSize

        updateRowSizes(size, startSpanIndex, viewSpanSize)

        return newSpace
    }

    private fun getSpanSpace(): Int {
        return layoutInfo.getSecondaryTotalSpace() / numberOfSpans
    }

    private fun updateRowSizes(size: Int, startSpanIndex: Int, spanSize: Int) {
        maxRowSize = max(size, maxRowSize)
        for (i in startSpanIndex until startSpanIndex + spanSize) {
            rowSizes[i] = size
        }
    }

    private fun resetRowSizes() {
        rowSizes.fill(0)
        maxRowSize = 0
    }


}
