/*
 * Copyright 2024 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview

import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

/**
 * A helper class for re-ordering the contents of a [DpadRecyclerView].
 *
 * To use this, your adapter needs to implement [DpadDragHelper.DragAdapter]
 * and expose the mutable collection via [DpadDragHelper.DragAdapter.getMutableItems].
 */
class DpadDragHelper<T>(
    private val adapter: DragAdapter<T>,
    private val callback: DragCallback,
    private val cancelKeyCodes: Set<Int> = setOf(
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_BACK
    )
) {

    /**
     * True if the attached [DpadRecyclerView] is currently in drag mode, false otherwise
     */
    var isDragging: Boolean = false
        private set

    private var currentRecyclerView: DpadRecyclerView? = null
    private var previousKeyInterceptListener: DpadRecyclerView.OnKeyInterceptListener? = null
    private var keyInterceptListener = object : DpadRecyclerView.OnKeyInterceptListener {
        override fun onInterceptKeyEvent(event: KeyEvent): Boolean {
            if (!isDragging) {
                return false
            }
            if (cancelKeyCodes.contains(event.keyCode)) {
                stopDrag()
                return true
            }
            if (event.action == KeyEvent.ACTION_UP) {
                return false
            }
            return onKeyEvent(event)
        }
    }

    /**
     * Attaches the [DpadRecyclerView] that will be dragged.
     * This is required before calling [startDrag]
     */
    fun attachToRecyclerView(recyclerView: DpadRecyclerView) {
        if (currentRecyclerView === recyclerView) {
            return
        }
        detachFromRecyclerView()
        currentRecyclerView = recyclerView
    }

    /**
     * Detaches the previously attached [DpadRecyclerView] and stops dragging
     */
    fun detachFromRecyclerView() {
        stopDrag()
        currentRecyclerView = null
    }


    /**
     * Starts the dragging action for the ViewHolder at [position].
     *
     * [DragCallback.onDragStarted] will be called after this method if this returns true
     *
     * @param position the position of the item to be dragged
     * @return true if the dragging action was started, false otherwise
     */
    fun startDrag(position: Int): Boolean {
        if (isDragging) {
            return true
        }
        val recyclerView = currentRecyclerView
            ?: throw IllegalStateException(
                "RecyclerView not attached. Please use attachToRecyclerView before calling startDrag"
            )
        val adapter = recyclerView.adapter ?: return false
        if (position < 0 || position >= adapter.itemCount) {
            return false
        }
        if (recyclerView.getSelectedPosition() != position) {
            recyclerView.setSelectedPosition(position, object : ViewHolderTask() {
                override fun execute(viewHolder: RecyclerView.ViewHolder) {
                    startDrag(recyclerView, viewHolder)
                }
            })
            return true
        } else {
            recyclerView.findViewHolderForAdapterPosition(position)?.let {
                startDrag(recyclerView, it)
                return true
            }
        }
        return false
    }

    private fun startDrag(
        recyclerView: DpadRecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ) {
        isDragging = true
        previousKeyInterceptListener = recyclerView.getOnKeyInterceptListener()
        recyclerView.setOnKeyInterceptListener(keyInterceptListener)
        recyclerView.isFocusable = true
        recyclerView.isFocusableInTouchMode = true
        recyclerView.requestFocus()
        callback.onDragStarted(viewHolder)
    }

    /**
     * Cancels the current ongoing dragging action
     * [DragCallback.onDragStopped] will be called after this method
     */
    fun stopDrag() {
        if (!isDragging) {
            return
        }
        currentRecyclerView?.let { recyclerView ->
            previousKeyInterceptListener?.let { listener ->
                recyclerView.setOnKeyInterceptListener(listener)
            }
        }
        isDragging = false
        callback.onDragStopped()
    }

    private fun onKeyEvent(event: KeyEvent): Boolean {
        val recyclerView = currentRecyclerView ?: return false
        val direction = getFocusDirection(event) ?: return false
        val view = recyclerView.focusSearch(direction) ?: return false
        val targetViewHolder = recyclerView.findContainingViewHolder(view) ?: return false
        val selectedViewHolder = recyclerView.findViewHolderForAdapterPosition(
            recyclerView.getSelectedPosition()
        ) ?: return false

        if (selectedViewHolder.absoluteAdapterPosition == RecyclerView.NO_POSITION
            || targetViewHolder.absoluteAdapterPosition == RecyclerView.NO_POSITION
        ) {
            return false
        }
        val currentAdapter = recyclerView.adapter ?: return false
        if (recyclerView.getSpanCount() == 1) {
            moveLinear(
                adapter = currentAdapter,
                items = adapter.getMutableItems(),
                srcIndex = selectedViewHolder.bindingAdapterPosition,
                targetIndex = targetViewHolder.bindingAdapterPosition
            )
        } else {
            moveGrid(
                adapter = currentAdapter,
                items = adapter.getMutableItems(),
                srcIndex = selectedViewHolder.bindingAdapterPosition,
                targetIndex = targetViewHolder.bindingAdapterPosition
            )
        }
        return true
    }

    private fun getFocusDirection(event: KeyEvent): Int? {
        return when (event.keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> View.FOCUS_LEFT
            KeyEvent.KEYCODE_DPAD_RIGHT -> View.FOCUS_RIGHT
            KeyEvent.KEYCODE_DPAD_UP -> View.FOCUS_UP
            KeyEvent.KEYCODE_DPAD_DOWN -> View.FOCUS_DOWN
            else -> null
        }
    }

    /**
     * A linear move just swaps positions
     */
    private fun moveLinear(
        adapter: RecyclerView.Adapter<*>,
        items: MutableList<T>,
        srcIndex: Int,
        targetIndex: Int
    ) {
        Collections.swap(items, srcIndex, targetIndex)
        adapter.notifyItemMoved(srcIndex, targetIndex)
    }

    /**
     * A grid move needs to remove the element at the given position
     * and insert it in the new position, otherwise order is not kept
     */
    private fun moveGrid(
        adapter: RecyclerView.Adapter<*>,
        items: MutableList<T>,
        srcIndex: Int,
        targetIndex: Int
    ) {
        val item = items.removeAt(srcIndex)
        items.add(targetIndex, item)
        adapter.notifyItemMoved(srcIndex, targetIndex)
        /**
         * Now notify the range that was affected
         * If src < target -> notify all indexes from src until target
         * If src > target -> notify all indexes from target until src
         */
        if (srcIndex < targetIndex) {
            adapter.notifyItemRangeChanged(srcIndex, targetIndex - srcIndex)
        } else {
            adapter.notifyItemRangeChanged(targetIndex, srcIndex - targetIndex)
        }
    }

    interface DragAdapter<T> {
        /**
         * @return the mutable collection of items backing the adapter
         */
        fun getMutableItems(): MutableList<T>
    }

    interface DragCallback {

        /**
         * Indicates that the dragging action has started. [DpadRecyclerView] will receive focus
         *
         * @param viewHolder the view holder that is being dragged
         */
        fun onDragStarted(viewHolder: RecyclerView.ViewHolder)

        /**
         * Indicates that the dragging action has stopped
         */
        fun onDragStopped()
    }

}
