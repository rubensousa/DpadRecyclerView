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
import android.view.View
import android.view.View.MeasureSpec
import androidx.collection.SparseArrayCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutResult
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.OnChildLayoutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.PreLayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.StructureEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutArchitect
import kotlin.math.abs
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
    private val rowViews = Array<View?>(layoutInfo.getSpanCount()) { null }
    private var pivotView: View? = null
    private val layoutRow = GridRow(
        numberOfSpans = layoutInfo.getSpanCount(),
        width = layoutInfo.getSecondaryTotalSpace()
    )
    private val gridState = GridState(layoutManager)
    private val pivotRow = GridRow(layoutRow)
    private val architect = LinearLayoutArchitect(layoutInfo)
    private var pivotLayoutPosition = RecyclerView.NO_POSITION

    override fun onLayoutStarted(state: State) {
        super.onLayoutStarted(state)
        layoutRow.setWidth(layoutInfo.getSecondaryTotalSpace())
        pivotRow.setWidth(layoutRow.getWidth())
    }

    override fun updateLayoutRequestForScroll(
        layoutRequest: LayoutRequest,
        state: State,
        scrollOffset: Int
    ) {
        val scrollDistance = abs(scrollOffset)

        if (scrollOffset < 0) {
            val view = layoutInfo.getChildClosestToStart() ?: return
            layoutRequest.prepend(
                layoutInfo.getLayoutPositionOf(view),
                layoutRequest.defaultItemDirection.opposite()
            ) {
                setCheckpoint(layoutInfo.getDecoratedStart(view))
                architect.updateExtraLayoutSpace(layoutRequest, state)
                setAvailableScrollSpace(max(0, layoutInfo.getStartAfterPadding() - checkpoint))
                setFillSpace(scrollDistance + extraLayoutSpaceStart - availableScrollSpace)
            }
        } else {
            val view = layoutInfo.getChildClosestToEnd() ?: return
            layoutRequest.append(layoutInfo.getLayoutPositionOf(view)) {
                setCheckpoint(layoutInfo.getDecoratedEnd(view))
                architect.updateExtraLayoutSpace(layoutRequest, state)
                setAvailableScrollSpace(max(0, checkpoint - layoutInfo.getEndAfterPadding()))
                setFillSpace(scrollDistance + extraLayoutSpaceEnd - availableScrollSpace)
            }
        }
    }

    override fun onChildrenOffset(offset: Int) {
        super.onChildrenOffset(offset)
        layoutRow.offsetBy(offset)
        pivotRow.offsetBy(offset)
    }

    override fun initLayout(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ): View {
        pivotLayoutPosition = pivotPosition
        layoutRow.reset(layoutAlignment.getParentKeyline())
        pivotRow.reset(layoutAlignment.getParentKeyline())
        val pivotView = layoutPivotRow(pivotPosition, layoutRequest, recycler, state)
        pivotRow.copy(layoutRow)
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
        prepend(layoutRequest, pivotRow.getFirstPosition()) {
            setCheckpoint(pivotRow.startOffset)
            setFillSpace(checkpoint - layoutInfo.getStartAfterPadding())
        }
        fill(layoutRequest, recycler, state)
    }

    private fun layoutFromPivotToEnd(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        append(layoutRequest, pivotRow.getLastPosition()) {
            setCheckpoint(pivotRow.endOffset)
            setFillSpace(layoutInfo.getEndAfterPadding() - checkpoint)
        }
        fill(layoutRequest, recycler, state)
    }

    override fun preLayout(
        preLayoutRequest: PreLayoutRequest,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        gridState.save()
        val firstView = preLayoutRequest.firstView
        if (firstView != null) {
            prepend(layoutRequest, preLayoutRequest.firstPosition) {
                setCheckpoint(layoutInfo.getDecoratedStart(firstView))
                setFillSpace(preLayoutRequest.extraLayoutSpace)
            }
            fill(layoutRequest, recycler, state)
        }
        val lastView = preLayoutRequest.lastView
        if (lastView != null) {
            append(layoutRequest, preLayoutRequest.lastPosition) {
                setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
                setFillSpace(preLayoutRequest.extraLayoutSpace)
            }
            fill(layoutRequest, recycler, state)
        }
    }

    override fun predictiveLayout(
        firstView: View,
        lastView: View,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        val scrapList = recycler.scrapList
        if (scrapList.isEmpty()) {
            return
        }
        val disappearingViews = SparseArrayCompat<View>()
        for (i in 0 until scrapList.size) {
            val scrap = scrapList[i]
            val viewPosition = layoutInfo.getAdapterPositionOf(scrap.itemView)
            if (viewPosition != RecyclerView.NO_POSITION) {
                disappearingViews.put(viewPosition, scrap.itemView)
            }
        }
        if (disappearingViews.isEmpty()) {
            return
        }
        val firstViewPosition = layoutInfo.getLayoutPositionOf(firstView)
        val rowOffsets = calculateRowOffsets(layoutRequest, recycler, state)
        layoutDisappearingViews(layoutRequest, firstViewPosition, disappearingViews, rowOffsets)
    }

    private fun calculateRowOffsets(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State,
    ): SparseArrayCompat<Int> {
        val offsets = SparseArrayCompat<Int>()
        for (i in 0 until layoutManager.childCount) {
            val view = layoutManager.getChildAt(i) ?: continue
            val layoutParams = view.layoutParams as DpadLayoutParams
            val rowIndex = getSpanGroupIndex(recycler, state, layoutParams.viewLayoutPosition)
            val offset = if (layoutRequest.isVertical) {
                layoutManager.getDecoratedTop(view) - layoutParams.topMargin
            } else {
                layoutManager.getDecoratedLeft(view) - layoutParams.leftMargin
            }
            offsets.put(rowIndex, offset)
        }
        return offsets
    }

    private fun layoutDisappearingViews(
        layoutRequest: LayoutRequest,
        firstViewPosition: Int,
        views: SparseArrayCompat<View>,
        rowOffsets: SparseArrayCompat<Int>,
    ) {
        val secondarySpecMode = layoutInfo.orientationHelper.modeInOther
        var firstRowIndex = rowOffsets.keyAt(0)
        var lastRowIndex = rowOffsets.keyAt(rowOffsets.size() - 1)
        for (i in views.size() - 1 downTo 0) {
            val view = views.valueAt(i)
            val position = views.keyAt(i)
            val layoutParams = view.layoutParams as DpadLayoutParams
            val rowIndex = layoutParams.spanGroupIndex
            if (position < firstViewPosition) {
                layoutManager.addDisappearingView(view, 0)
            } else {
                layoutManager.addDisappearingView(view)
            }
            layoutManager.calculateItemDecorationsForChild(view, insets)
            measureChild(view, layoutRow, layoutParams, secondarySpecMode, alreadyMeasured = false)
            val decoratedSize = layoutInfo.getDecoratedSize(view)

            // Find the checkpoint for placing this view
            var checkpoint = 0
            var prepending = false

            if (rowIndex < firstRowIndex) {
                checkpoint = rowOffsets[firstRowIndex]!!
                firstRowIndex = rowIndex
                prepending = true
                rowOffsets.put(rowIndex, checkpoint - decoratedSize)
            } else if (rowIndex > lastRowIndex) {
                checkpoint = rowOffsets[lastRowIndex]!!
                lastRowIndex = rowIndex
                rowOffsets.put(rowIndex, checkpoint + decoratedSize)
            } else {
                checkpoint = rowOffsets[rowIndex]!!
            }

            updateLayoutBounds(
                isVertical = layoutRequest.isVertical,
                isPrepending = prepending,
                checkpoint = checkpoint,
                rowHeight = decoratedSize,
                bounds = viewBounds
            )
            updateViewBounds(view, layoutRequest.isVertical, layoutParams, viewBounds)
            performLayout(view, viewBounds)
        }
    }


    override fun layoutExtraSpace(
        layoutRequest: LayoutRequest,
        preLayoutRequest: PreLayoutRequest,
        recycler: Recycler,
        state: State
    ) {
        val firstView = layoutInfo.getChildClosestToStart() ?: return
        prepend(layoutRequest, layoutInfo.getLayoutPositionOf(firstView)) {
            architect.updateExtraLayoutSpace(layoutRequest, state)
            setCheckpoint(layoutInfo.getDecoratedStart(firstView))
            setFillSpace(extraLayoutSpaceStart + preLayoutRequest.extraLayoutSpace)
        }
        fill(layoutRequest, recycler, state)

        val lastView = layoutInfo.getChildClosestToEnd() ?: return
        append(layoutRequest, layoutInfo.getLayoutPositionOf(lastView)) {
            architect.updateExtraLayoutSpace(layoutRequest, state)
            setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
            setFillSpace(extraLayoutSpaceEnd)
        }
        fill(layoutRequest, recycler, state)
    }

    override fun onLayoutChildrenFinished() {
        gridState.clear()
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
        layoutRow.reset(keyline = layoutRequest.checkpoint)
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
        updateLayoutBounds(
            isVertical = layoutRequest.isVertical,
            isPrepending = layoutRequest.isLayingOutStart(),
            checkpoint = layoutRequest.checkpoint,
            rowHeight = row.height,
            bounds = bounds
        )

        for (i in 0 until viewCount) {
            val view = getRowViewAt(i)
            val layoutParams = layoutInfo.getLayoutParams(view)
            if (layoutParams.viewLayoutPosition == pivotLayoutPosition) {
                pivotView = view
            }
            updateViewBounds(view, layoutRequest.isVertical, layoutParams, bounds)
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
        isVertical: Boolean,
        isPrepending: Boolean,
        checkpoint: Int,
        rowHeight: Int,
        bounds: ViewBounds
    ) {
        if (isVertical) {
            if (isPrepending) {
                bounds.bottom = checkpoint
                bounds.top = bounds.bottom - rowHeight
            } else {
                bounds.top = checkpoint
                bounds.bottom = bounds.top + rowHeight
            }
        } else {
            if (isPrepending) {
                bounds.right = checkpoint
                bounds.left = bounds.right - rowHeight
            } else {
                bounds.left = checkpoint
                bounds.right = bounds.left + rowHeight
            }
        }
    }

    private fun updateViewBounds(
        view: View,
        isVertical: Boolean,
        layoutParams: DpadLayoutParams,
        bounds: ViewBounds
    ) {
        val perpendicularSize = layoutInfo.getPerpendicularDecoratedSize(view)
        if (isVertical) {
            if (layoutInfo.isRTL()) {
                bounds.right = layoutManager.paddingLeft + layoutRow.getSpanBorder(
                    layoutRow.numberOfSpans - layoutParams.spanIndex
                )
                bounds.left = bounds.right - perpendicularSize
            } else {
                bounds.left = layoutManager.paddingLeft + layoutRow.getSpanBorder(
                    layoutParams.spanIndex
                )
                bounds.right = bounds.left + perpendicularSize
            }
        } else {
            bounds.top = layoutManager.paddingTop + layoutRow.getSpanBorder(
                layoutParams.spanIndex
            )
            bounds.bottom = bounds.top + perpendicularSize
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
        val spanSpace = row.getSpaceForSpanRange(
            startSpan = layoutParams.spanIndex,
            spanSize = layoutParams.spanSize,
            isVerticalRTL = layoutInfo.isVertical() && layoutInfo.isRTL()
        )
        val wSpec: Int
        val hSpec: Int
        if (layoutInfo.isVertical()) {
            wSpec = LayoutManager.getChildMeasureSpec(
                spanSpace, secondarySpecMode,
                horizontalInsets, layoutParams.width, false
            )
            hSpec = LayoutManager.getChildMeasureSpec(
                layoutInfo.getTotalSpace(), layoutManager.heightMode,
                verticalInsets, layoutParams.height, true
            )
        } else {
            hSpec = LayoutManager.getChildMeasureSpec(
                spanSpace, secondarySpecMode,
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
            val layoutPosition = layoutInfo.getLayoutPositionOf(view)
            params.updateSpan(
                index = span,
                groupIndex = getSpanGroupIndex(recycler, state, layoutPosition),
                size = getSpanSize(recycler, state, layoutPosition)
            )
            span += params.spanSize
            i += increment
        }
    }

    private fun getSpanIndex(recycler: Recycler, state: State, position: Int): Int {
        if (!state.isPreLayout) {
            return layoutInfo.getCachedSpanIndex(position)
        }
        val spanIndex = gridState.getSpanIndex(position)
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
        val spanSize = gridState.getSpanSize(position)
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

    private fun getSpanGroupIndex(recycler: Recycler, state: State, position: Int): Int {
        if (!state.isPreLayout) {
            return layoutInfo.getRowIndex(position)
        }
        val groupIndex = gridState.getSpanGroupIndex(position)
        if (groupIndex != -1) {
            return groupIndex
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(position)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            Log.w(DpadRecyclerView.TAG, "Cannot find span size for pre layout position: $position")
            return 1
        }
        return layoutInfo.getRowIndex(adapterPosition)
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

    private fun prepend(
        request: LayoutRequest,
        fromPosition: Int,
        block: LayoutRequest.() -> Unit
    ) {
        request.prepend(fromPosition, request.defaultItemDirection.opposite()) {
            setRecyclingEnabled(false)
            block(this)
        }
    }

    private fun append(request: LayoutRequest, fromPosition: Int, block: LayoutRequest.() -> Unit) {
        request.append(fromPosition) {
            setRecyclingEnabled(false)
            block(this)
        }
    }

}
