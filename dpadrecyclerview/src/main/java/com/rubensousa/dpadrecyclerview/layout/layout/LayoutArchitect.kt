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

package com.rubensousa.dpadrecyclerview.layout.layout

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.layout.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layout.ViewSelector
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Adapted internal logic of LinearLayoutManager and GridLayoutManager with custom pivot alignment
 */
internal class LayoutArchitect(
    private val layoutManager: LayoutManager,
    private val configuration: LayoutConfiguration,
    private val viewSelector: ViewSelector,
    private val layoutInfo: LayoutInfo
) {

    companion object {
        const val TAG = "LayoutArchitect"
        private const val DEBUG = true
    }

    private val pivotInfo = PivotInfo()
    private val layoutState = LayoutState()
    private val layoutResult = LayoutResult()
    private val extraLayoutSpace = IntArray(2)
    private val childRecycler = ChildRecycler(layoutManager, layoutInfo, configuration)
    private val rowArchitect = RowArchitect(layoutManager, layoutInfo, configuration)
    private val gridArchitect = GridArchitect(layoutManager, layoutInfo, configuration)
    private var dpadRecyclerView: RecyclerView? = null

    fun setRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
    }

    fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        // If we don't have any items, recycle them all
        if (state.itemCount == 0) {
            layoutManager.removeAndRecycleAllViews(recycler)
            return
        }

        // Disable recycling at this stage, since we need to add as many views as we need
        layoutState.recycle = false
        layoutState.reverseLayout = shouldReverseLayout()
        layoutState.spanCount = configuration.spanCount

        layoutState.updateDirectionFromLastScroll()

        updateExtraLayoutSpace(state)
        var extraForStart = getExtraLayoutForStart()
        var extraForEnd = getExtraLayoutForEnd()

        // Detach all existing views
        layoutManager.detachAndScrapAttachedViews(recycler)

        layoutState.isInfinite = layoutInfo.isWrapContent()
        layoutState.isPreLayout = state.isPreLayout
        // This isn't needed since recycling doesn't happen at this stage
        layoutState.extraLayoutSpace = 0

        var startOffset = 0
        var endOffset = 0

        val pivotPosition = viewSelector.position
        rowArchitect.layoutPivot(pivotPosition, recycler, pivotInfo)
        log(pivotInfo.toString())
        log(layoutState.toString())

        if (layoutState.reverseLayout) {
            updateLayoutStateToFillStart(pivotInfo)
            layoutState.extraFillSpace = extraForStart
            fill(recycler, state)
            startOffset = layoutState.offset
            if (layoutState.available > 0) {
                extraForEnd += layoutState.available
            }

            // fill towards end
            updateLayoutStateToFillEnd(pivotInfo)
            layoutState.extraFillSpace = extraForEnd
            fill(recycler, state)
            endOffset = layoutState.offset
        } else {
            // fill towards end
            updateLayoutStateToFillEnd(pivotInfo)
            log("Before filling towards end: $layoutState")

            layoutState.extraFillSpace = extraForEnd
            fill(recycler, state)
            log("After filling towards end: $layoutState")
            endOffset = layoutState.offset
            if (layoutState.available > 0) {
                extraForStart += layoutState.available
            }

            // fill towards start
            updateLayoutStateToFillStart(pivotInfo)
            log("Before filling towards start: $layoutState")

            layoutState.extraFillSpace = extraForStart
            fill(recycler, state)
            log("After filling towards start: $layoutState")
            startOffset = layoutState.offset
        }

        layoutForPredictiveAnimations(recycler, state, startOffset, endOffset)

        if (!state.isPreLayout) {
            layoutInfo.update()
        }

    }

    // TODO
    fun onLayoutCompleted(state: RecyclerView.State?) {
        layoutResult.reset()
    }

    /**
     * Keeps adding views until the remaining space is exhausted
     */
    private fun fill(recycler: Recycler, state: RecyclerView.State): Int {
        val start = layoutState.available
        if (layoutState.availableScrollSpace != LayoutState.SCROLL_SPACE_NONE) {
            if (layoutState.available < 0) {
                layoutState.availableScrollSpace += layoutState.available
            }
            childRecycler.recycleByLayoutState(recycler, layoutState)
        }
        var remainingSpace = layoutState.available + layoutState.extraFillSpace
        while ((layoutState.isInfinite || remainingSpace > 0) && layoutState.hasMoreItems(state)) {
            layoutResult.reset()
            layoutChunk(recycler, state)

            if (layoutResult.finished) {
                break
            }
            layoutState.offset += layoutResult.consumed * layoutState.direction.value
            /**
             * Consume the available space if:
             * * layoutChunk did not request to be ignored
             * * OR we are laying out scrap children
             * * OR we are not doing pre-layout
             */
            if (!layoutResult.ignoreConsumed
                || layoutState.scrappedViews != null
                || !state.isPreLayout
            ) {
                layoutState.available -= layoutResult.consumed
                // we keep a separate remaining space because available is important for recycling
                remainingSpace -= layoutResult.consumed
            }
            if (layoutState.availableScrollSpace != LayoutState.SCROLL_SPACE_NONE) {
                layoutState.availableScrollSpace += layoutResult.consumed
                if (layoutState.available < 0) {
                    layoutState.availableScrollSpace += layoutState.available
                }
                childRecycler.recycleByLayoutState(recycler, layoutState)
            }
        }
        return start - layoutState.available
    }

    private fun layoutChunk(recycler: Recycler, recyclerViewState: RecyclerView.State) {
        if (configuration.spanCount == 1) {
            rowArchitect.layoutChunk(recycler, layoutState, layoutResult)
        } else {
            gridArchitect.layoutChunk(recycler, recyclerViewState, layoutState, layoutResult)
        }
    }

    fun scroll(offset: Int, recycler: Recycler, state: RecyclerView.State): Int {
        layoutState.recycle = true
        val direction = if (offset > 0) {
            LayoutState.LayoutDirection.END
        } else {
            LayoutState.LayoutDirection.START
        }
        val absoluteOffset = abs(offset)
        var consumed = -1

        if (consumed < 0) {
            // Reached an edge of the list, just return
            return 0
        }
        val scrolled = if (absoluteOffset > consumed) {
            if (direction == LayoutState.LayoutDirection.END) {
                consumed
            } else {
                -consumed
            }
        } else {
            offset
        }
        layoutInfo.orientationHelper.offsetChildren(-scrolled)
        return scrolled
    }

    // TODO
    fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State?,
        layoutPrefetchRegistry: LayoutManager.LayoutPrefetchRegistry
    ) {

    }

    fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: LayoutManager.LayoutPrefetchRegistry
    ) {
        val prefetchCount: Int = configuration.initialPrefetchItemCount
        if (adapterItemCount != 0 && prefetchCount != 0) {
            // Prefetch items centered around the selected position
            val initialPosition = max(
                0, min(
                    viewSelector.position - (prefetchCount - 1) / 2,
                    adapterItemCount - prefetchCount
                )
            )
            var i = initialPosition
            while (i < adapterItemCount && i < initialPosition + prefetchCount) {
                layoutPrefetchRegistry.addPosition(i, 0)
                i++
            }
        }
    }

    /**
     * If necessary, layouts new items for predictive animations
     */
    private fun layoutForPredictiveAnimations(
        recycler: Recycler,
        state: RecyclerView.State,
        startOffset: Int,
        endOffset: Int
    ) {
        // If there are scrap children that we did not layout, we need to find where they did go
        // and layout them accordingly so that animations can work as expected.
        // This case may happen if new views are added or an existing view expands and pushes
        // another view out of bounds.
        if (!state.willRunPredictiveAnimations()
            || layoutManager.childCount == 0
            || state.isPreLayout
            || !layoutManager.supportsPredictiveItemAnimations()
        ) {
            return
        }
        // to make the logic simpler, we calculate the size of children and call fill.
        var scrapExtraStart = 0
        var scrapExtraEnd = 0
        val scrapList = recycler.scrapList
        val scrapSize = scrapList.size
        val firstChildPos: Int = layoutManager.getChildAt(0)?.let { view ->
            layoutManager.getPosition(view)
        } ?: return
        for (i in 0 until scrapSize) {
            val scrap = scrapList[i]
            val layoutParams = scrap.itemView.layoutParams as RecyclerView.LayoutParams
            if (layoutParams.isItemRemoved) {
                continue
            }
            val position = scrap.layoutPosition
            val direction = if (position < firstChildPos != layoutState.reverseLayout) {
                LayoutState.LayoutDirection.START
            } else {
                LayoutState.LayoutDirection.END
            }
            if (direction == LayoutState.LayoutDirection.START) {
                scrapExtraStart += layoutInfo.orientationHelper.getDecoratedMeasurement(scrap.itemView)
            } else {
                scrapExtraEnd += layoutInfo.orientationHelper.getDecoratedMeasurement(scrap.itemView)
            }
        }

        layoutState.scrappedViews = scrapList
        if (scrapExtraStart > 0) {
            layoutInfo.getChildClosestToStart()?.let { view ->
                updateLayoutStateToFillStart(layoutManager.getPosition(view), startOffset)
            }
            layoutState.extraFillSpace = scrapExtraStart
            layoutState.available = 0
            layoutState.assignPositionFromScrapList()
            fill(recycler, state)
        }
        if (scrapExtraEnd > 0) {
            layoutInfo.getChildClosestToEnd()?.let { view ->
                updateLayoutStateToFillEnd(layoutManager.getPosition(view), endOffset)
            }
            layoutState.extraFillSpace = scrapExtraEnd
            layoutState.available = 0
            layoutState.assignPositionFromScrapList()
            fill(recycler, state)
        }
        layoutState.scrappedViews = null
    }

    private fun updateLayoutStateToFillStart(position: Int, offset: Int) {
        layoutState.available = offset - layoutInfo.orientationHelper.startAfterPadding
        layoutState.currentPosition = position
        layoutState.itemDirection = if (layoutState.reverseLayout) {
            LayoutState.ItemDirection.TAIL
        } else {
            LayoutState.ItemDirection.HEAD
        }
        layoutState.direction = LayoutState.LayoutDirection.START
        layoutState.offset = offset
        layoutState.availableScrollSpace = LayoutState.SCROLL_SPACE_NONE
    }

    private fun updateLayoutStateToFillStart(pivotInfo: PivotInfo) {
        updateLayoutStateToFillStart(pivotInfo.position - 1, pivotInfo.headOffset)
    }

    private fun updateLayoutStateToFillEnd(position: Int, offset: Int) {
        layoutState.available = layoutInfo.orientationHelper.endAfterPadding - offset
        layoutState.itemDirection = if (layoutState.reverseLayout) {
            LayoutState.ItemDirection.HEAD
        } else {
            LayoutState.ItemDirection.TAIL
        }
        layoutState.currentPosition = position
        layoutState.direction = LayoutState.LayoutDirection.END
        layoutState.offset = offset
        layoutState.availableScrollSpace = LayoutState.SCROLL_SPACE_NONE
    }

    private fun updateLayoutStateToFillEnd(pivotInfo: PivotInfo) {
        updateLayoutStateToFillEnd(pivotInfo.position + 1, pivotInfo.tailOffset)
    }

    private fun updateLayoutState(
        direction: LayoutState.LayoutDirection,
        requiredSpace: Int,
        state: RecyclerView.State
    ) {
        layoutState.isInfinite = layoutInfo.isWrapContent()
        layoutState.direction = direction
        resetExtraLayoutSpace()

        // Update the available extra layout space
        calculateExtraLayoutSpace(state)

        // Make sure the layout state uses these latest calculated values
        updateLayoutStateForExtraSpace()

        val layoutToEnd = layoutState.isLayingOutEnd()
        if (layoutToEnd) {
            updateEndLayoutState()
        } else {
            updateStartLayoutState()
        }

        layoutState.available = requiredSpace
    }

    private fun updateExtraLayoutSpace(state: RecyclerView.State) {
        extraLayoutSpace[0] = 0
        extraLayoutSpace[1] = 0
        calculateExtraLayoutSpace(state)
    }

    private fun getExtraLayoutForStart(): Int {
        return max(0, extraLayoutSpace[0]) + layoutInfo.orientationHelper.startAfterPadding
    }

    private fun getExtraLayoutForEnd(): Int {
        return max(0, extraLayoutSpace[1]) + layoutInfo.orientationHelper.endPadding
    }

    private fun updateLayoutStateForExtraSpace() {
        val extraForStart = max(0, extraLayoutSpace[0])
        val extraForEnd = max(0, extraLayoutSpace[1])
        val layoutToEnd = layoutState.isLayingOutEnd()
        layoutState.extraFillSpace = if (layoutToEnd) extraForEnd else extraForStart
        layoutState.extraLayoutSpace = if (layoutToEnd) extraForStart else extraForEnd
    }

    private fun updateEndLayoutState() {
        layoutState.extraFillSpace += layoutInfo.orientationHelper.endPadding
        layoutState.itemDirection = if (layoutState.reverseLayout) {
            LayoutState.ItemDirection.HEAD
        } else {
            LayoutState.ItemDirection.TAIL
        }

        val child = layoutInfo.getChildClosestToEnd() ?: return

        layoutState.currentPosition =
            layoutManager.getPosition(child) + layoutState.itemDirection.value
        val decoratedEnd = layoutInfo.orientationHelper.getDecoratedEnd(child)
        layoutState.offset = decoratedEnd

        // calculate how much we can scroll without adding new children
        layoutState.availableScrollSpace =
            decoratedEnd - layoutInfo.orientationHelper.endAfterPadding
    }

    private fun updateStartLayoutState() {
        layoutState.extraFillSpace += layoutInfo.orientationHelper.startAfterPadding
        layoutState.itemDirection = if (layoutState.reverseLayout) {
            LayoutState.ItemDirection.TAIL
        } else {
            LayoutState.ItemDirection.HEAD
        }

        val child = layoutInfo.getChildClosestToStart() ?: return

        layoutState.currentPosition =
            layoutManager.getPosition(child) + layoutState.itemDirection.value

        val decoratedStart = layoutInfo.orientationHelper.getDecoratedStart(child)
        layoutState.offset = decoratedStart

        // calculate how much we can scroll without adding new children
        layoutState.availableScrollSpace =
            -decoratedStart + layoutInfo.orientationHelper.startAfterPadding
    }

    private fun resetExtraLayoutSpace() {
        extraLayoutSpace[0] = 0
        extraLayoutSpace[1] = 0
    }

    private fun calculateExtraLayoutSpace(state: RecyclerView.State) {
        var extraLayoutSpaceStart = 0
        var extraLayoutSpaceEnd = 0

        val extraScrollSpace = getDefaultExtraLayoutSpace(state)
        if (layoutState.isLayingOutStart()) {
            extraLayoutSpaceStart = extraScrollSpace
        } else {
            extraLayoutSpaceEnd = extraScrollSpace
        }
        extraLayoutSpace[0] = extraLayoutSpaceStart
        extraLayoutSpace[1] = extraLayoutSpaceEnd
    }

    /**
     * Layout an extra page by default if we're scrolling
     */
    private fun getDefaultExtraLayoutSpace(state: RecyclerView.State): Int {
        return if (state.hasTargetScrollPosition()) {
            layoutInfo.orientationHelper.totalSpace
        } else {
            0
        }
    }

    /**
     * Calculates the view layout order. (e.g. from end to start or start to end)
     * RTL layout support is applied automatically. So if layout is RTL and
     * [LayoutConfiguration.reverseLayout] is true, elements will be laid out starting from left.
     */
    private fun shouldReverseLayout(): Boolean {
        return if (configuration.isVertical() || !layoutInfo.isRTL()) {
            configuration.reverseLayout
        } else {
            !configuration.reverseLayout
        }
    }

    private fun log(message: String) {
        if (DEBUG) {
            Log.d(TAG, message)
        }
    }

}
