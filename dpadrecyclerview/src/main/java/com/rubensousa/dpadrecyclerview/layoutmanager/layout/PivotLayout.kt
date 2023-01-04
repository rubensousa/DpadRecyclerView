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
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid.GridLayoutEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller
import kotlin.math.max
import kotlin.math.min

internal class PivotLayout(
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

    private val layoutRequest = LayoutRequest()
    private val childLayoutListener = ChildLayoutListener()
    private var structureEngineer = createStructureEngineer()
    private val layoutCompleteListeners = ArrayList<DpadRecyclerView.OnLayoutCompletedListener>()

    fun updateStructure() {
        structureEngineer = createStructureEngineer()
        reset()
    }

    private fun createStructureEngineer(): StructureEngineer {
        return if (configuration.spanCount > 1) {
            GridLayoutEngineer(layoutManager, layoutInfo, layoutAlignment, childLayoutListener)
        } else {
            LinearLayoutEngineer(layoutManager, layoutInfo, layoutAlignment, childLayoutListener)
        }
    }

    /**
     * There's different stages of layout:
     * 1. First layout: just layout the pivot and then every view around it
     * 2. Intermediate layout requests: Items were inserted/removed/updated.
     * In this case, we need to update their layout positions.
     * This step shouldn't interfere with ongoing scroll events
     * 3. Views cleared: just remove all views
     */
    fun onLayoutChildren(recycler: Recycler, state: State) {
        if (DEBUG) {
            Log.i(TAG, "OnLayoutChildren: ${state.asString()}")
        }
        layoutRequest.init(
            isPreLayout = state.isPreLayout,
            gravity = configuration.gravity,
            isVertical = configuration.isVertical(),
            reverseLayout = configuration.reverseLayout
        )
        structureEngineer.init(layoutRequest, state)
        layoutInfo.setLayoutInProgress()
        layoutAlignment.update()

        // Fast removal
        if (state.itemCount == 0) {
            layoutManager.removeAndRecycleAllViews(recycler)
            reset()
            return
        }

        pivotSelector.consumePendingSelectionChanges()

        if (state.isPreLayout) {
            preLayoutChildren(pivotSelector.position, recycler, state)
            return
        }

        layoutChildren(recycler, state)
    }

    private fun preLayoutChildren(
        pivotPosition: Int,
        recycler: Recycler,
        recyclerViewState: State
    ) {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return
        }
        if (DEBUG) {
            Log.i(TAG, "PreLayoutStart")
            structureEngineer.logChildren()
        }

        structureEngineer.prelayout(pivotPosition, layoutRequest, recycler, recyclerViewState)

        if (DEBUG) {
            Log.i(TAG, "PreLayoutFinished")
            structureEngineer.logChildren()
        }
    }

    private fun layoutChildren(recycler: Recycler, recyclerViewState: State) {
        if (DEBUG) {
            Log.i(TAG, "LayoutStart")
            structureEngineer.logChildren()
        }

        structureEngineer.layout(pivotSelector.position, layoutRequest, recycler, recyclerViewState)

        if (DEBUG) {
            Log.i(TAG, "LayoutFinished")
            structureEngineer.logChildren()
        }
    }

    fun reset() {
        layoutRequest.clear()
    }

    fun onLayoutCompleted(state: State) {
        layoutInfo.onLayoutCompleted()
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
        structureEngineer.scrollBy(scrollOffset, layoutRequest, recycler, state)
        return scrollOffset
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

    private fun State.asString(): String {
        val remainingScroll = if (layoutInfo.isVertical()) {
            this.remainingScrollVertical
        } else {
            this.remainingScrollHorizontal
        }
        return "itemCount=${itemCount}, " +
                "remainingScroll=$remainingScroll, " +
                "predictiveAnimations=${willRunPredictiveAnimations()}"
    }

    private inner class ChildLayoutListener : OnChildLayoutListener {
        override fun onChildCreated(view: View, state: State) {
            scroller.onChildCreated(view)
        }

        override fun onChildLaidOut(view: View, state: State) {
            scroller.onChildLaidOut(view)
            layoutAlignment.updateScrollLimits()
        }
    }

}
