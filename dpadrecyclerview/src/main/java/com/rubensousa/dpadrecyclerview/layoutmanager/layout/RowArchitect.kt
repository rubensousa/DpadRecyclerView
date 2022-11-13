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
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration

/**
 * Layout algorithm:
 * 1. Layout the view at the selected position (pivot) and align it
 * 2. Starting from the bottom/end of the selected view,
 * fill towards the top/start until there's no more space
 * 3. Starting from the top/end of the selected view,
 * fill towards the top/start until there's no more space
 *
 * - [layoutPivot] - creates the pivot view and aligns it based on the alignment configuration
 * - [layoutChunk] - creates the next view and aligns it based on the current layout state
 * (Adapted from LinearLayoutManager)
 */
internal class RowArchitect(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration,
) {

    companion object {
        const val TAG = "RowArchitect"
    }

    private val viewBounds = Rect()

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
        val view = recycler.getViewForPosition(position)
        layoutManager.addView(view)
        layoutManager.measureChildWithMargins(view, 0, 0)
        val size = layoutInfo.getMeasuredSize(view)
        val headOffset = keyline - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tailOffset = keyline + size / 2 + layoutInfo.getEndDecorationSize(view)

        if (configuration.isVertical()) {
            applyHorizontalGravity(view, viewBounds)
            viewBounds.top = headOffset
            viewBounds.bottom = tailOffset
        } else {
            applyVerticalGravity(view, viewBounds)
            viewBounds.left = headOffset
            viewBounds.right = tailOffset
        }
        layoutView(view, viewBounds)
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
        Log.d(TAG, "LayoutChunk requested for: $layoutState")
        if (view == null) {
            // if we are laying out views in scrap, this may return null which means there is
            // no more items to layout.
            Log.d(TAG, "LayoutChunk finished for: $layoutState")
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

        if (configuration.isVertical()) {
            // We need to align this view to an edge or center it, depending on the gravity set
            applyHorizontalGravity(view, viewBounds)
            if (layoutState.isLayingOutStart()) {
                viewBounds.bottom = layoutState.offset
                viewBounds.top = layoutState.offset - result.consumed
            } else {
                viewBounds.top = layoutState.offset
                viewBounds.bottom = layoutState.offset + result.consumed
            }
        } else {
            // We need to align this view to an edge or center it, depending on the gravity set
            applyVerticalGravity(view, viewBounds)
            if (layoutState.isLayingOutStart()) {
                viewBounds.right = layoutState.offset
                viewBounds.left = layoutState.offset - result.consumed
            } else {
                viewBounds.left = layoutState.offset
                viewBounds.right = layoutState.offset + result.consumed
            }
        }
        // We calculate everything with View's bounding box (which includes decor and margins)
        // To calculate correct layout position, we subtract margins.
        layoutView(view, viewBounds)

        // Consume the available space if the view is not removed OR changed
        if (params.isItemRemoved || params.isItemChanged) {
            result.ignoreConsumed = true
        }
    }

    // TODO Support all gravity types
    private fun applyHorizontalGravity(view: View, bounds: Rect) {
        if (layoutInfo.isRTL()) {
            bounds.right = layoutManager.width - layoutManager.paddingRight
            bounds.left = viewBounds.right - layoutInfo.getPerpendicularDecoratedSize(view)
        } else {
            bounds.left = layoutManager.paddingLeft
            bounds.right = viewBounds.left + layoutInfo.getPerpendicularDecoratedSize(view)
        }
    }

    // TODO Support all gravity types
    private fun applyVerticalGravity(view: View, bounds: Rect) {
        bounds.top = layoutManager.paddingTop
        bounds.bottom = viewBounds.top + layoutInfo.getPerpendicularDecoratedSize(view)
    }

    private fun layoutView(view: View, bounds: Rect) {
        layoutManager.layoutDecoratedWithMargins(
            view, bounds.left, bounds.top, bounds.right, bounds.bottom
        )
    }

}
