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
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid.GridLayoutEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller

internal class PivotLayout(
    private val layoutManager: RecyclerView.LayoutManager,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration,
    private val pivotSelector: PivotSelector,
    private val scroller: LayoutScroller,
    private val layoutInfo: LayoutInfo
) {

    companion object {
        const val TAG = "PivotLayout"
    }

    private val childLayoutListener = ChildLayoutListener()
    private var structureEngineer = createStructureEngineer()
    private val layoutCompleteListeners = ArrayList<DpadRecyclerView.OnLayoutCompletedListener>()
    private val itemChanges = ItemChanges()

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

    fun onLayoutChildren(recycler: Recycler, state: State) {
        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "OnLayoutChildren: ${state.asString()}")
        }
        structureEngineer.onLayoutStarted(state)
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
        state: State
    ) {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return
        }
        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "PreLayoutStart: ${state.asString()}")
            structureEngineer.logChildren()
        }

        structureEngineer.preLayoutChildren(pivotPosition, recycler, state)

        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "PreLayoutFinished")
            structureEngineer.logChildren()
        }
    }

    private fun layoutChildren(recycler: Recycler, state: State) {
        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "LayoutStart: ${state.asString()}")
            structureEngineer.logChildren()
        }

        structureEngineer.layoutChildren(pivotSelector.position, itemChanges, recycler, state)

        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "LayoutFinished")
            structureEngineer.logChildren()
        }

        structureEngineer.onLayoutFinished()
    }

    fun reset() {
        structureEngineer.clear()
    }

    fun onItemsAdded(positionStart: Int, itemCount: Int) {
        itemChanges.insertionPosition = positionStart
        itemChanges.insertionItemCount = itemCount
    }

    fun onItemsRemoved(positionStart: Int, itemCount: Int) {
        itemChanges.removalPosition = positionStart
        itemChanges.removalItemCount = itemCount
    }

    fun onItemsMoved(from: Int, to: Int, itemCount: Int) {
        itemChanges.moveFromPosition = from
        itemChanges.moveToPosition = to
        itemChanges.moveItemCount = itemCount
    }

    fun onLayoutCompleted(state: State) {
        itemChanges.reset()
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
        return structureEngineer.scrollBy(scrollOffset, recycler, state, recycleChildren = true)
    }

    private fun State.asString(): String {
        val remainingScroll = if (layoutInfo.isVertical()) {
            this.remainingScrollVertical
        } else {
            this.remainingScrollHorizontal
        }
        return "itemCount=${itemCount}, " +
                "didStructureChange=${didStructureChange()}, " +
                "remainingScroll=$remainingScroll, " +
                "predictiveAnimations=${willRunPredictiveAnimations()}"
    }

    private inner class ChildLayoutListener : OnChildLayoutListener {
        override fun onChildCreated(view: View) {
            scroller.onChildCreated(view)
        }

        override fun onChildLaidOut(view: View) {
            scroller.onChildLaidOut(view)
        }
    }

}
