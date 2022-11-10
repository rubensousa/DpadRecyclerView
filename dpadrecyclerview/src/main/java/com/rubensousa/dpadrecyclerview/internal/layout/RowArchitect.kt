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

package com.rubensousa.dpadrecyclerview.internal.layout

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler

internal class RowArchitect(
    private val configuration: TvLayoutConfiguration,
    private val layout: LayoutManager,
    private val layoutInfo: TvLayoutInfo,
) {

    private var anchorViewLocation = Rect()
    private var viewOffsetLocation = Rect()

    /**
     * Layout algorithm:
     * 1. Layout the view at the selected position and align it
     * 2. Starting from the bottom/end of the selected view,
     * fill towards the top/start until there's no more space
     * 3. Starting from the top/end of the selected view,
     * fill towards the top/start until there's no more space
     */
    fun layout(selectedPosition: Int, keyline: Int, limit: Int, itemCount: Int, recycler: Recycler) {
        layoutAnchor(selectedPosition, keyline, recycler)
        // Offset location should be at the anchor
        viewOffsetLocation.set(anchorViewLocation)
        layoutBeforeAnchor(selectedPosition, limit, recycler)

        // Offset location should be at again at the anchor to layout the second part
        viewOffsetLocation.set(anchorViewLocation)
        layoutAfterAnchor(selectedPosition, limit, itemCount, recycler)
    }

    private fun layoutAnchor(anchorPosition: Int, keyline: Int, recycler: Recycler) {
        val view = addView(anchorPosition, recycler, LayoutDirection.START)
        layout.measureChildWithMargins(view, 0, 0)
        val size = layoutInfo.getMeasuredSize(view)
        val perpendicularSize = layoutInfo.getPerpendicularDecoratedSize(view)

        val offsetBeforeAnchor = keyline - size / 2 - layoutInfo.getStartDecorationSize(view)
        val offsetAfterAnchor = keyline + size / 2 + layoutInfo.getEndDecorationSize(view)

        // TODO support gravity here
        if (configuration.isVertical()) {
            anchorViewLocation.top = offsetBeforeAnchor
            anchorViewLocation.bottom = offsetAfterAnchor
            anchorViewLocation.left = layout.paddingLeft
            anchorViewLocation.right = anchorViewLocation.left + perpendicularSize
        } else {
            anchorViewLocation.left = offsetBeforeAnchor
            anchorViewLocation.right = offsetAfterAnchor
            anchorViewLocation.top = layout.paddingTop
            anchorViewLocation.bottom = anchorViewLocation.top + perpendicularSize
        }
        layoutView(view, anchorViewLocation)
    }

    private fun layoutBeforeAnchor(anchorPosition: Int, limit: Int, recycler: Recycler) {
        var remainingSpace = calculateRemainingSpace(limit)
        var currentPosition = anchorPosition - 1
        while (remainingSpace >= 0 && currentPosition >= 0) {
            val view = addView(currentPosition, recycler, LayoutDirection.START)
            updateViewOffsetLocation(view, start = true)
            layoutView(view, viewOffsetLocation)
            currentPosition--
            remainingSpace = updateRemainingSpace(remainingSpace)
        }
    }

    private fun layoutAfterAnchor(anchorPosition: Int, itemCount: Int, limit: Int, recycler: Recycler) {
        // Current offset location should be set to the anchor
        viewOffsetLocation.set(anchorViewLocation)
        var remainingSpace = calculateRemainingSpace(limit)
        var currentPosition = anchorPosition + 1
        while (remainingSpace >= 0 && currentPosition < itemCount) {
            val view = addView(currentPosition, recycler, LayoutDirection.END)
            updateViewOffsetLocation(view, start = false)
            layoutView(view, viewOffsetLocation)
            currentPosition++
            remainingSpace = updateRemainingSpace(remainingSpace)
        }
    }

    private fun updateViewOffsetLocation(view: View, start: Boolean) {
        layout.measureChildWithMargins(view, 0, 0)
        val size = layoutInfo.getDecoratedSize(view)
        val perpendicularSize = layoutInfo.getPerpendicularDecoratedSize(view)

        // TODO support gravity here
        if (configuration.isVertical()) {
            if (start) {
                viewOffsetLocation.bottom = viewOffsetLocation.top
                viewOffsetLocation.top = viewOffsetLocation.bottom - size
            } else {
                viewOffsetLocation.top = viewOffsetLocation.bottom
                viewOffsetLocation.bottom = viewOffsetLocation.top + size
            }
            viewOffsetLocation.left = layout.paddingLeft
            viewOffsetLocation.right = viewOffsetLocation.left + perpendicularSize
        } else {
            if (start) {
                viewOffsetLocation.right = viewOffsetLocation.left
                viewOffsetLocation.left = viewOffsetLocation.right - size
            } else {
                viewOffsetLocation.left = viewOffsetLocation.right
                viewOffsetLocation.right = viewOffsetLocation.left + size
            }
            viewOffsetLocation.top = layout.paddingTop
            viewOffsetLocation.bottom = viewOffsetLocation.top + perpendicularSize
        }
    }

    private fun updateRemainingSpace(remainingSpace: Int): Int {
        return if (configuration.isVertical()) {
            remainingSpace - viewOffsetLocation.height()
        } else {
            remainingSpace - viewOffsetLocation.width()
        }
    }

    private fun layoutView(view: View, bounds: Rect) {
        layout.layoutDecoratedWithMargins(
            view,
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )
    }

    private fun addView(position: Int, recycler: Recycler, direction: LayoutDirection): View {
        val view = recycler.getViewForPosition(position)
        if (direction == LayoutDirection.START) {
            layout.addView(view, 0)
        } else {
            layout.addView(view)
        }
        return view
    }

    private fun calculateRemainingSpace(limit: Int): Int {
        return if (configuration.isVertical()) {
            limit - anchorViewLocation.height()
        } else {
            limit - anchorViewLocation.width()
        }
    }

    private fun layoutDecoratedWithMargins(
        left: Int, top: Int, right: Int, bottom: Int,
        width: Int, height: Int, viewBounds: Rect
    ) {
        var viewLeft = left
        var viewTop = top
        var viewRight = right
        var viewBottom = bottom
        val gravity = configuration.gravity
        if (configuration.isHorizontal() && gravity != Gravity.TOP && configuration.spanCount == 1) {
            if (gravity == Gravity.CENTER) {
                val viewHeight = viewBottom - viewTop
                viewTop = height / 2 - viewHeight / 2
                viewBottom = viewTop + viewHeight
            } else if (gravity == Gravity.BOTTOM) {
                val viewHeight = viewBottom - viewTop
                viewBottom = height
                viewTop = viewBottom - viewHeight
            }
        } else if (configuration.isVertical() && gravity != Gravity.START && configuration.spanCount == 1) {
            if (gravity == Gravity.CENTER) {
                val viewWidth = viewRight - viewLeft
                viewLeft = width / 2 - viewWidth / 2
                viewRight = viewLeft + viewWidth
            } else if (gravity == Gravity.END) {
                val viewWidth = viewRight - viewLeft
                viewRight = width
                viewLeft = viewRight - viewWidth
            }
        }
        viewBounds.set(viewLeft, viewTop, viewRight, viewBottom)
    }

}
