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
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.BuildConfig
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.GridArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LayoutResult
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller
import kotlin.math.max
import kotlin.math.min

internal class LayoutArchitect(
    private val layoutManager: RecyclerView.LayoutManager,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration,
    private val pivotSelector: PivotSelector,
    private val scroller: LayoutScroller,
    private val layoutInfo: LayoutInfo
) {

    companion object {
        const val TAG = "LayoutArchitect"
        private val DEBUG = BuildConfig.DEBUG
    }

    private val pivotInfo = PivotInfo()
    private val layoutState = LayoutState()
    private val layoutResult = LayoutResult()
    private val layoutCalculator = LayoutCalculator(layoutInfo)
    private val childRecycler = ChildRecycler(layoutManager, layoutInfo, configuration)
    private val rowArchitect = RowArchitect(
        layoutManager, layoutAlignment, layoutInfo, configuration, childRecycler,
        object : OnChildLayoutListener {
            override fun onChildCreated(view: View, state: State) {
                scroller.onChildCreated(view)
            }
            override fun onChildLaidOut(view: View, state: State) {
                layoutAlignment.updateScrollLimits()
                scroller.onChildLaidOut(view)
            }
        }
    )
    private val gridArchitect = GridArchitect(layoutManager, layoutInfo, configuration)
    private val layoutCompleteListeners = ArrayList<DpadRecyclerView.OnLayoutCompletedListener>()

    /**
     * There's different stages of layout:
     * 1. First layout: just layout the pivot and then every view around it
     * 2. Intermediate layout requests: Items were inserted/removed/updated.
     * In this case, we need to update their layout positions.
     * This step shouldn't interfere with ongoing scroll events
     * 3. Views cleared: just remove all views
     */
    fun onLayoutChildren(recycler: Recycler, state: State) {
        Log.i(TAG, "OnLayoutChildren: $state")
        layoutInfo.setLayoutInProgress()
        layoutAlignment.update()
        layoutCalculator.init(layoutState, state)

        // Fast removal
        if (state.itemCount == 0) {
            layoutManager.removeAndRecycleAllViews(recycler)
            layoutState.clear()
            return
        }

        predictiveLayoutPass(recycler, state)
    }

    private fun predictiveLayoutPass(recycler: Recycler, state: State) {
        pivotInfo.update(pivotSelector.position, layoutManager, state)

        layoutManager.detachAndScrapAttachedViews(recycler)

        rowArchitect.layoutPivot(layoutState, recycler, pivotInfo, state)

        // Layout views after the pivot
        layoutCalculator.updateLayoutStateAfterPivot(layoutState, pivotInfo)
        rowArchitect.layoutEnd(layoutState, recycler, state)

        // Layout views before the pivot
        layoutCalculator.updateLayoutStateBeforePivot(layoutState, pivotInfo)
        rowArchitect.layoutStart(layoutState, recycler, state)

        layoutForPredictiveAnimations(recycler, state)

        // Now that all views are laid out, make sure the pivot is still in the correct position
        alignPivot(recycler, state)

        // Layout extra space if needed
        layoutExtraSpace(recycler, state)

        // We might have views we no longer need after aligning the pivot, so recycle them
        removeInvisibleViews(recycler)
    }

    private fun layoutExtraSpace(recycler: Recycler, state: State) {
        layoutCalculator.updateLayoutStateForExtraLayoutStart(layoutState, state)
        rowArchitect.layoutStart(layoutState, recycler, state)

        layoutCalculator.updateLayoutStateForExtraLayoutEnd(layoutState, state)
        rowArchitect.layoutEnd(layoutState, recycler, state)
    }

    private fun removeInvisibleViews(recycler: Recycler) {
        layoutState.setRecyclingEnabled(true)
        childRecycler.recycleFromStart(recycler, layoutState)
        childRecycler.recycleFromEnd(recycler, layoutState)
    }

    /**
     * Layout scrap views that were not laid out in the previous step
     * to ensure animations work as expected.
     */
    private fun layoutForPredictiveAnimations(recycler: Recycler, state: State) {
        val firstChild = layoutManager.getChildAt(0)
        if (!state.willRunPredictiveAnimations() || firstChild == null || state.isPreLayout) {
            return
        }
        val scrapList = recycler.scrapList
        var scrapExtraStart = 0
        var scrapExtraEnd = 0
        val firstChildPosition = layoutInfo.getLayoutPositionOf(firstChild)
        for (i in 0 until scrapList.size) {
            val scrap = scrapList[i]
            if (layoutInfo.isRemoved(scrap)) {
                continue
            }
            val position = scrap.layoutPosition
            val direction = if (position < firstChildPosition != layoutState.reverseLayout) {
                LayoutDirection.START
            } else {
                LayoutDirection.END
            }
            if (direction == LayoutDirection.START) {
                scrapExtraStart += layoutInfo.getDecoratedSize(scrap.itemView)
            } else {
                scrapExtraEnd += layoutInfo.getDecoratedSize(scrap.itemView)
            }
        }

        layoutState.setExtraLayoutSpaceStart(scrapExtraStart)
        layoutState.setExtraLayoutSpaceEnd(scrapExtraEnd)
        layoutState.setScrap(recycler.scrapList)

        if (scrapExtraStart > 0) {
            val anchor = layoutInfo.getChildClosestToStart()
            if (anchor != null) {
                layoutCalculator.updateLayoutStateForPredictiveStart(
                    layoutState,
                    layoutManager.getPosition(anchor)
                )
                rowArchitect.layoutStart(layoutState, recycler, state)
            }
        }

        if (scrapExtraEnd > 0) {
            val anchor = layoutInfo.getChildClosestToEnd()
            if (anchor != null) {
                layoutCalculator.updateLayoutStateForPredictiveEnd(
                    layoutState,
                    layoutManager.getPosition(anchor)
                )
                rowArchitect.layoutEnd(layoutState, recycler, state)
            }
        }

        layoutState.setScrap(null)
    }

    private fun alignPivot(recycler: Recycler, state: State) {
        pivotInfo.update(pivotSelector.position, layoutManager, state)
        pivotInfo.view?.let { pivotView ->
            // Offset all views by the existing remaining scroll so that they're still scrolled
            // to their final locations
            val scrollOffset = layoutAlignment.calculateScrollForAlignment(pivotView)
            scrollBy(scrollOffset - getRemainingScroll(state), recycler, state)
        }
    }

    private fun getRemainingScroll(state: State): Int {
        return if (configuration.isVertical()) {
            state.remainingScrollVertical
        } else {
            state.remainingScrollHorizontal
        }
    }

    fun onLayoutCompleted(state: State) {
        layoutInfo.onLayoutCompleted()
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
        // Do nothing if we don't have children
        if (state.itemCount == 0 || offset == 0) {
            return 0
        }
        val scrollOffset = layoutAlignment.getCappedScroll(offset)

        // Offset views immediately
        offsetBy(scrollOffset)

        // Now layout the next views and recycle the ones we don't need along the way
        layoutCalculator.updateLayoutStateForScroll(layoutState, state, scrollOffset)
        rowArchitect.layout(layoutState, recycler, state)
        return scrollOffset
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
                    pivotSelector.position - (prefetchCount - 1) / 2,
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
