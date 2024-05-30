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

import android.util.Log
import android.view.View
import android.view.ViewParent
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.OnViewFocusedListener
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import kotlin.math.max
import kotlin.math.min

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
        const val OFFSET_DISABLED = Int.MIN_VALUE
    }

    var isRetainingFocus = false

    var position: Int = RecyclerView.NO_POSITION
        private set

    var subPosition: Int = 0
        private set

    /**
     * The offset to be applied to [position], due to adapter change, on the next layout pass.
     * Set to [OFFSET_DISABLED] means we should stop adding it to [position] until the next layout.
     */
    private var positionOffset = 0
    private var recyclerView: RecyclerView? = null
    private var isSelectionUpdatePending = false
    private val selectionListeners = ArrayList<OnViewHolderSelectedListener>()
    private val focusListeners = ArrayList<OnViewFocusedListener>()
    private val requestLayoutRunnable = Runnable {
        layoutManager.requestLayout()
    }
    private var selectedViewHolder: DpadViewHolder? = null
    private var pendingChildFocus: View? = null

    fun update(newPosition: Int, newSubPosition: Int = 0): Boolean {
        val previousPosition = position
        val previousSubPosition = subPosition
        position = constrainPivotPosition(
            position = newPosition,
            itemCount = layoutManager.itemCount
        )
        subPosition = newSubPosition
        return position != previousPosition || subPosition != previousSubPosition
    }

    fun consumePendingSelectionChanges(state: RecyclerView.State): Boolean {
        var consumed = false
        if (position != RecyclerView.NO_POSITION
            && positionOffset != OFFSET_DISABLED
            && positionOffset != 0
        ) {
            applyPositionOffset(state.itemCount)
            subPosition = 0
            consumed = true
        } else {
            // If we didn't adjust the pivot, just ensure it's still within bounds
            position = constrainPivotPosition(
                position = position,
                itemCount = state.itemCount
            )
        }
        positionOffset = 0
        return consumed
    }

    fun focus(view: View) {
        view.requestFocus()
        // Exit early if there's no one listening for focus events
        if (focusListeners.isEmpty() || isRetainingFocus) {
            return
        }
        val currentRecyclerView = recyclerView ?: return
        /**
         * Do not notify listeners for views that are not a direct child of this RecyclerView
         * This will happen when a parent RecyclerView
         * finds a focusable inside a nested RecyclerView
         */
        if (findParentRecyclerView(view) !== currentRecyclerView) {
            return
        }

        /**
         * If the view didn't receive focus directly,
         * we need to verify if the focused view is actually part of this RecyclerView.
         */
        if (view.hasFocus() && !view.isFocused) {
            val focusedView = view.findFocus()
            if (focusedView != null
                && findParentRecyclerView(focusedView) !== currentRecyclerView
            ) {
                return
            }
        }
        val focusedViewHolder = currentRecyclerView.findContainingViewHolder(view) ?: return
        focusListeners.forEach { listener ->
            listener.onViewFocused(
                parent = focusedViewHolder,
                child = view,
            )
        }
        // Now signal the event to any parent RecyclerView, if it exists
        val parentRecyclerView = findParentRecyclerView(currentRecyclerView)
        if (parentRecyclerView is DpadRecyclerView) {
            parentRecyclerView.onNestedChildFocused(view)
        }
    }

    fun notifyNestedChildFocus(view: View) {
        pendingChildFocus = null
        val parentViewHolder = recyclerView?.findViewHolderForLayoutPosition(position) ?: return
        if (!isViewAChildOf(view, parentViewHolder)) {
            pendingChildFocus = view
            return
        }
        focusListeners.forEach { listener ->
            listener.onViewFocused(
                parent = parentViewHolder,
                child = view
            )
        }
    }

    fun onChildFocused(focusedView: View?) {
        pendingChildFocus?.let { view ->
            if (view === focusedView) {
                notifyNestedChildFocus(view)
            }
        }
        pendingChildFocus = null
    }

    private fun isViewAChildOf(view: View, viewHolder: ViewHolder): Boolean {
        var parent: ViewParent? = view.parent
        while (parent != null) {
            if (parent === viewHolder.itemView) {
                return true
            }
            parent = parent.parent
        }
        return false
    }

    private fun findParentRecyclerView(view: View): RecyclerView? {
        var parent: ViewParent? = view.parent
        while (parent != null) {
            if (parent is RecyclerView) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

    private fun applyPositionOffset(itemCount: Int) {
        val previousPosition = position
        position = constrainPivotPosition(
            position = position + positionOffset,
            itemCount = itemCount
        )
        if (position != previousPosition) {
            setSelectionUpdatePending()
        }
    }

    /**
     * Calculates the pivot position so that is within bounds of the current layout state
     */
    private fun constrainPivotPosition(position: Int, itemCount: Int): Int {
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }
        return max(0, min(itemCount - 1, position))
    }

    fun onLayoutChildren(state: RecyclerView.State) {
        // Clear the selected position if there are no more items
        if (state.itemCount == 0) {
            isSelectionUpdatePending = position != RecyclerView.NO_POSITION
            position = RecyclerView.NO_POSITION
            subPosition = 0
        } else if (position >= state.itemCount) {
            position = state.itemCount - 1
            subPosition = 0
        } else if (position == RecyclerView.NO_POSITION && state.itemCount > 0) {
            // Make sure the pivot is set to 0 by default whenever we have items
            position = 0
            positionOffset = 0
            setSelectionUpdatePending()
        }
    }

    fun onLayoutCompleted() {
        if (isSelectionUpdatePending) {
            isSelectionUpdatePending = false
            dispatchViewHolderSelected()
            dispatchViewHolderSelectedAndAligned()
        }
    }

    fun getCurrentSubPositions(): Int {
        return selectedViewHolder?.getSubPositionAlignments()?.size ?: 0
    }

    fun setSelectionUpdatePending() {
        isSelectionUpdatePending = true
    }

    fun resetPositionOffset() {
        positionOffset = 0
    }

    fun disablePositionOffset() {
        positionOffset = OFFSET_DISABLED
    }

    fun onItemsAdded(positionStart: Int, itemCount: Int) {
        if (DpadRecyclerView.DEBUG) {
            Log.i(
                DpadRecyclerView.TAG,
                "onItemsAdded: $itemCount, positionStart: $positionStart, totalItems: ${layoutManager.itemCount}"
            )
        }
        if (position != RecyclerView.NO_POSITION && positionOffset != OFFSET_DISABLED) {
            val finalPosition = position + positionOffset
            if (positionStart <= finalPosition) {
                // If items are inserted before the pivot,
                // move its position by the item count
                positionOffset += itemCount
            }
        }
    }

    fun onItemsChanged() {
        resetPositionOffset()
    }

    fun onItemsRemoved(positionStart: Int, itemCount: Int) {
        if (DpadRecyclerView.DEBUG) {
            Log.i(
                DpadRecyclerView.TAG,
                "onItemsRemoved: $itemCount, positionStart: $positionStart, totalItems: ${layoutManager.itemCount}"
            )
        }
        if (position != RecyclerView.NO_POSITION && positionOffset != OFFSET_DISABLED) {
            val finalPosition = position + positionOffset
            if (positionStart > finalPosition) {
                // Change was out of bounds, just ignore
                return
            }
            if (positionStart + itemCount > finalPosition) {
                // If the focused position was removed,
                // stop updating the offset until the next layout pass
                positionOffset += positionStart - finalPosition
                applyPositionOffset(layoutManager.itemCount)
                positionOffset = Int.MIN_VALUE
                setSelectionUpdatePending()
            } else {
                positionOffset -= itemCount
            }
        }
    }

    fun onItemsMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (DpadRecyclerView.DEBUG) {
            Log.i(
                DpadRecyclerView.TAG, "onItemsMoved $itemCount fromPosition $fromPosition, " +
                        "toPosition: $toPosition, totalItems: ${layoutManager.itemCount}"
            )
        }
        if (position != RecyclerView.NO_POSITION && positionOffset != Int.MIN_VALUE) {
            val finalPosition = position + positionOffset
            if (fromPosition <= finalPosition && finalPosition < fromPosition + itemCount) {
                // moved items include focused position
                positionOffset += toPosition - fromPosition
            } else if (fromPosition < finalPosition && toPosition > finalPosition - itemCount) {
                // move items before focused position to after focused position
                positionOffset -= itemCount
            } else if (fromPosition > finalPosition && toPosition < finalPosition) {
                // move items after focused position to before focused position
                positionOffset += itemCount
            }
        }
    }

    fun clear() {
        val hadPivot = position != RecyclerView.NO_POSITION
        position = RecyclerView.NO_POSITION
        subPosition = 0
        positionOffset = 0
        if (hadPivot) {
            dispatchViewHolderSelected()
            dispatchViewHolderSelectedAndAligned()
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

        if (viewHolder !== selectedViewHolder) {
            selectedViewHolder?.onViewHolderDeselected()
            if (viewHolder is DpadViewHolder) {
                selectedViewHolder = viewHolder
                viewHolder.onViewHolderSelected()
            } else {
                selectedViewHolder = null
            }
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

        if (viewHolder is DpadViewHolder) {
            viewHolder.onViewHolderSelectedAndAligned()
        }

        if (!hasSelectionListeners()) {
            return
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

    fun addOnViewHolderFocusedListener(listener: OnViewFocusedListener) {
        focusListeners.add(listener)
    }

    fun removeOnViewHolderFocusedListener(listener: OnViewFocusedListener) {
        focusListeners.remove(listener)
    }

    fun clearOnViewHolderFocusedListeners() {
        focusListeners.clear()
    }

    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
    }

    fun removeCurrentViewHolderSelection(clearSelection: Boolean) {
        if (clearSelection) {
            position = 0
            subPosition = 0
            positionOffset = 0
        }
        selectedViewHolder?.onViewHolderDeselected()
        selectedViewHolder = null
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

}
