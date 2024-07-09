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

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadLoopDirection
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnChildLaidOutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
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
    private val layoutInfo: LayoutInfo,
) {

    companion object {
        const val TAG = "PivotLayout"
    }

    private val childLayoutListener = ChildLayoutListener()
    private var layoutListener: OnChildLaidOutListener? = null
    private var structureEngineer = createStructureEngineer()
    private val layoutCompleteListeners = ArrayList<DpadRecyclerView.OnLayoutCompletedListener>()
    private val itemChanges = ItemChanges()
    private var anchor: Int? = null
    private var initialSelectionPending = false

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

    fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (state.itemCount == 0) {
            // Clear the selected position if there are no more items
            if (pivotSelector.position != RecyclerView.NO_POSITION) {
                pivotSelector.setSelectionUpdatePending()
            }
            pivotSelector.reset()
        } else if (pivotSelector.position >= state.itemCount) {
            pivotSelector.update(newPosition = state.itemCount - 1)
            pivotSelector.setSelectionUpdatePending()
        } else if (pivotSelector.position == RecyclerView.NO_POSITION && state.itemCount > 0) {
            // Make sure the pivot is set to the first focusable position whenever we have items
            initialSelectionPending = true
            pivotSelector.setSelectionUpdatePending()
        }

        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "OnLayoutChildren: ${state.asString()}")
        }
        layoutInfo.setLayoutInProgress()

        // Fast removal
        if (state.itemCount == 0 || !configuration.isLayoutEnabled) {
            layoutManager.removeAndRecycleAllViews(recycler)
            reset()
            return
        }

        pivotSelector.consumePendingSelectionChanges(state)
        structureEngineer.onLayoutStarted(state)

        if (state.isPreLayout) {
            preLayoutChildren(pivotSelector.position, recycler, state)
            return
        }

        layoutChildren(recycler, state)
    }

    private fun preLayoutChildren(
        pivotPosition: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
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

    private fun layoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "LayoutStart for pivot ${pivotSelector.position}: ${state.asString()}")
            structureEngineer.logChildren()
        }

        if (configuration.keepLayoutAnchor) {
            saveAnchorState()
        }

        structureEngineer.layoutChildren(pivotSelector.position, itemChanges, recycler, state)

        if (configuration.keepLayoutAnchor) {
            restoreAnchorState(recycler, state)
        }

        if (DpadRecyclerView.DEBUG) {
            Log.i(TAG, "LayoutFinished")
            structureEngineer.logChildren()
        }

        structureEngineer.onLayoutFinished()
    }

    private fun saveAnchorState() {
        val currentPivotPosition = if (!layoutInfo.shouldReverseLayout()) {
            layoutInfo.findFirstVisiblePosition()
        } else {
            layoutInfo.findLastVisiblePosition()
        }
        if (currentPivotPosition == RecyclerView.NO_POSITION) {
            return
        }
        pivotSelector.update(currentPivotPosition)
        layoutInfo.findViewByPosition(currentPivotPosition)?.let { view ->
            anchor = if (!layoutInfo.shouldReverseLayout()) {
                layoutInfo.getDecoratedStart(view)
            } else {
                layoutInfo.getDecoratedEnd(view)
            }
        }
    }

    private fun restoreAnchorState(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        anchor?.let { previousAnchor ->
            if (pivotSelector.position != RecyclerView.NO_POSITION) {
                layoutInfo.findViewByPosition(pivotSelector.position)?.let { view ->
                    val currentAnchor = if (!layoutInfo.shouldReverseLayout()) {
                        layoutInfo.getDecoratedStart(view)
                    } else {
                        layoutInfo.getDecoratedEnd(view)
                    }
                    scrollBy(currentAnchor - previousAnchor, recycler, state)
                }
            }
            anchor = null
        }
    }

    fun onLayoutCompleted(state: RecyclerView.State) {
        if (initialSelectionPending) {
            initialSelectionPending = false
            updateInitialSelection()
        }
        itemChanges.reset()
        layoutInfo.onLayoutCompleted()
        layoutCompleteListeners.forEach { listener ->
            listener.onLayoutCompleted(state)
        }
    }

    /**
     * We need to confirm that the initial selection makes sense.
     * It can happen that position 0 points to a non focusable position
     */
    private fun updateInitialSelection() {
        val view = findFirstFocusableView() ?: return
        val adapterPosition = layoutInfo.getAdapterPositionOf(view)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            return
        }
        if (pivotSelector.position != adapterPosition) {
            pivotSelector.update(adapterPosition)
            scroller.scrollToSelectedPosition(smooth = false)
        }
    }

    private fun findFirstFocusableView(): View? {
        for (i in 0 until layoutManager.itemCount) {
            val view = layoutInfo.findViewByAdapterPosition(i)
            if (view != null && layoutInfo.isViewFocusable(view)) {
                return view
            }
        }
        return null
    }

    fun reset() {
        structureEngineer.clear()
    }

    fun onItemsAdded(positionStart: Int, itemCount: Int) {
        itemChanges.insertionPosition = positionStart
        itemChanges.insertionItemCount = itemCount
        onItemsChanged()
    }

    fun onItemsRemoved(positionStart: Int, itemCount: Int) {
        itemChanges.removalPosition = positionStart
        itemChanges.removalItemCount = itemCount
        onItemsChanged()
    }

    fun onItemsMoved(from: Int, to: Int, itemCount: Int) {
        itemChanges.moveFromPosition = from
        itemChanges.moveToPosition = to
        itemChanges.moveItemCount = itemCount
        onItemsChanged()
    }

    private fun onItemsChanged() {
        if (!layoutInfo.isScrolling) {
            return
        }
        val firstPos = layoutInfo.findFirstAddedPosition()
        val lastPos = layoutInfo.findLastAddedPosition()
        val changesOutOfBounds = if (!layoutInfo.shouldReverseLayout()) {
            itemChanges.isOutOfBounds(firstPos, lastPos)
        } else {
            itemChanges.isOutOfBounds(lastPos, firstPos)
        }
        if (changesOutOfBounds) {
            return
        }
        layoutInfo.getRecyclerView()?.apply {
            stopScroll()
            requestLayout()
        }
    }

    fun setOnChildLaidOutListener(listener: OnChildLaidOutListener?) {
        layoutListener = listener
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
        state: RecyclerView.State,
    ): Int {
        if (configuration.isVertical()) {
            return 0
        }
        return scrollBy(dx, recycler, state)
    }

    fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int {
        if (configuration.isHorizontal()) {
            return 0
        }
        return scrollBy(dy, recycler, state)
    }

    fun onSaveInstanceState(): Parcelable {
        return SavedState(
            pivotSelector.position,
            layoutInfo.isLoopingStart,
            layoutInfo.isLoopingAllowed,
            layoutInfo.getConfiguration().loopDirection
        )
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            pivotSelector.update(state.selectedPosition)
            layoutInfo.updateLoopingState(state.isLoopingStart, state.isLoopingAllowed)
            layoutInfo.getConfiguration().setLoopDirection(state.loopDirection)
            if (state.selectedPosition != RecyclerView.NO_POSITION) {
                pivotSelector.setSelectionUpdatePending()
                layoutManager.requestLayout()
            }
        }
    }

    private fun scrollBy(
        offset: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int {
        // Do nothing if we don't have children
        if (state.itemCount == 0 || offset == 0 || !configuration.isLayoutEnabled) {
            return 0
        }
        val scrollOffset = layoutAlignment.getCappedScroll(offset)
        return structureEngineer.scrollBy(scrollOffset, recycler, state, recycleChildren = true)
    }

    private fun RecyclerView.State.asString(): String {
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

    private data class SavedState(
        val selectedPosition: Int,
        val isLoopingStart: Boolean,
        val isLoopingAllowed: Boolean,
        val loopDirection: DpadLoopDirection,
    ) : Parcelable {

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        constructor(parcel: Parcel) : this(
            selectedPosition = parcel.readInt(),
            isLoopingStart = parcel.readByte() == 1.toByte(),
            isLoopingAllowed = parcel.readByte() == 1.toByte(),
            loopDirection = DpadLoopDirection.values()[parcel.readInt()]
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(selectedPosition)
            parcel.writeByte(if (isLoopingStart) 1 else 0)
            parcel.writeByte(if (isLoopingAllowed) 1 else 0)
            parcel.writeInt(loopDirection.ordinal)
        }

        override fun describeContents(): Int {
            return 0
        }
    }

    private inner class ChildLayoutListener : OnChildLayoutListener {
        override fun onChildCreated(view: View) {
            scroller.onChildCreated(view)
        }

        override fun onChildLaidOut(view: View) {
            scroller.onChildLaidOut(view)
            val layoutParams = view.layoutParams as DpadLayoutParams
            // If this is the new pivot, request focus now that it was found
            // in case it didn't get focus yet
            if (!scroller.isSearchingPivot()
                && !view.hasFocus()
                && layoutParams.absoluteAdapterPosition == pivotSelector.position
            ) {
                scroller.scrollToSelectedPosition(
                    smooth = configuration.isSmoothFocusChangesEnabled
                )
            }
            layoutListener?.let { listener ->
                val recyclerView = layoutInfo.getRecyclerView() ?: return@let
                val viewHolder = layoutInfo.getChildViewHolder(view) ?: return
                listener.onChildLaidOut(recyclerView, viewHolder)
            }
        }

        override fun onBlockLaidOut() {
            scroller.onBlockLaidOut()
        }

    }

}
