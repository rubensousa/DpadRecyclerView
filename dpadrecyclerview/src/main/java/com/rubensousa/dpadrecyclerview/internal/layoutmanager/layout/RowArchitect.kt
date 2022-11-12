/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

package com.rubensousa.dpadrecyclerview.internal.layoutmanager.layout

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.LayoutConfiguration

/**
 * Layout algorithm:
 * 1. Layout the view at the selected position and align it
 * 2. Starting from the bottom/end of the selected view,
 * fill towards the top/start until there's no more space
 * 3. Starting from the top/end of the selected view,
 * fill towards the top/start until there's no more space
 *
 * This was mostly adapted from LinearLayoutManager.
 */
internal class RowArchitect(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration,
) {

    private var pivotLocation = Rect()
    private var viewOffsetLocation = Rect()

    // TODO Use specific parent keyline alignment + child alignment and move this to another class
    private fun calculateParentKeyline(): Int {
        return if (configuration.isHorizontal()) {
            layoutManager.width / 2
        } else {
            layoutManager.height / 2
        }
    }

    fun layoutPivot(
        position: Int,
        recycler: Recycler,
        pivotInfo: PivotInfo
    ) {
        val keyline = calculateParentKeyline()
        val view = addView(position, recycler, LayoutState.LayoutDirection.START)
        layoutManager.measureChildWithMargins(view, 0, 0)
        val size = layoutInfo.getMeasuredSize(view)
        val perpendicularSize = layoutInfo.getPerpendicularDecoratedSize(view)

        val headOffset = keyline - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tailOffset = keyline + size / 2 + layoutInfo.getEndDecorationSize(view)

        // TODO support gravity here
        if (configuration.isVertical()) {
            pivotLocation.top = headOffset
            pivotLocation.bottom = tailOffset
            pivotLocation.left = layoutManager.paddingLeft
            pivotLocation.right = pivotLocation.left + perpendicularSize
        } else {
            pivotLocation.left = headOffset
            pivotLocation.right = tailOffset
            pivotLocation.top = layoutManager.paddingTop
            pivotLocation.bottom = pivotLocation.top + perpendicularSize
        }
        layoutView(view, pivotLocation)
        pivotInfo.position = position
        pivotInfo.headOffset = headOffset
        pivotInfo.tailOffset = tailOffset
    }

    fun layoutChunk(
        recycler: RecyclerView.Recycler,
        layoutState: LayoutState,
        result: LayoutResult
    ) {
        val view = layoutState.next(recycler)
        if (view == null) {
            // if we are laying out views in scrap, this may return null which means there is
            // no more items to layout.
            result.finished = true
            return
        }
        val params = view.layoutParams as RecyclerView.LayoutParams
        if (layoutState.scrappedViews == null) {
            if (layoutState.reverseLayout == layoutState.isLayingOutStart()) {
                layoutManager.addView(view)
            } else {
                layoutManager.addView(view, 0)
            }
        } else if (layoutState.reverseLayout == layoutState.isLayingOutStart()) {
            layoutManager.addDisappearingView(view)
        } else {
            layoutManager.addDisappearingView(view, 0)
        }
        layoutManager.measureChildWithMargins(view, 0, 0)
        result.consumed = layoutInfo.orientationHelper.getDecoratedMeasurement(view)
        val left: Int
        val top: Int
        val right: Int
        val bottom: Int
        if (configuration.isVertical()) {
            // Support gravity here
            if (layoutInfo.isRTL()) {
                right = layoutManager.width - layoutManager.paddingRight
                left = right - layoutInfo.getPerpendicularDecoratedSize(view)
            } else {
                left = layoutManager.paddingLeft
                right = left + layoutInfo.getPerpendicularDecoratedSize(view)
            }
            if (layoutState.isLayingOutStart()) {
                bottom = layoutState.offset
                top = layoutState.offset - result.consumed
            } else {
                top = layoutState.offset
                bottom = layoutState.offset + result.consumed
            }
        } else {
            top = layoutManager.paddingTop
            bottom = top + layoutInfo.getPerpendicularDecoratedSize(view)
            if (layoutState.isLayingOutStart()) {
                right = layoutState.offset
                left = layoutState.offset - result.consumed
            } else {
                left = layoutState.offset
                right = layoutState.offset + result.consumed
            }
        }
        // We calculate everything with View's bounding box (which includes decor and margins)
        // To calculate correct layout position, we subtract margins.
        layoutManager.layoutDecoratedWithMargins(view, left, top, right, bottom)

        // Consume the available space if the view is not removed OR changed
        if (params.isItemRemoved || params.isItemChanged) {
            result.ignoreConsumed = true
        }
    }


    private fun updateViewOffsetLocation(view: View, start: Boolean) {
        layoutManager.measureChildWithMargins(view, 0, 0)
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
            viewOffsetLocation.left = layoutManager.paddingLeft
            viewOffsetLocation.right = viewOffsetLocation.left + perpendicularSize
        } else {
            if (start) {
                viewOffsetLocation.right = viewOffsetLocation.left
                viewOffsetLocation.left = viewOffsetLocation.right - size
            } else {
                viewOffsetLocation.left = viewOffsetLocation.right
                viewOffsetLocation.right = viewOffsetLocation.left + size
            }
            viewOffsetLocation.top = layoutManager.paddingTop
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
        layoutManager.layoutDecoratedWithMargins(
            view,
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom
        )
    }

    private fun addView(
        position: Int,
        recycler: Recycler,
        direction: LayoutState.LayoutDirection
    ): View {
        val view = recycler.getViewForPosition(position)
        if (direction == LayoutState.LayoutDirection.START) {
            layoutManager.addView(view, 0)
        } else {
            layoutManager.addView(view)
        }
        return view
    }

    private fun calculateRemainingSpace(limit: Int): Int {
        return if (configuration.isVertical()) {
            limit - pivotLocation.height()
        } else {
            limit - pivotLocation.width()
        }
    }

}
