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

class DpadDragHelper(
    private val callback: DragCallback,
    private val cancelKeyCodes: Set<Int> = setOf(
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_BACK
    )
) {

    private var currentRecyclerView: DpadRecyclerView? = null
    private var isDragging: Boolean = false
    private var selectedViewHolder: RecyclerView.ViewHolder? = null
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

    fun attachRecyclerView(recyclerView: DpadRecyclerView) {
        if (currentRecyclerView === recyclerView) {
            return
        }
        detachFromRecyclerView()
        currentRecyclerView = recyclerView
    }

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
                "RecyclerView not attached. Please use attachRecyclerView before calling startDrag"
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
        selectedViewHolder = viewHolder
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
        selectedViewHolder = null
        isDragging = false
        callback.onDragStopped()
        currentRecyclerView?.requestLayout()
    }

    private fun onKeyEvent(event: KeyEvent): Boolean {
        val recyclerView = currentRecyclerView ?: return false
        val direction = getFocusDirection(event) ?: return false
        val view = recyclerView.focusSearch(direction) ?: return false
        val viewHolder = recyclerView.findContainingViewHolder(view) ?: return false
        selectedViewHolder?.let { srcViewHolder ->
            return callback.move(src = srcViewHolder, target = viewHolder)
        }
        return false
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

    interface DragCallback {

        /**
         * @return true if [src] should be moved to [target]'s position,
         * false to keep the original positions
         */
        fun move(
            src: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean

        /**
         * Indicates that the dragging action has started. [DpadRecyclerView] will receive focus
         *
         * @param viewHolder the view holder that is being dragged
         */
        fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {}

        /**
         * Indicates that the dragging action has stopped
         */
        fun onDragStopped() {}
    }

}
