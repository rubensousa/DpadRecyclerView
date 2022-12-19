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

package com.rubensousa.dpadrecyclerview.layoutmanager

import android.os.Parcel
import android.os.Parcelable
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

/**
 * Responsibilities:
 * - Holding the current pivot state
 * - Saving/restoring pivot state
 */
internal class PivotSelector(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo
) {

    companion object {
        const val TAG = "PivotState"
    }

    var position: Int = RecyclerView.NO_POSITION
        private set

    var subPosition: Int = 0
        private set

    private var recyclerView: RecyclerView? = null
    private var isSelectionUpdatePending = false
    private val selectionListeners = ArrayList<OnViewHolderSelectedListener>()
    private val requestLayoutRunnable = Runnable {
        layoutInfo.requestLayout()
    }
    private var selectedViewHolder: DpadViewHolder? = null

    fun getCurrentSubPositions(): Int {
        return selectedViewHolder?.getAlignments()?.size ?: 0
    }

    fun setSelectionUpdatePending() {
        isSelectionUpdatePending = true
    }

    fun onLayoutChildren(state: RecyclerView.State) {
        // Make sure the pivot is set to 0 by default whenever we have items
        if (position == RecyclerView.NO_POSITION && state.itemCount > 0) {
            position = 0
            isSelectionUpdatePending = true
        }
    }

    fun onLayoutCompleted() {
        // If we had items, but now we don't, trigger an update for RecyclerView.NO_POSITION
        if (position >= 0 && layoutManager.childCount == 0) {
            position = RecyclerView.NO_POSITION
            subPosition = 0
            isSelectionUpdatePending = true
        }
        if (isSelectionUpdatePending) {
            isSelectionUpdatePending = false
            dispatchViewHolderSelected()
            dispatchViewHolderSelectedAndAligned()
        }
    }

    // TODO
    fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {

    }

    // TODO
    fun onItemsChanged(recyclerView: RecyclerView) {

    }

    // TODO
    fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {

    }

    // TODO
    fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {

    }

    // TODO
    fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {

    }

    fun update(position: Int, subPosition: Int = 0): Boolean {
        val changed = position != this.position || subPosition != this.subPosition
        this.position = position
        this.subPosition = subPosition
        return changed
    }

    fun onSaveInstanceState(): Parcelable {
        return SavedState(position)
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            position = state.selectedPosition
            if (position != RecyclerView.NO_POSITION) {
                isSelectionUpdatePending = true
                layoutManager.requestLayout()
            }
        }
    }

    fun dispatchViewHolderSelected() {
        val recyclerView = this.recyclerView ?: return
        val view = if (position == RecyclerView.NO_POSITION) {
            null
        } else {
            layoutInfo.findViewByPosition(position)
        }

        val viewHolder = if (view != null) {
            recyclerView.getChildViewHolder(view)
        } else {
            null
        }

        selectedViewHolder?.onViewHolderDeselected()

        if (viewHolder is DpadViewHolder) {
            selectedViewHolder = viewHolder
            viewHolder.onViewHolderSelected()
        } else {
            selectedViewHolder = null
        }

        if (!hasSelectionListeners()) {
            return
        }

        if (viewHolder != null) {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelected(
                    recyclerView, viewHolder, position, subPosition
                )
            }
        } else {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelected(
                    recyclerView, null, RecyclerView.NO_POSITION, 0
                )
            }
        }

        /**
         * We might have a requestLayout event from children reacting to the selection changes,
         * so schedule a new layout pass if we're not in the layout phase.
         */
        if (!layoutInfo.isLayoutInProgress && !recyclerView.isLayoutRequested) {
            val childCount = layoutInfo.getChildCount()
            for (i in 0 until childCount) {
                val child = layoutInfo.getChildAt(i)
                if (child != null && child.isLayoutRequested) {
                    scheduleNewLayout(recyclerView)
                    break
                }
            }
        }
    }

    fun dispatchViewHolderSelectedAndAligned() {
        val recyclerView = this.recyclerView ?: return

        if (!hasSelectionListeners()) {
            return
        }

        val view = if (position == RecyclerView.NO_POSITION) {
            null
        } else {
            layoutInfo.findViewByPosition(position)
        }
        val viewHolder = if (view != null) {
            recyclerView.getChildViewHolder(view)
        } else {
            null
        }

        if (viewHolder != null) {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelectedAndAligned(
                    recyclerView, viewHolder, position, subPosition
                )
            }
        } else {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelectedAndAligned(
                    recyclerView, null, RecyclerView.NO_POSITION, 0
                )
            }
        }
    }

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        selectionListeners.add(listener)
    }

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        selectionListeners.remove(listener)
    }

    fun clearOnViewHolderSelectedListeners() {
        selectionListeners.clear()
    }

    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
    }

    /**
     * RecyclerView prevents us from requesting layout in many cases
     * (during layout, during scroll, etc.)
     * We might need to resize rows when wrap_content is used, so schedule a new layout request
     */
    private fun scheduleNewLayout(recyclerView: RecyclerView) {
        ViewCompat.postOnAnimation(recyclerView, requestLayoutRunnable)
    }

    private fun hasSelectionListeners(): Boolean = selectionListeners.isNotEmpty()

    data class SavedState(val selectedPosition: Int) : Parcelable {

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        constructor(parcel: Parcel) : this(parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(selectedPosition)
        }

        override fun describeContents(): Int {
            return 0
        }
    }

}
