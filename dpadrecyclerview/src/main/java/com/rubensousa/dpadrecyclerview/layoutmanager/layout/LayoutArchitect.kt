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

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotState
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.GridArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LayoutResult
import kotlin.math.max
import kotlin.math.min

internal class LayoutArchitect(
    private val layoutManager: RecyclerView.LayoutManager,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration,
    private val pivotState: PivotState,
    private val layoutInfo: LayoutInfo
) {

    companion object {
        const val TAG = "LayoutArchitect"
        private const val DEBUG = true
    }

    private val pivotInfo = PivotInfo()
    private val layoutState = LayoutState()
    private val layoutResult = LayoutResult()
    private val layoutCalculator = LayoutCalculator(layoutInfo)
    private val rowArchitect = RowArchitect(
        layoutManager, layoutAlignment, layoutInfo, configuration
    )
    private val gridArchitect = GridArchitect(layoutManager, layoutInfo, configuration)
    private val layoutCompleteListeners = ArrayList<DpadRecyclerView.OnLayoutCompletedListener>()

    /**
     * There's different stages of layout:
     * 1. First layout: just layout the pivot and then every view around it
     * 2. Intermediate layout requests: Items were inserted/removed/updated.
     * In this case, we need to update their layout positions.
     * This step shouldn't interfere with ongoing scroll events
     */
    fun onLayoutChildren(recycler: Recycler, state: State) {
        layoutInfo.setLayoutInProgress(true)
        layoutAlignment.update()
        layoutCalculator.init(layoutState, state, configuration)

        if (recycler.scrapList.isEmpty() && layoutManager.childCount == 0) {
            if (!state.isPreLayout) {
                initialLayoutPass(recycler, state)
            }
            return
        }

        predictiveLayoutPass(recycler, state)
    }

    private fun initialLayoutPass(recycler: Recycler, state: State) {
        // Detach all existing views before updating the layout
        layoutManager.detachAndScrapAttachedViews(recycler)

        pivotInfo.update(pivotState.position, layoutManager, state)

        // Start the layout with the pivot since all other Views are around it
        rowArchitect.layoutPivot(layoutState, recycler, pivotInfo)

        // Layout views before the pivot
        layoutCalculator.updateLayoutStateBeforePivot(layoutState, pivotInfo)
        rowArchitect.layoutStart(layoutState, recycler, state)

        // Now layout views after the pivot
        layoutCalculator.updateLayoutStateAfterPivot(layoutState, pivotInfo)
        rowArchitect.layoutEnd(layoutState, recycler, state)

        // Now that all views are laid out, make sure the pivot is still in the correct position
        alignPivot(recycler, state)
    }

    private fun predictiveLayoutPass(recycler: Recycler, state: State) {
        if (state.isPreLayout) {
            onPreLayoutChildren(recycler, state)
            return
        }
        pivotInfo.update(pivotState.position, layoutManager, state)

        layoutManager.detachAndScrapAttachedViews(recycler)

        rowArchitect.layoutPivot(layoutState, recycler, pivotInfo)

        // Layout views before the pivot
        layoutCalculator.updateLayoutStateBeforePivot(layoutState, pivotInfo)
        rowArchitect.layoutStart(layoutState, recycler, state)

        // Layout views after the pivot
        layoutCalculator.updateLayoutStateAfterPivot(layoutState, pivotInfo)
        rowArchitect.layoutEnd(layoutState, recycler, state)

        alignPivot(recycler, state)

        // Offset the pivot by the existing remaining scroll to prevent over scrolling it
        val remainingScroll = if(configuration.isVertical()) {
            state.remainingScrollVertical
        } else {
            state.remainingScrollHorizontal
        }
        offsetBy(-remainingScroll)

        layoutForPredictiveAnimations(recycler, state)
    }

    private fun layoutForPredictiveAnimations(recycler: Recycler, state: State) {
        if (!state.willRunPredictiveAnimations() || state.isPreLayout) {
            return
        }
        // Do nothing if we don't have any children now
        val firstChild = layoutManager.getChildAt(0)
        val lastChild = layoutManager.getChildAt(layoutManager.childCount - 1)
        if (firstChild == null || lastChild == null) {
            return
        }

        var minEdge = Int.MAX_VALUE
        var maxEdge = Int.MIN_VALUE
        val minOldPosition = layoutInfo.getChildViewHolder(firstChild)?.oldPosition
            ?: RecyclerView.NO_POSITION
        val maxOldPosition = layoutInfo.getChildViewHolder(lastChild)?.oldPosition
            ?: RecyclerView.NO_POSITION

        val scrapList = recycler.scrapList

        for (i in 0 until scrapList.size) {
            val view = scrapList[i].itemView
            val layoutParams = layoutInfo.getLayoutParams(view)
            if (didChildStateChange(view, layoutParams, minOldPosition, maxOldPosition)) {
                minEdge = min(minEdge, layoutInfo.getDecoratedStart(view))
                maxEdge = max(maxEdge, layoutInfo.getDecoratedEnd(view))
            }
        }

        if (maxEdge > minEdge) {
            // Add extra space in both directions
            // since we need to make sure the pivot is still aligned
            layoutState.setExtraLayoutSpace(maxEdge - minEdge)
        }

        layoutState.setScrap(scrapList)

        val viewClosestToStart = layoutInfo.getChildClosestToStart()
        val viewClosestToEnd = layoutInfo.getChildClosestToEnd()

        if (viewClosestToStart != null) {
            layoutCalculator.updateLayoutStateForPredictiveStart(layoutState, viewClosestToStart)
            rowArchitect.layoutStart(layoutState, recycler, state)
        }

        if (viewClosestToEnd != null) {
            layoutCalculator.updateLayoutStateForPredictiveEnd(layoutState, viewClosestToEnd)
            rowArchitect.layoutEnd(layoutState, recycler, state)
        }

        layoutState.setScrap(null)
    }

    /**
     * RecyclerView will run predictive item animations,
     * so we need to layout the views in their current outdated positions and add any new views in.
     * Steps:
     * 1. Layout the existing views in their "old" positions
     * 2. Layout the new views in their "temporary" positions before the changes are applied
     */
    private fun onPreLayoutChildren(recycler: Recycler, state: State) {
        // Do nothing if we don't have any children now
        val firstChild = layoutManager.getChildAt(0)
        val lastChild = layoutManager.getChildAt(layoutManager.childCount - 1)
        if (firstChild == null || lastChild == null) {
            return
        }
        var minEdge = Int.MAX_VALUE
        var maxEdge = Int.MIN_VALUE
        val minOldPosition = layoutInfo.getChildViewHolder(firstChild)?.oldPosition
            ?: RecyclerView.NO_POSITION
        val maxOldPosition = layoutInfo.getChildViewHolder(lastChild)?.oldPosition
            ?: RecyclerView.NO_POSITION

        val childCount = layoutManager.childCount

        /**
         * Traverse all children to get the new bounds for layout
         * Since a child could've changed its size or moved by now,
         * we need to keep track of the total space required to handle its changes
         */
        for (i in 0 until childCount) {
            val view = requireNotNull(layoutManager.getChildAt(i))
            val layoutParams = layoutInfo.getLayoutParams(view)
            if (didChildStateChange(view, layoutParams, minOldPosition, maxOldPosition)) {
                minEdge = min(minEdge, layoutInfo.getDecoratedStart(view))
                maxEdge = max(maxEdge, layoutInfo.getDecoratedEnd(view))
            }
        }
        if (maxEdge > minEdge) {
            // Add extra space in both directions
            // since we need to make sure the pivot is still aligned
            layoutState.setExtraLayoutSpace(maxEdge - minEdge)
        }

        val viewClosestToStart = layoutInfo.getChildClosestToStart()
        val viewClosestToEnd = layoutInfo.getChildClosestToEnd()

        // Detach all existing views now that we know the changes required
        layoutManager.detachAndScrapAttachedViews(recycler)

        if (viewClosestToStart != null) {
            layoutCalculator.updatePreLayoutStateBeforeStart(layoutState, viewClosestToStart)
            rowArchitect.layoutStart(layoutState, recycler, state)
        }

        if (viewClosestToEnd != null) {
            layoutCalculator.updatePreLayoutStateAfterEnd(layoutState, viewClosestToEnd)
            rowArchitect.layoutEnd(layoutState, recycler, state)
        }

    }

    private fun didChildStateChange(
        view: View,
        layoutParams: LayoutParams,
        minOldPosition: Int,
        maxOldPosition: Int
    ): Boolean {
        // If layout might change
        if (layoutParams.isItemChanged || layoutParams.isItemRemoved || view.isLayoutRequested) {
            return true
        }
        // If focus was lost
        if (view.hasFocus() && pivotState.position != layoutParams.absoluteAdapterPosition) {
            return true
        }
        // If focus was gained
        if (!view.hasFocus() && pivotState.position == layoutParams.absoluteAdapterPosition) {
            return true
        }
        val newPosition = layoutInfo.getAdapterPositionOf(view)
        // If it moved outside the previous visible range
        return newPosition < minOldPosition || newPosition > maxOldPosition
    }

    private fun alignPivot(recycler: Recycler, state: State) {
        pivotInfo.update(pivotState.position, layoutManager, state)
        pivotInfo.view?.let { pivotView ->
            val scrollOffset = layoutAlignment.calculateScrollForAlignment(pivotView)
            offsetBy(scrollOffset)
        }
    }

    fun onLayoutCompleted(state: State) {
        layoutInfo.setLayoutInProgress(false)
        layoutResult.reset()
        layoutCompleteListeners.forEach { listener ->
            listener.onLayoutCompleted(state)
        }
    }

    fun addOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        layoutCompleteListeners.add(listener)
    }

    fun removeOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        layoutCompleteListeners.remove(listener)
    }

    fun clearOnLayoutCompletedListeners() {
        layoutCompleteListeners.clear()
    }

    fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: State
    ): Int {
        if (configuration.isVertical()) {
            return 0
        }
        return scrollBy(dx, recycler, state)
    }

    fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: State
    ): Int {
        Log.i(TAG, "ScrollVertically by: $dy")
        if (configuration.isHorizontal()) {
            return 0
        }
        return scrollBy(dy, recycler, state)
    }

    private fun scrollBy(
        offset: Int,
        recycler: RecyclerView.Recycler,
        state: State
    ): Int {
        pivotInfo.update(pivotState.position, layoutManager, state)
        val pivotView = pivotInfo.view
        // Do nothing if we don't have children or a valid pivot
        if (layoutManager.childCount == 0 || offset == 0 || pivotView == null) {
            return 0
        }
        layoutCalculator.updateLayoutStateForScroll(layoutState, state, offset)
        rowArchitect.layout(layoutState, recycler, state)
        offsetBy(offset)
        return offset
    }

    private fun offsetBy(offset: Int) {
        layoutInfo.orientationHelper.offsetChildren(-offset)
        pivotInfo.offset(-offset)
        layoutState.offsetWindow(-offset)
    }

    // TODO
    fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: State?,
        layoutPrefetchRegistry: RecyclerView.LayoutManager.LayoutPrefetchRegistry
    ) {

    }

    fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: RecyclerView.LayoutManager.LayoutPrefetchRegistry
    ) {
        val prefetchCount: Int = configuration.initialPrefetchItemCount
        if (adapterItemCount != 0 && prefetchCount != 0) {
            // Prefetch items centered around the pivot
            val initialPosition = max(
                0, min(
                    pivotState.position - (prefetchCount - 1) / 2,
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

}
