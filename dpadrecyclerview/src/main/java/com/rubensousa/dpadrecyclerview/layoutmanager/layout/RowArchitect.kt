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
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LayoutResult
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutState

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
    private val layoutAlignment: LayoutAlignment,
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration,
) {

    companion object {
        const val TAG = "RowArchitect"
    }

    private val viewBounds = Rect()

    fun layoutPivot(layoutState: LayoutState, recycler: Recycler, pivotInfo: PivotInfo) {
        val view = recycler.getViewForPosition(pivotInfo.position)
        layoutManager.addView(view)
        layoutManager.measureChildWithMargins(view, 0, 0)
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view, size)
        val headOffset = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tailOffset = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)

        if (configuration.isVertical()) {
            viewBounds.top = headOffset
            viewBounds.bottom = tailOffset
            applyHorizontalGravity(view, viewBounds)
        } else {
            viewBounds.left = headOffset
            viewBounds.right = tailOffset
            applyVerticalGravity(view, viewBounds)
        }
        layoutView(view, viewBounds)
        pivotInfo.headOffset = headOffset
        pivotInfo.tailOffset = tailOffset
        layoutState.setWindowStart(headOffset)
        layoutState.setWindowEnd(tailOffset)
    }

    fun layout(layoutState: LayoutState, recycler: Recycler, state: RecyclerView.State) {
        if (layoutState.isLayingOutEnd()) {
            layoutEnd(layoutState, recycler, state)
        } else {
            layoutStart(layoutState, recycler, state)
        }
    }

    fun layoutEnd(layoutState: LayoutState, recycler: Recycler, state: RecyclerView.State) {
        var remainingSpace = layoutState.fillSpace
        while (shouldContinueLayout(remainingSpace, layoutState, state)) {
            val view = layoutState.getNextView(recycler) ?: return // No more views to layout, exit
            layoutManager.addView(view)
            layoutManager.measureChildWithMargins(view, 0, 0)
            val decoratedSize = layoutInfo.orientationHelper.getDecoratedMeasurement(view)

            if (configuration.isVertical()) {
                // We need to align this view to an edge or center it, depending on the gravity set
                applyHorizontalGravity(view, viewBounds)
                viewBounds.top = layoutState.checkpoint
                viewBounds.bottom = viewBounds.top + decoratedSize
            } else {
                applyVerticalGravity(view, viewBounds)
                viewBounds.left = layoutState.checkpoint
                viewBounds.right = viewBounds.left + decoratedSize
            }
            layoutView(view, viewBounds)
            layoutState.appendWindow(viewBounds.height())
            remainingSpace -= viewBounds.height()
            Log.i(TAG, "LayoutState: $layoutState")
        }
    }

    fun layoutStart(layoutState: LayoutState, recycler: Recycler, state: RecyclerView.State) {
        var remainingSpace = layoutState.fillSpace
        while (shouldContinueLayout(remainingSpace, layoutState, state)) {
            val view = layoutState.getNextView(recycler) ?: return // No more views to layout, exit
            layoutManager.addView(view, 0)
            layoutManager.measureChildWithMargins(view, 0, 0)
            val decoratedSize = layoutInfo.orientationHelper.getDecoratedMeasurement(view)

            if (configuration.isVertical()) {
                applyHorizontalGravity(view, viewBounds)
                viewBounds.bottom = layoutState.checkpoint
                viewBounds.top = viewBounds.bottom - decoratedSize
            } else {
                applyVerticalGravity(view, viewBounds)
                viewBounds.right = layoutState.checkpoint
                viewBounds.left = viewBounds.left - decoratedSize
            }
            layoutView(view, viewBounds)
            layoutState.prependWindow(viewBounds.height())
            remainingSpace -= viewBounds.height()
            Log.i(TAG, "LayoutState: $layoutState")
        }
    }

    private fun shouldContinueLayout(
        remainingSpace: Int,
        layoutState: LayoutState,
        state: RecyclerView.State
    ): Boolean {
        return layoutState.hasMoreItems(state) && (remainingSpace > 0 || layoutState.isInfinite())
    }

    private fun applyHorizontalGravity(view: View, bounds: Rect) {
        val horizontalGravity = if (configuration.reverseLayout) {
            Gravity.getAbsoluteGravity(
                configuration.gravity.and(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK),
                View.LAYOUT_DIRECTION_RTL
            )
        } else {
            configuration.gravity.and(Gravity.HORIZONTAL_GRAVITY_MASK)
        }
        when (horizontalGravity) {
            Gravity.CENTER, Gravity.CENTER_HORIZONTAL -> {
                val width = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.left = layoutManager.width / 2 - width / 2
                bounds.right = bounds.left + width
            }
            Gravity.RIGHT -> {
                val width = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.right = layoutManager.width - layoutManager.paddingRight
                bounds.left = bounds.right - width
            }
            else -> { // Fallback to left gravity since this is the default expected behavior
                bounds.left = layoutManager.paddingLeft
                bounds.right = viewBounds.left + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }

    }

    private fun applyVerticalGravity(view: View, bounds: Rect) {
        when (configuration.gravity.and(Gravity.VERTICAL_GRAVITY_MASK)) {
            Gravity.CENTER, Gravity.CENTER_VERTICAL -> {
                val height = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.top = layoutManager.height / 2 - height / 2
                bounds.bottom = bounds.top + height
            }
            Gravity.BOTTOM -> {
                val height = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.bottom = layoutManager.height - layoutManager.paddingBottom
                bounds.top = bounds.bottom - height
            }
            else -> {  // Fallback to top gravity since this is the default expected behavior
                bounds.top = layoutManager.paddingTop
                bounds.bottom = viewBounds.top + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }
    }

    private fun layoutView(view: View, bounds: Rect) {
        layoutManager.layoutDecoratedWithMargins(
            view, bounds.left, bounds.top, bounds.right, bounds.bottom
        )
    }

    @Deprecated("Needs to use the new layout state class")
    fun layoutChunk(
        recycler: RecyclerView.Recycler,
        layoutState: LinearLayoutState,
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
        result.consumed = layoutInfo.getDecoratedSize(view)

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

    @Deprecated("Use new layout pivot that expects layout state")
    fun layoutPivot(recycler: Recycler, pivotInfo: PivotInfo) {
        val view = recycler.getViewForPosition(pivotInfo.position)
        layoutManager.addView(view)
        layoutManager.measureChildWithMargins(view, 0, 0)
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view, size)
        val headOffset = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tailOffset = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)

        if (configuration.isVertical()) {
            viewBounds.top = headOffset
            viewBounds.bottom = tailOffset
            applyHorizontalGravity(view, viewBounds)
        } else {
            viewBounds.left = headOffset
            viewBounds.right = tailOffset
            applyVerticalGravity(view, viewBounds)
        }
        layoutView(view, viewBounds)
        pivotInfo.headOffset = headOffset
        pivotInfo.tailOffset = tailOffset
    }

}
