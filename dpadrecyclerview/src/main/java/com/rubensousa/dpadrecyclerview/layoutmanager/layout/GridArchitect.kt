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
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.recycling.GridRecycler

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

    override fun addPivot(view: View, position: Int, bounds: Rect, layoutState: LayoutState) {
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view)
        val headOffset = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tailOffset = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)
        if (layoutInfo.isVertical()) {
            bounds.top = headOffset
            bounds.bottom = tailOffset
            appendHorizontalSpan(view, position, bounds)
        } else {
            bounds.left = headOffset
            bounds.right = tailOffset
            appendVerticalSpan(view, position, bounds)
        }

        // If the pivot is using the first column,
        // we need to mark this as the new the start of our layout window
        if (layoutInfo.getStartColumnIndex(position) == 0) {
            layoutState.updateWindowStart(headOffset)
            layoutState.updateWindowEnd(headOffset)
        }

        // If the pivot is using the last column,
        // we need to mark this as the new end of our layout window
        if (layoutInfo.isPositionAtLastColumn(position)) {
            layoutState.updateWindowEnd(tailOffset)
        }
    }

    override fun appendView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            bounds.top = layoutState.checkpoint
            bounds.bottom = bounds.top + decoratedSize
            appendHorizontalSpan(view, position, bounds)
        } else {
            // TODO Apply top and bottom bounds
            bounds.left = layoutState.checkpoint
            bounds.right = bounds.left + decoratedSize
            appendVerticalSpan(view, position, bounds)
        }

        layoutState.appendWindow(consumedSpace)
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
            bounds.bottom = layoutState.checkpoint
            bounds.top = bounds.bottom - decoratedSize
            prependHorizontalSpan(view, position, bounds)
        } else {
            // TODO Apply top and bottom bounds
            bounds.right = layoutState.checkpoint
            bounds.left = bounds.right - decoratedSize
            prependHorizontalSpan(view, position, bounds)
        }

        layoutState.prependWindow(consumedSpace)
        return consumedSpace
    }

    /**
     * @return height of the view if it filled the last span or 0
     */
    private fun appendHorizontalSpan(view: View, position: Int, bounds: Rect): Int {
        val startColumnIndex = layoutInfo.getStartColumnIndex(position)
        val spanWidth = layoutManager.width / layoutInfo.getSpanCount()
        bounds.left = spanWidth * startColumnIndex
        // TODO Check if this is greater than the width to move to the next row
        bounds.right = bounds.left + spanWidth
        return if (layoutInfo.isPositionAtLastColumn(position)) {
            bounds.height()
        } else {
            0
        }
    }

    /**
     * @return height of the view if it filled the first span or 0
     */
    private fun prependHorizontalSpan(view: View, position: Int, bounds: Rect): Int {
        val columnIndex = layoutInfo.getStartColumnIndex(position)
        val spanWidth = layoutManager.width / layoutInfo.getSpanCount()
        bounds.right = spanWidth * (columnIndex + 1)
        // TODO Check if this is less than the width to move to the previous row
        bounds.left = bounds.right - spanWidth
        return if (columnIndex == 0) {
            bounds.height()
        } else {
            0
        }
    }

    /**
     * @return height of the view if it filled the last span or 0
     */
    private fun appendVerticalSpan(view: View, position: Int, bounds: Rect): Int {
        val layoutPosition = layoutInfo.getLayoutPositionOf(view)
        return 0
    }

    /**
     * @return height of the view if it filled the first span or 0
     */
    private fun prependHorizontal(view: View, position: Int, bounds: Rect): Int {
        val layoutPosition = layoutInfo.getLayoutPositionOf(view)
        return 0
    }

}
