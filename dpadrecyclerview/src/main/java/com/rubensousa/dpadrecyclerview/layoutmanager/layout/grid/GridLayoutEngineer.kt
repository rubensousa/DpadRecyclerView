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
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutArchitect

internal class GridLayoutEngineer(
    layoutManager: LayoutManager,
    layoutInfo: LayoutInfo,
    layoutAlignment: LayoutAlignment,
    onChildLayoutListener: OnChildLayoutListener
) : StructureEngineer(layoutManager, layoutInfo, layoutAlignment, onChildLayoutListener) {

    companion object {
        const val TAG = "GridLayoutEngineer"
    }

    private val insets = Rect()
    private val architect = LinearLayoutArchitect(layoutInfo)
    private val preLayoutSpanSizeCache = SparseIntArray()
    private val preLayoutSpanIndexCache = SparseIntArray()
    private val rowViews = Array<View?>(layoutInfo.getSpanCount()) { null }
    private val layoutRow = GridRow(
        numberOfSpans = layoutInfo.getSpanCount(),
        width = layoutInfo.getSecondaryTotalSpace()
    )

    override fun getArchitect(): LayoutArchitect = architect

    override fun init(layoutRequest: LayoutRequest, state: State) {
        super.init(layoutRequest, state)
        layoutRow.setWidth(layoutInfo.getSecondaryTotalSpace())
    }

    override fun onPreLayout() {
        cachePreLayoutSpanMapping()
    }

    override fun onLayoutChildrenFinished() {
        clearPreLayoutSpanMappingCache();
    }

    override fun placePivot(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    ) {
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view)
        val head = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tail = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)
        val decoratedSize = tail - head
        val spanSize = layoutInfo.getSpanSize(position)

        layoutRow.init(
            newTop = head,
            viewSize = decoratedSize,
            spanIndex = layoutInfo.getStartColumnIndex(position),
            spanSize = spanSize
        )

        if (layoutRequest.isVertical) {
            bounds.top = head
            bounds.bottom = tail
            bounds.left = layoutRow.getStartOffset()
            bounds.right = layoutRow.getEndOffset()
        } else {
            bounds.left = head
            bounds.right = tail
            bounds.top = layoutRow.getStartOffset()
            bounds.bottom = layoutRow.getEndOffset()
        }
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
        val fillSpanCount = calculateSpansToFill(layoutRequest, recycler, state)
        val viewCount = getViewsForRow(layoutRequest, recycler, state, fillSpanCount)
        assignSpans(recycler, state, viewCount, layoutRequest.itemDirection)

        val rowHeight = fillRow(viewCount, layoutRequest)
        layoutResult.consumedSpace = rowHeight
        reMeasureChildren(viewCount, rowHeight)
        layoutRow(viewCount, rowHeight, layoutRequest, layoutResult)
    }

    /**
     * Add views from [rowViews] to the RecyclerView and keeps track of the row size
     */
    private fun fillRow(
        viewCount: Int,
        layoutRequest: LayoutRequest
    ): Int {
        val secondarySpecMode = layoutInfo.orientationHelper.modeInOther
        repeat(viewCount) { index ->
            val view = rowViews[index]!!
            addView(view, layoutRequest)
            val layoutParams = layoutInfo.getLayoutParams(view)

            layoutManager.calculateItemDecorationsForChild(view, insets)

            measureChild(view, layoutParams, secondarySpecMode, alreadyMeasured = false)

            val decoratedSize = layoutInfo.getDecoratedSize(view)
            if (layoutRequest.isLayingOutEnd()) {
                layoutRow.append(decoratedSize, layoutParams.spanSize)
            } else {
                layoutRow.prepend(decoratedSize, layoutParams.spanSize)
            }
        }
        return layoutRow.height
    }

    private fun layoutRow(
        viewCount: Int,
        rowHeight: Int,
        layoutRequest: LayoutRequest,
        layoutResult: LayoutResult
    ) {
        updateLayoutBounds(layoutRequest, rowHeight, viewBounds)

        for (i in 0 until viewCount) {
            val view = getRowViewAt(i)
            val layoutParams = layoutInfo.getLayoutParams(view)
            val perpendicularSize = layoutInfo.getPerpendicularDecoratedSize(view)
            if (layoutRequest.isVertical) {
                if (layoutInfo.isRTL()) {
                    viewBounds.right = layoutManager.paddingLeft + layoutRow.getSpanBorder(
                        layoutRow.numberOfSpans - layoutParams.spanIndex
                    )
                    viewBounds.left = viewBounds.right - perpendicularSize
                } else {
                    viewBounds.left = layoutManager.paddingLeft + layoutRow.getSpanBorder(
                        layoutParams.spanIndex
                    )
                    viewBounds.right = viewBounds.left + perpendicularSize
                }
            } else {
                viewBounds.top = layoutManager.paddingTop + layoutRow.getSpanBorder(
                    layoutParams.spanIndex
                )
                viewBounds.bottom = viewBounds.top + perpendicularSize
            }

            performLayout(view, viewBounds)

            if (shouldSkipSpaceOf(view)) {
                layoutResult.skipConsumption = true
            }

            onChildLayoutListener.onChildLaidOut(view)
        }

        // Clear view references since we no longer need them
        rowViews.fill(null)
    }

    private fun updateLayoutBounds(
        layoutRequest: LayoutRequest,
        rowHeight: Int,
        viewBounds: ViewBounds
    ) {
        if (layoutRequest.isVertical) {
            if (layoutRequest.isLayingOutStart()) {
                viewBounds.bottom = layoutRequest.checkpoint
                viewBounds.top = viewBounds.bottom - rowHeight
            } else {
                viewBounds.top = layoutRequest.checkpoint
                viewBounds.bottom = viewBounds.top + rowHeight
            }
        } else {
            if (layoutRequest.isLayingOutStart()) {
                viewBounds.right = layoutRequest.checkpoint
                viewBounds.left = viewBounds.right - rowHeight
            } else {
                viewBounds.left = layoutRequest.checkpoint
                viewBounds.right = viewBounds.left + rowHeight
            }
        }
    }

    /**
     * Views that don't have the height of the row need to be re-measured
     */
    private fun reMeasureChildren(viewCount: Int, rowHeight: Int) {
        for (i in 0 until viewCount) {
            val view = getRowViewAt(i)
            val layoutParams = layoutInfo.getLayoutParams(view)
            if (layoutRow.getHeightAt(layoutParams.spanIndex) != rowHeight) {
                layoutInfo.getDecorationInsets(view, insets)
                val verticalInsets = (insets.top + insets.bottom
                        + layoutParams.topMargin + layoutParams.bottomMargin)
                val horizontalInsets = (insets.left + insets.right
                        + layoutParams.leftMargin + layoutParams.rightMargin)
                val totalSpaceInOther = layoutRow.getSpaceForSpanRange(
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
        layoutParams: DpadLayoutParams,
        secondarySpecMode: Int,
        alreadyMeasured: Boolean
    ) {
        val verticalInsets = (insets.top + insets.bottom
                + layoutParams.topMargin + layoutParams.bottomMargin)
        val horizontalInsets = (insets.left + insets.right
                + layoutParams.leftMargin + layoutParams.rightMargin)
        val availableSpaceInOther = layoutRow.getSpaceForSpanRange(
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

    private fun calculateSpansToFill(
        request: LayoutRequest,
        recycler: Recycler,
        state: State
    ): Int {
        return if (request.itemDirection == LayoutRequest.ItemDirection.TAIL) {
            layoutRow.numberOfSpans
        } else {
            val spanIndex = getSpanIndex(recycler, state, request.currentPosition)
            val spanSize = getSpanSize(recycler, state, request.currentPosition)
            spanIndex + spanSize
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
            params.setSpanSize(getSpanSize(recycler, state, layoutInfo.getLayoutPositionOf(view)))
            params.setSpanIndex(span)
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
