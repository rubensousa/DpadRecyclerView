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
import androidx.collection.forEach
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
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
    private val layoutRow = GridRow(
        numberOfSpans = layoutInfo.getSpanCount(),
        width = layoutInfo.getSecondaryTotalSpace()
    )
    private val gridState = GridState(layoutManager)
    private val pivotRow = GridRow(layoutRow)
    private var pivotLayoutPosition = RecyclerView.NO_POSITION

    override fun onLayoutStarted(state: State) {
        super.onLayoutStarted(state)
        gridState.save()
        layoutRow.setWidth(layoutInfo.getSecondaryTotalSpace())
        pivotRow.setWidth(layoutRow.getWidth())
    }

    override fun onChildrenOffset(offset: Int) {
        super.onChildrenOffset(offset)
        layoutRow.offsetBy(offset)
        pivotRow.offsetBy(offset)
    }

    override fun initLayout(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: Recycler,
        state: State
    ): View {
        pivotLayoutPosition = pivotPosition
        layoutRow.reset(layoutAlignment.getParentKeyline())
        pivotRow.reset(layoutAlignment.getParentKeyline())
        val pivotView = layoutPivotRow(pivotPosition, layoutRequest, viewProvider, recycler, state)
        pivotRow.copy(layoutRow)
        // Layout from pivot to start
        layoutRequest.prepend(pivotRow.getFirstPosition()) {
            setCheckpoint(pivotRow.startOffset)
            setFillSpace(checkpoint - layoutInfo.getStartAfterPadding())
        }
        fill(layoutRequest, viewProvider, recycler, state)

        // Layout from pivot to end
        layoutRequest.append(pivotRow.getLastPosition()) {
            setCheckpoint(pivotRow.endOffset)
            setFillSpace(layoutInfo.getEndAfterPadding() - checkpoint)
        }
        fill(layoutRequest, viewProvider, recycler, state)
        pivotLayoutPosition = RecyclerView.NO_POSITION
        return pivotView
    }

    private fun layoutPivotRow(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: Recycler,
        state: State
    ): View {
        val pivotSpanIndex = getSpanIndex(recycler, state, pivotPosition)
        val firstSpanPosition = max(0, pivotPosition - pivotSpanIndex)
        if (!layoutRequest.reverseLayout) {
            layoutRequest.append(firstSpanPosition) {
                setCurrentPosition(firstSpanPosition)
                setCheckpoint(layoutAlignment.getParentKeyline())
                setFillSpace(1)
            }
        } else {
            layoutRequest.prepend(firstSpanPosition) {
                setCurrentPosition(firstSpanPosition)
                setCheckpoint(layoutAlignment.getParentKeyline())
                setFillSpace(1)
            }
        }
        fill(layoutRequest, viewProvider, recycler, state)
        return requireNotNull(layoutManager.findViewByPosition(pivotPosition))
    }

    override fun layoutDisappearingViews(
        firstView: View,
        lastView: View,
        layoutRequest: LayoutRequest,
        scrapViewProvider: ScrapViewProvider,
        recycler: Recycler,
        state: State
    ) {
        val firstViewPosition = layoutInfo.getLayoutPositionOf(firstView)
        val startSpanGroups = SparseArrayCompat<Int>()
        val endSpanGroups = SparseArrayCompat<Int>()

        scrapViewProvider.getScrap().forEach { position, viewHolder ->
            val spanGroupIndex = layoutInfo.getLayoutParams(viewHolder.itemView).spanGroupIndex
            if (spanGroupIndex != DpadLayoutParams.INVALID_SPAN_ID) {
                val direction = if (position < firstViewPosition != layoutRequest.reverseLayout) {
                    LayoutDirection.START
                } else {
                    LayoutDirection.END
                }
                val viewSize = layoutInfo.getDecoratedSize(viewHolder.itemView)
                if (direction == LayoutDirection.START) {
                    startSpanGroups.put(spanGroupIndex, viewSize)
                } else {
                    endSpanGroups.put(spanGroupIndex, viewSize)
                }
            }
        }

        var scrapExtraStart = 0
        var scrapExtraEnd = 0

        startSpanGroups.forEach { _, value ->
            scrapExtraStart += value
        }

        endSpanGroups.forEach { _, value ->
            scrapExtraEnd += value
        }

        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "Scrap extra: $scrapExtraStart, $scrapExtraEnd")
        }

        if (scrapExtraStart > 0) {
            layoutRequest.prepend(layoutInfo.getLayoutPositionOf(firstView)) {
                setCheckpoint(layoutInfo.getDecoratedStart(firstView))
                setFillSpace(scrapExtraStart)
                scrapViewProvider.setNextLayoutPosition(this)
            }
            fill(layoutRequest, scrapViewProvider, recycler, state)
        }

        if (scrapExtraEnd > 0) {
            layoutRequest.append(layoutInfo.getLayoutPositionOf(lastView)) {
                setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
                setFillSpace(scrapExtraEnd)
                scrapViewProvider.setNextLayoutPosition(this)
            }
            fill(layoutRequest, scrapViewProvider, recycler, state)
        }
    }

    override fun onLayoutFinished() {
        gridState.clear()
    }

    override fun onLayoutCleared() {
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
        viewProvider: ViewProvider,
        recycler: Recycler,
        state: State,
        layoutResult: LayoutResult
    ) {
        layoutRow.reset(keyline = layoutRequest.checkpoint)
        val fillSpanCount = calculateSpansToFill(layoutRequest)
        val viewCount = getViewsForRow(layoutRequest, viewProvider, recycler, state, fillSpanCount)
        assignSpans(recycler, state, viewCount, appending = layoutRequest.isAppending())

        val rowHeight = fillRow(viewCount, layoutRow, layoutRequest)
        layoutResult.consumedSpace = rowHeight
        reMeasureChildren(layoutRow, viewCount, layoutRequest)

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
            if (layoutRequest.isAppending() && !row.fitsEnd(layoutParams.spanSize)) {
                return row.height
            }
            if (layoutRequest.isPrepending() && !row.fitsStart(layoutParams.spanSize)) {
                return row.height
            }
            addView(view, layoutRequest)
            layoutManager.calculateItemDecorationsForChild(view, insets)

            measureChild(
                view, row, layoutParams, secondarySpecMode, alreadyMeasured = false,
                layoutRequest
            )

            onChildLayoutListener.onChildCreated(view)

            val decoratedSize = layoutInfo.getDecoratedSize(view)
            if (layoutRequest.isAppending()) {
                row.append(
                    decoratedSize,
                    layoutParams.viewLayoutPosition,
                    layoutParams.spanSize
                )
            } else {
                row.prepend(
                    decoratedSize,
                    layoutParams.viewLayoutPosition,
                    layoutParams.spanSize
                )
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
            isPrepending = layoutRequest.isPrepending(),
            checkpoint = layoutRequest.checkpoint,
            rowHeight = row.height,
            bounds = bounds
        )

        for (i in 0 until viewCount) {
            val view = getRowViewAt(i)
            val layoutParams = layoutInfo.getLayoutParams(view)
            updateViewBounds(view, layoutRequest, layoutParams, bounds)
            performLayout(view, bounds)

            if (DpadRecyclerView.DEBUG) {
                Log.i(TAG, "Laid out view ${layoutInfo.getLayoutPositionOf(view)} at: $viewBounds")
            }

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
        layoutRequest: LayoutRequest,
        layoutParams: DpadLayoutParams,
        bounds: ViewBounds
    ) {
        val perpendicularSize = layoutInfo.getPerpendicularDecoratedSize(view)
        if (layoutRequest.isVertical) {
            bounds.left = layoutManager.paddingLeft + layoutRow.getSpanBorder(
                layoutParams.spanIndex
            )
            bounds.right = bounds.left + perpendicularSize
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
    private fun reMeasureChildren(row: GridRow, viewCount: Int, layoutRequest: LayoutRequest) {
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
                    reverseLayout = layoutRequest.reverseLayout
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
        alreadyMeasured: Boolean,
        layoutRequest: LayoutRequest
    ) {
        val verticalInsets = (insets.top + insets.bottom
                + layoutParams.topMargin + layoutParams.bottomMargin)
        val horizontalInsets = (insets.left + insets.right
                + layoutParams.leftMargin + layoutParams.rightMargin)
        val spanSpace = row.getSpaceForSpanRange(
            startSpan = layoutParams.spanIndex,
            spanSize = layoutParams.spanSize,
            reverseLayout = layoutRequest.reverseLayout
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
        return if (request.isAppending()) {
            layoutRow.getAvailableAppendSpans()
        } else {
            layoutRow.getAvailablePrependSpans()
        }
    }

    private fun getViewsForRow(
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: Recycler,
        state: State,
        spansToFill: Int
    ): Int {
        var remainingSpans = spansToFill
        var viewCount = 0
        while (isRowIncomplete(viewCount, viewProvider, layoutRequest, state, remainingSpans)) {
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
            val view = viewProvider.next(layoutRequest, state) ?: break
            rowViews[viewCount] = view
            viewCount++
        }
        return viewCount
    }

    private fun isRowIncomplete(
        viewCount: Int,
        viewProvider: ViewProvider,
        layoutRequest: LayoutRequest,
        state: State,
        remainingSpans: Int
    ): Boolean {
        return viewCount < layoutRow.numberOfSpans
                && viewProvider.hasNext(layoutRequest, state)
                && remainingSpans > 0
    }

    /**
     * Assigns a span index and span size to each view inside [rowViews].
     */
    private fun assignSpans(
        recycler: Recycler,
        state: State,
        count: Int,
        appending: Boolean
    ) {
        var start = 0
        var end = 0
        var increment = 0
        if (appending) {
            start = 0
            end = count
            increment = 1
        } else {
            start = count - 1
            end = -1
            increment = -1
        }
        var span = 0
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
            Log.w(
                DpadRecyclerView.TAG,
                "Cannot find span index for pre layout position: $position"
            )
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
            Log.w(
                DpadRecyclerView.TAG,
                "Cannot find span size for pre layout position: $position"
            )
            return 1
        }
        return layoutInfo.getSpanSize(adapterPosition)
    }

    private fun getSpanGroupIndex(recycler: Recycler, state: State, position: Int): Int {
        if (!state.isPreLayout) {
            return layoutInfo.getSpanGroupIndex(position)
        }
        val groupIndex = gridState.getSpanGroupIndex(position)
        if (groupIndex != -1) {
            return groupIndex
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(position)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            Log.w(
                DpadRecyclerView.TAG,
                "Cannot find span size for pre layout position: $position"
            )
            return 1
        }
        return layoutInfo.getSpanGroupIndex(adapterPosition)
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
