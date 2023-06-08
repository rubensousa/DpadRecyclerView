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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear

import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.collection.forEach
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutResult
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.OnChildLayoutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.StructureEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider.ScrapViewProvider
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider.ViewProvider
import kotlin.math.max

/**
 * General layout algorithm:
 * 1. Layout the view at the selected position (pivot) and align it to the keyline
 * 2. Starting from the bottom/end of the selected view,
 * fill towards the top/start until there's no more space
 * 3. Starting from the top/end of the selected view,
 * fill towards the top/start until there's no more space
 */
internal class LinearLayoutEngineer(
    layoutManager: RecyclerView.LayoutManager,
    layoutInfo: LayoutInfo,
    layoutAlignment: LayoutAlignment,
    private val onChildLayoutListener: OnChildLayoutListener,
) : StructureEngineer(layoutManager, layoutInfo, layoutAlignment) {

    companion object {
        const val TAG = "LinearLayoutEngineer"
    }

    override fun initLayout(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): View {
        val pivotView = recycler.getViewForPosition(pivotPosition)
        layoutPivot(pivotView, layoutRequest)

        // Layout from pivot to start
        layoutRequest.prepend(pivotPosition) {
            setCheckpoint(layoutInfo.getDecoratedStart(pivotView))
            setFillSpace(max(0, checkpoint - layoutInfo.getStartAfterPadding()))
        }
        fill(layoutRequest, viewProvider, recycler, state)

        // Layout from pivot to end
        layoutRequest.append(pivotPosition) {
            setCheckpoint(layoutInfo.getDecoratedEnd(pivotView))
            setFillSpace(max(0, layoutInfo.getEndAfterPadding() - checkpoint))
        }
        fill(layoutRequest, viewProvider, recycler, state)

        return pivotView
    }

    private fun layoutPivot(pivotView: View, layoutRequest: LayoutRequest) {
        layoutManager.addView(pivotView)
        updatePivotBounds(pivotView, viewBounds, layoutRequest)
        onChildLayoutListener.onChildCreated(pivotView)

        performLayout(pivotView, viewBounds)
        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "Laid pivot ${layoutInfo.getLayoutPositionOf(pivotView)} at: $viewBounds")
        }
        viewBounds.setEmpty()
        onChildLayoutListener.onChildLaidOut(pivotView)
    }

    override fun layoutLoop(
        pivotView: View,
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Boolean {
        val lastView = layoutInfo.getChildClosestToEnd() ?: return false
        val lastViewPosition = layoutInfo.getLayoutPositionOf(lastView)
        if (lastViewPosition == RecyclerView.NO_POSITION) {
            return false
        }

        val firstView = layoutInfo.getChildClosestToStart() ?: return false
        val firstViewPosition = layoutInfo.getLayoutPositionOf(firstView)
        if (firstViewPosition == RecyclerView.NO_POSITION) {
            return false
        }

        val distanceToStartEdge = max(0, layoutInfo.getDecoratedStart(pivotView))
        val distanceToEndEdge =
            max(0, layoutInfo.getEndAfterPadding() - layoutInfo.getDecoratedEnd(pivotView))
        val requiredEndSpace = distanceToEndEdge + layoutInfo.getDecoratedSize(pivotView)
        val requiredStartSpace = distanceToStartEdge + layoutInfo.getDecoratedSize(pivotView)
        val requiredTotalSpace = layoutInfo.getTotalSpace() + layoutInfo.getDecoratedSize(pivotView)

        layoutRequest.setRecyclingEnabled(true)

        layoutRequest.append(lastViewPosition) {
            setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
            setFillSpace(requiredStartSpace)
        }
        fill(layoutRequest, viewProvider, recycler, state)
        val newEndEdge = layoutRequest.checkpoint
        viewRecycler.recycleFromEnd(recycler, layoutRequest)

        layoutRequest.prepend(firstViewPosition) {
            setCheckpoint(layoutInfo.getDecoratedStart(firstView))
            setFillSpace(requiredEndSpace)
        }
        fill(layoutRequest, viewProvider, recycler, state)
        val newStartEdge = layoutRequest.checkpoint
        viewRecycler.recycleFromStart(recycler, layoutRequest)

        layoutRequest.setRecyclingEnabled(false)
        // If we couldn't fill the required space, don't allow looping
        if (newEndEdge - newStartEdge < requiredTotalSpace) {
            layoutRequest.setCurrentPosition(lastViewPosition)
            layoutRequest.moveToNextPosition()
            return false
        }

        // Allow looping to fill the required space
        layoutRequest.setIsLoopingAllowed(true)

        // Fill the end of the layout with the first items
        layoutRequest.append(lastViewPosition) {
            setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
            setFillSpace(
                max(0, layoutInfo.getEndAfterPadding() - layoutInfo.getDecoratedEnd(lastView))
            )
        }
        fill(layoutRequest, viewProvider, recycler, state)

        // Fill the start of the layout with the last items
        layoutRequest.prepend(firstViewPosition) {
            setCheckpoint(layoutInfo.getDecoratedStart(firstView))
            setFillSpace(
                max(0, layoutInfo.getDecoratedStart(firstView) - layoutInfo.getStartAfterPadding())
            )
        }
        fill(layoutRequest, viewProvider, recycler, state)

        return true
    }

    override fun layoutDisappearingViews(
        firstView: View,
        lastView: View,
        layoutRequest: LayoutRequest,
        scrapViewProvider: ScrapViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        val firstViewPosition = layoutInfo.getLayoutPositionOf(firstView)
        var scrapExtraStart = 0
        var scrapExtraEnd = 0
        scrapViewProvider.getScrap().forEach { position, viewHolder ->
            val direction = if (position < firstViewPosition != layoutRequest.reverseLayout) {
                LayoutDirection.START
            } else {
                LayoutDirection.END
            }
            if (direction == LayoutDirection.START) {
                scrapExtraStart += layoutInfo.getDecoratedSize(viewHolder.itemView)
            } else {
                scrapExtraEnd += layoutInfo.getDecoratedSize(viewHolder.itemView)
            }
        }

        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "Scrap extra layout: $scrapExtraStart, $scrapExtraEnd")
        }

        if (scrapExtraStart > 0) {
            layoutRequest.prepend(layoutInfo.getLayoutPositionOf(firstView)) {
                setRecyclingEnabled(false)
                setCheckpoint(layoutInfo.getDecoratedStart(firstView))
                setFillSpace(scrapExtraStart)
                scrapViewProvider.setNextLayoutPosition(this)
            }
            fill(layoutRequest, scrapViewProvider, recycler, state)
        }

        if (scrapExtraEnd > 0) {
            layoutRequest.append(layoutInfo.getLayoutPositionOf(lastView)) {
                setRecyclingEnabled(false)
                setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
                setFillSpace(scrapExtraEnd)
                scrapViewProvider.setNextLayoutPosition(this)
            }
            fill(layoutRequest, scrapViewProvider, recycler, state)
        }
    }

    /**
     * Places the pivot in the correct layout position and returns its bounds via [bounds]
     */
    private fun updatePivotBounds(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest) {
        layoutManager.measureChildWithMargins(view, 0, 0)
        val size = if (layoutRequest.isVertical) {
            view.measuredHeight
        } else {
            view.measuredWidth
        }
        val viewStart = layoutAlignment.getChildStart(view)
        val viewEnd = viewStart + size
        val headOffset = viewStart - getStartDecorationSize(view, layoutRequest)
        val tailOffset = viewEnd + getEndDecorationSize(view, layoutRequest)

        if (layoutRequest.isVertical) {
            bounds.top = headOffset
            bounds.bottom = tailOffset
            applyHorizontalGravity(view, bounds, layoutRequest)
        } else {
            bounds.left = headOffset
            bounds.right = tailOffset
            applyVerticalGravity(view, bounds, layoutRequest)
        }
    }

    override fun layoutBlock(
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        layoutResult: LayoutResult
    ) {
        val view = viewProvider.next(layoutRequest, state)
        addView(view, layoutRequest)
        onChildLayoutListener.onChildCreated(view)
        layoutManager.measureChildWithMargins(view, 0, 0)

        layoutResult.consumedSpace = if (layoutRequest.isAppending()) {
            append(view, viewBounds, layoutRequest)
        } else {
            prepend(view, viewBounds, layoutRequest)
        }

        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "Laid out view ${layoutInfo.getLayoutPositionOf(view)} at: $viewBounds")
        }

        if (shouldSkipSpaceOf(view)) {
            layoutResult.skipConsumption = true
        }

        performLayout(view, viewBounds)
        viewBounds.setEmpty()
        onChildLayoutListener.onChildLaidOut(view)
        onChildLayoutListener.onBlockLaidOut()
    }

    private fun append(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        if (layoutRequest.isVertical) {
            // We need to align this view to an edge or center it, depending on the gravity set
            applyHorizontalGravity(view, bounds, layoutRequest)
            bounds.top = layoutRequest.checkpoint
            bounds.bottom = bounds.top + decoratedSize
        } else {
            applyVerticalGravity(view, bounds, layoutRequest)
            bounds.left = layoutRequest.checkpoint
            bounds.right = bounds.left + decoratedSize
        }
        return decoratedSize
    }

    private fun prepend(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        if (layoutRequest.isVertical) {
            applyHorizontalGravity(view, bounds, layoutRequest)
            bounds.bottom = layoutRequest.checkpoint
            bounds.top = bounds.bottom - decoratedSize

        } else {
            applyVerticalGravity(view, bounds, layoutRequest)
            bounds.right = layoutRequest.checkpoint
            bounds.left = bounds.right - decoratedSize
        }
        return decoratedSize
    }

    private fun applyHorizontalGravity(
        view: View,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    ) {
        val horizontalGravity = if (layoutRequest.reverseLayout) {
            Gravity.getAbsoluteGravity(
                layoutRequest.gravity.and(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK),
                View.LAYOUT_DIRECTION_RTL
            )
        } else {
            layoutRequest.gravity.and(Gravity.HORIZONTAL_GRAVITY_MASK)
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
                bounds.right = bounds.left + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }

    }

    private fun applyVerticalGravity(view: View, bounds: ViewBounds, layoutRequest: LayoutRequest) {
        when (layoutRequest.gravity.and(Gravity.VERTICAL_GRAVITY_MASK)) {
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
                bounds.bottom = bounds.top + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }
    }

    private fun getStartDecorationSize(view: View, layoutRequest: LayoutRequest): Int {
        return if (layoutRequest.isVertical) {
            layoutManager.getTopDecorationHeight(view)
        } else {
            layoutManager.getLeftDecorationWidth(view)
        }
    }

    private fun getEndDecorationSize(view: View, layoutRequest: LayoutRequest): Int {
        return if (layoutRequest.isVertical) {
            layoutManager.getBottomDecorationHeight(view)
        } else {
            layoutManager.getRightDecorationWidth(view)
        }
    }

}
