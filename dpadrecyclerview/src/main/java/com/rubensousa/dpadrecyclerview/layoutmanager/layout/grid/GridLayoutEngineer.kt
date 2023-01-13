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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import android.graphics.Rect
import android.util.Log
import android.util.SparseIntArray
import android.view.View
import android.view.View.MeasureSpec
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutResult
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.OnChildLayoutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.StructureEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import kotlin.math.max

internal class GridLayoutEngineer(
    layoutManager: LayoutManager,
    layoutInfo: LayoutInfo,
    layoutAlignment: LayoutAlignment,
    private val onChildLayoutListener: OnChildLayoutListener
) : StructureEngineer(layoutManager, layoutInfo, layoutAlignment) {

    companion object {
        const val TAG = "GridLayoutEngineer"
    }

    private val insets = Rect()
    private val preLayoutSpanSizeCache = SparseIntArray()
    private val preLayoutSpanIndexCache = SparseIntArray()
    private val rowViews = Array<View?>(layoutInfo.getSpanCount()) { null }
    private var pivotView: View? = null
    private val layoutRow = GridRow(
        numberOfSpans = layoutInfo.getSpanCount(),
        width = layoutInfo.getSecondaryTotalSpace()
    )
    private val architect = GridLayoutArchitect(layoutInfo)
    private var pivotLayoutPosition = RecyclerView.NO_POSITION

    override fun getArchitect(): LayoutArchitect = architect

    override fun onLayoutStarted(state: State) {
        super.onLayoutStarted(state)
        layoutRow.setWidth(layoutInfo.getSecondaryTotalSpace())
    }

    override fun onChildrenOffset(offset: Int) {
        super.onChildrenOffset(offset)
        layoutRow.offsetBy(offset)
    }

    override fun initLayout(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ): View {
        pivotLayoutPosition = pivotPosition
        layoutRow.reset(layoutAlignment.getParentKeyline())
        val pivotView = layoutPivotRow(pivotPosition, layoutRequest, recycler, state)
        layoutFromPivotToStart(layoutRequest, recycler, state)
        layoutFromPivotToEnd(layoutRequest, recycler, state)
        pivotLayoutPosition = RecyclerView.NO_POSITION
        return pivotView
    }

    private fun layoutPivotRow(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ): View {
        val pivotSpanIndex = getSpanIndex(recycler, state, pivotPosition)
        val firstSpanPosition = max(0, pivotPosition - pivotSpanIndex)
        layoutRequest.append(firstSpanPosition) {
            setCurrentPosition(firstSpanPosition)
            setCheckpoint(layoutAlignment.getParentKeyline())
            setFillSpace(1)
        }
        fill(layoutRequest, recycler, state)
        return requireNotNull(pivotView)
    }

    private fun layoutFromPivotToStart(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        if (layoutRow.isStartComplete()) {
            return
        }
        layoutRequest.prepend(layoutRow.getFirstPosition()) {
            setCheckpoint(layoutRow.startOffset)
            setFillSpace(checkpoint)
        }
        fill(layoutRequest, recycler, state)
    }

    private fun layoutFromPivotToEnd(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        if (layoutRow.isEndComplete()) {
            return
        }
        layoutRequest.append(layoutRow.getLastPosition()) {
            setCheckpoint(layoutRow.endOffset)
            setFillSpace(max(0, layoutInfo.getEndAfterPadding() - checkpoint))
        }
        fill(layoutRequest, recycler, state)
    }

    override fun onPreLayout() {
        cachePreLayoutSpanMapping()
    }

    override fun onLayoutChildrenFinished() {
        clearPreLayoutSpanMappingCache();
    }

    /**
     * This method will layout an entire row.
     * If the next item doesn't fit in the existing row, then we return early.
     *
     * Setup step:
     * - Calculate remaining spans for filling the current row
     * - Retrieve the views that will be used to fill the current row
     * - Assign a span index to each view
     *
     * Layout step:
     * - Loop through each view found in setup phase and add it
     * - Calculate the ItemDecorations and measure each child
     * - Keep track of the total row height and width
     * - Re-measure all views that didn't have the same height as the row
     * - Layout each view with decorations included
     */
    override fun layoutBlock(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State,
        layoutResult: LayoutResult
    ) {
        layoutRow.reset(keyline = 0)
        val fillSpanCount = calculateSpansToFill(layoutRequest)
        val viewCount = getViewsForRow(layoutRequest, recycler, state, fillSpanCount)
        assignSpans(recycler, state, viewCount, layoutRequest.currentItemDirection)

        val rowHeight = fillRow(viewCount, layoutRow, layoutRequest)
        layoutResult.consumedSpace = rowHeight
        reMeasureChildren(layoutRow, viewCount)

        layoutResult.skipConsumption = layoutRow(viewCount, layoutRow, viewBounds, layoutRequest)
    }

    /**
     * Add views from [rowViews] to the RecyclerView and keeps track of the row size
     */
    private fun fillRow(
        viewCount: Int,
        row: GridRow,
        layoutRequest: LayoutRequest
    ): Int {
        val secondarySpecMode = layoutInfo.orientationHelper.modeInOther
        repeat(viewCount) { index ->
            val view = getRowViewAt(index)
            val layoutParams = layoutInfo.getLayoutParams(view)
            if (layoutRequest.isLayingOutEnd() && !row.fitsEnd(layoutParams.spanSize)) {
                return row.height
            }
            if (layoutRequest.isLayingOutStart() && !row.fitsStart(layoutParams.spanSize)) {
                return row.height
            }
            addView(view, layoutRequest)
            onChildLayoutListener.onChildCreated(view)

            layoutManager.calculateItemDecorationsForChild(view, insets)

            measureChild(view, row, layoutParams, secondarySpecMode, alreadyMeasured = false)

            val decoratedSize = layoutInfo.getDecoratedSize(view)
            if (layoutRequest.isLayingOutEnd()) {
                row.append(decoratedSize, layoutParams.viewLayoutPosition, layoutParams.spanSize)
            } else {
                row.prepend(decoratedSize, layoutParams.viewLayoutPosition, layoutParams.spanSize)
            }
        }
        return row.height
    }

    private fun layoutRow(
        viewCount: Int,
        row: GridRow,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    ): Boolean {
        var skipConsumption = false
        updateLayoutBounds(layoutRequest, row, bounds)

        for (i in 0 until viewCount) {
            val view = getRowViewAt(i)
            val layoutParams = layoutInfo.getLayoutParams(view)
            if (layoutParams.viewLayoutPosition == pivotLayoutPosition) {
                pivotView = view
            }
            val perpendicularSize = layoutInfo.getPerpendicularDecoratedSize(view)
            if (layoutRequest.isVertical) {
                if (layoutInfo.isRTL()) {
                    bounds.right = layoutManager.paddingLeft + row.getSpanBorder(
                        row.numberOfSpans - layoutParams.spanIndex
                    )
                    bounds.left = bounds.right - perpendicularSize
                } else {
                    bounds.left = layoutManager.paddingLeft + row.getSpanBorder(
                        layoutParams.spanIndex
                    )
                    bounds.right = bounds.left + perpendicularSize
                }
            } else {
                bounds.top = layoutManager.paddingTop + row.getSpanBorder(
                    layoutParams.spanIndex
                )
                bounds.bottom = bounds.top + perpendicularSize
            }

            performLayout(view, bounds)

            if (shouldSkipSpaceOf(view)) {
                skipConsumption = true
            }
        }

        bounds.setEmpty()
        rowViews.forEach { view ->
            if (view != null) {
                onChildLayoutListener.onChildLaidOut(view)
            }
        }
        // Clear view references since we no longer need them
        rowViews.fill(null)
        return skipConsumption
    }

    private fun updateLayoutBounds(
        layoutRequest: LayoutRequest,
        row: GridRow,
        bounds: ViewBounds
    ) {
        val rowHeight = row.height
        if (layoutRequest.isVertical) {
            if (layoutRequest.isLayingOutStart()) {
                bounds.bottom = layoutRequest.checkpoint
                bounds.top = bounds.bottom - rowHeight
            } else {
                bounds.top = layoutRequest.checkpoint
                bounds.bottom = bounds.top + rowHeight
            }
        } else {
            if (layoutRequest.isLayingOutStart()) {
                bounds.right = layoutRequest.checkpoint
                bounds.left = bounds.right - rowHeight
            } else {
                bounds.left = layoutRequest.checkpoint
                bounds.right = bounds.left + rowHeight
            }
        }
    }

    /**
     * Views that don't have the height of the row need to be re-measured
     */
    private fun reMeasureChildren(row: GridRow, viewCount: Int) {
        val rowHeight = row.height
        for (i in 0 until viewCount) {
            val view = getRowViewAt(i)
            val layoutParams = layoutInfo.getLayoutParams(view)
            if (layoutInfo.getDecoratedSize(view) != rowHeight) {
                layoutInfo.getDecorationInsets(view, insets)
                val verticalInsets = (insets.top + insets.bottom
                        + layoutParams.topMargin + layoutParams.bottomMargin)
                val horizontalInsets = (insets.left + insets.right
                        + layoutParams.leftMargin + layoutParams.rightMargin)
                val totalSpaceInOther = row.getSpaceForSpanRange(
                    startSpan = layoutParams.spanIndex,
                    spanSize = layoutParams.spanSize,
                    isVerticalRTL = layoutInfo.isVertical() && layoutInfo.isRTL()
                )
                val wSpec: Int
                val hSpec: Int
                if (layoutInfo.isVertical()) {
                    wSpec = LayoutManager.getChildMeasureSpec(
                        totalSpaceInOther, MeasureSpec.EXACTLY,
                        horizontalInsets, layoutParams.width, false
                    )
                    hSpec = MeasureSpec.makeMeasureSpec(
                        rowHeight - verticalInsets,
                        MeasureSpec.EXACTLY
                    )
                } else {
                    wSpec = MeasureSpec.makeMeasureSpec(
                        rowHeight - horizontalInsets,
                        MeasureSpec.EXACTLY
                    )
                    hSpec = LayoutManager.getChildMeasureSpec(
                        totalSpaceInOther, MeasureSpec.EXACTLY,
                        verticalInsets, layoutParams.height, false
                    )
                }
                measureChildWithDecorationsAndMargins(
                    view, layoutParams, wSpec, hSpec, alreadyMeasured = true
                )
            }
        }
    }

    private fun measureChild(
        view: View,
        row: GridRow,
        layoutParams: DpadLayoutParams,
        secondarySpecMode: Int,
        alreadyMeasured: Boolean
    ) {
        val verticalInsets = (insets.top + insets.bottom
                + layoutParams.topMargin + layoutParams.bottomMargin)
        val horizontalInsets = (insets.left + insets.right
                + layoutParams.leftMargin + layoutParams.rightMargin)
        val availableSpaceInOther = row.getSpaceForSpanRange(
            startSpan = layoutParams.spanIndex,
            spanSize = layoutParams.spanSize,
            isVerticalRTL = layoutInfo.isVertical() && layoutInfo.isRTL()
        )
        val wSpec: Int
        val hSpec: Int
        if (layoutInfo.isVertical()) {
            wSpec = LayoutManager.getChildMeasureSpec(
                availableSpaceInOther, secondarySpecMode,
                horizontalInsets, layoutParams.width, false
            )
            hSpec = LayoutManager.getChildMeasureSpec(
                layoutInfo.getTotalSpace(), layoutManager.heightMode,
                verticalInsets, layoutParams.height, true
            )
        } else {
            hSpec = LayoutManager.getChildMeasureSpec(
                availableSpaceInOther, secondarySpecMode,
                verticalInsets, layoutParams.height, false
            )
            wSpec = LayoutManager.getChildMeasureSpec(
                layoutInfo.getTotalSpace(), layoutManager.widthMode,
                horizontalInsets, layoutParams.width, true
            )
        }
        measureChildWithDecorationsAndMargins(view, layoutParams, wSpec, hSpec, alreadyMeasured)
    }

    private fun measureChildWithDecorationsAndMargins(
        child: View,
        layoutParams: DpadLayoutParams,
        widthSpec: Int,
        heightSpec: Int,
        alreadyMeasured: Boolean
    ) {
        val measure: Boolean = if (alreadyMeasured) {
            shouldReMeasureChild(child, widthSpec, heightSpec, layoutParams)
        } else {
            shouldMeasureChild(child, widthSpec, heightSpec, layoutParams)
        }
        if (measure) {
            child.measure(widthSpec, heightSpec)
        }
    }

    private fun calculateSpansToFill(request: LayoutRequest): Int {
        return if (request.isLayingOutEnd()) {
            layoutRow.getAvailableAppendSpans()
        } else {
            layoutRow.getAvailablePrependSpans()
        }
    }

    private fun getViewsForRow(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State,
        spansToFill: Int
    ): Int {
        var remainingSpans = spansToFill
        var viewCount = 0
        while (isRowIncomplete(viewCount, layoutRequest, state, remainingSpans)) {
            val position = layoutRequest.currentPosition
            val spanSize = getSpanSize(recycler, state, position)
            if (spanSize > layoutRow.numberOfSpans) {
                throw IllegalArgumentException(
                    "Item at position $position requires $spanSize, " +
                            "but spanCount is ${layoutRow.numberOfSpans}"
                )
            }
            remainingSpans -= spanSize
            // If the current item doesn't fit into the current row, just exit
            if (remainingSpans < 0) {
                break
            }
            val view = layoutRequest.getNextView(recycler) ?: break
            rowViews[viewCount] = view
            viewCount++
        }
        return viewCount
    }

    private fun isRowIncomplete(
        viewCount: Int,
        layoutRequest: LayoutRequest,
        state: State,
        remainingSpans: Int
    ): Boolean {
        return viewCount < layoutRow.numberOfSpans
                && layoutRequest.hasMoreItems(state)
                && remainingSpans > 0
    }

    /**
     * Assigns a span index and span size to each view inside [rowViews].
     */
    private fun assignSpans(
        recycler: Recycler,
        state: State,
        count: Int,
        itemDirection: LayoutRequest.ItemDirection
    ) {
        var span = 0
        var start = 0
        var end = 0
        var increment = 0
        if (itemDirection == LayoutRequest.ItemDirection.TAIL) {
            start = 0
            end = count
            increment = 1
        } else {
            start = count - 1
            end = -1
            increment = -1
        }
        span = 0
        var i = start
        while (i != end) {
            val view = getRowViewAt(i)
            val params = layoutInfo.getLayoutParams(view)
            params.updateSpan(
                index = span,
                size = getSpanSize(recycler, state, layoutInfo.getLayoutPositionOf(view))
            )
            span += params.spanSize
            i += increment
        }
    }

    private fun getSpanIndex(recycler: Recycler, state: State, position: Int): Int {
        if (!state.isPreLayout) {
            return layoutInfo.getCachedSpanIndex(position)
        }
        val spanIndex = preLayoutSpanIndexCache.get(position, -1)
        if (spanIndex != -1) {
            return spanIndex
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(position)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            Log.w(DpadRecyclerView.TAG, "Cannot find span index for pre layout position: $position")
            return 1
        }
        return layoutInfo.getCachedSpanIndex(adapterPosition)
    }

    private fun getSpanSize(recycler: Recycler, state: State, position: Int): Int {
        if (!state.isPreLayout) {
            return layoutInfo.getSpanSize(position)
        }
        val spanSize = preLayoutSpanSizeCache.get(position, -1)
        if (spanSize != -1) {
            return spanSize
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(position)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            Log.w(DpadRecyclerView.TAG, "Cannot find span size for pre layout position: $position")
            return 1
        }
        return layoutInfo.getSpanSize(adapterPosition)
    }

    private fun clearPreLayoutSpanMappingCache() {
        preLayoutSpanSizeCache.clear()
        preLayoutSpanIndexCache.clear()
    }

    private fun cachePreLayoutSpanMapping() {
        val childCount = layoutManager.childCount
        for (i in 0 until childCount) {
            val view = layoutManager.getChildAt(i) ?: continue
            val layoutParams = layoutInfo.getLayoutParams(view)
            val viewPosition = layoutParams.viewLayoutPosition
            preLayoutSpanSizeCache.put(viewPosition, layoutParams.spanSize)
            preLayoutSpanIndexCache.put(viewPosition, layoutParams.spanIndex)
        }
    }

    private fun getRowViewAt(index: Int): View {
        return requireNotNull(rowViews[index])
    }

    private fun shouldReMeasureChild(
        child: View,
        widthSpec: Int,
        heightSpec: Int,
        lp: RecyclerView.LayoutParams
    ): Boolean {
        return !layoutManager.isMeasurementCacheEnabled
                || !isMeasurementUpToDate(child.measuredWidth, widthSpec, lp.width)
                || !isMeasurementUpToDate(child.measuredHeight, heightSpec, lp.height)
    }

    private fun shouldMeasureChild(
        child: View,
        widthSpec: Int,
        heightSpec: Int,
        lp: RecyclerView.LayoutParams
    ): Boolean {
        return child.isLayoutRequested
                || !layoutManager.isMeasurementCacheEnabled
                || !isMeasurementUpToDate(child.width, widthSpec, lp.width)
                || !isMeasurementUpToDate(child.height, heightSpec, lp.height)
    }

    private fun isMeasurementUpToDate(childSize: Int, spec: Int, dimension: Int): Boolean {
        val specMode = MeasureSpec.getMode(spec)
        val specSize = MeasureSpec.getSize(spec)
        if (dimension > 0 && childSize != dimension) {
            return false
        }
        when (specMode) {
            MeasureSpec.UNSPECIFIED -> return true
            MeasureSpec.AT_MOST -> return specSize >= childSize
            MeasureSpec.EXACTLY -> return specSize == childSize
        }
        return false
    }

}
