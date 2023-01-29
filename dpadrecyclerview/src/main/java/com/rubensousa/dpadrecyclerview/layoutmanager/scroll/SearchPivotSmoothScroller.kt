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

package com.rubensousa.dpadrecyclerview.layoutmanager.scroll

import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import kotlin.math.sqrt

/**
 * Smooth scrolls until there's a pivot we can focus or if we reach an edge of the list.
 *
 * This smooth scroller will always be triggered whenever we haven't laid out an edge of the list.
 * For this reason, we need to report whenever a new child is laid out to determine
 * if we need to stop scrolling.
 */
internal class SearchPivotSmoothScroller(
    recyclerView: RecyclerView,
    maxPendingMoves: Int,
    private val layoutInfo: LayoutInfo,
    private val pivotSelector: PivotSelector,
    private val alignment: LayoutAlignment,
    private val listener: Listener
) : LinearSmoothScroller(recyclerView.context) {

    companion object {
        // Forces smooth scroller to run until target is actually set
        const val UNDEFINED_TARGET = -2
    }

    private var isCanceled = false
    private val movements = PendingScrollMovements(maxPendingMoves, layoutInfo)

    init {
        targetPosition = UNDEFINED_TARGET
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        val smoothScrollSpeedFactor = layoutInfo.getConfiguration().smoothScrollSpeedFactor
        return super.calculateSpeedPerPixel(displayMetrics) * smoothScrollSpeedFactor
    }

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        val scrollOffset = alignment.calculateScrollOffset(targetView, 0)
        // Check if we don't need to scroll
        if (scrollOffset == 0) {
            return
        }
        var dx = 0
        var dy = 0
        if (layoutInfo.isHorizontal()) {
            dx = scrollOffset
        } else {
            dy = scrollOffset
        }
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
        val time = calculateTimeForDeceleration(distance)
        action.update(dx, dy, time, mDecelerateInterpolator)
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (movements.pendingMoves == 0) {
            return null
        }
        val direction = if (movements.pendingMoves < 0) -1f else 1f
        return if (layoutInfo.isHorizontal()) {
            PointF(direction, 0f)
        } else {
            PointF(0f, direction)
        }
    }

    fun onChildCreated(child: View) {
        // Do nothing if we don't have pending moves or if we have multiple spans
        if (layoutInfo.isGrid() || !movements.hasPendingMoves()) {
            return
        }
        val viewHolder = layoutInfo.getChildViewHolder(child) ?: return
        val adapterPosition = viewHolder.absoluteAdapterPosition
        if (layoutInfo.isViewFocusable(child)
            && adapterPosition != RecyclerView.NO_POSITION
            && movements.shouldScrollToView(adapterPosition, pivotSelector.position)
            && movements.consume()
        ) {
            listener.onPivotAttached(adapterPosition)
        }
    }

    fun onChildLaidOut(view: View) {
        if (layoutInfo.isGrid()) {
            consumeGridMoves()
        } else {
            val viewHolder = layoutInfo.getChildViewHolder(view)
            if (viewHolder?.absoluteAdapterPosition == pivotSelector.position) {
                listener.onPivotLaidOut(view)
            }
        }
        if (movements.shouldStopScrolling() && isRunning) {
            targetPosition = pivotSelector.position
            stop()
        }
    }

    private fun consumeGridMoves() {
        if (!movements.hasPendingMoves()) {
            return
        }
        val currentPosition = pivotSelector.position
        var focusedColumn = if (currentPosition != RecyclerView.NO_POSITION) {
            layoutInfo.getStartColumnIndex(currentPosition)
        } else {
            RecyclerView.NO_POSITION
        }
        var targetPosition = currentPosition
        var targetView: View? = null

        // Start searching for the new view in the same column
        val childCount = layoutInfo.getChildCount()
        var i = 0
        val moves = movements.pendingMoves
        while (i < childCount && moves != 0) {
            val childIndex = if (moves > 0) {
                i
            } else {
                childCount - 1 - i
            }
            i++
            val child = layoutInfo.getChildAt(childIndex)
            if (child == null || !layoutInfo.isViewFocusable(child)) {
                continue
            }
            val childPosition = layoutInfo.getAdapterPositionOf(child)
            val columnIndex = layoutInfo.getStartColumnIndex(childPosition)
            if (focusedColumn == RecyclerView.NO_POSITION) {
                targetPosition = childPosition
                targetView = child
                focusedColumn = columnIndex
            } else if (columnIndex == focusedColumn) {
                // TODO Support different span sizes
                targetPosition = childPosition
                targetView = child
                movements.consume()
            }
        }

        if (targetView != null) {
            pivotSelector.update(position = targetPosition, subPosition = 0)
            listener.onPivotLaidOut(targetView)
        }
    }

    fun addScrollMovement(forward: Boolean) {
        if (forward) {
            movements.increase()
        } else {
            movements.decrease()
        }
    }

    fun cancel() {
        isCanceled = true
    }

    override fun onStop() {
        super.onStop()
        if (isCanceled) {
            listener.onSmoothScrollerStopped()
            return
        }

        movements.clear()

        val pivotView = findViewByPosition(targetPosition)
        if (pivotView != null) {
            listener.onPivotFound(pivotView)
        } else {
            listener.onPivotNotFound(targetPosition)
        }

        listener.onSmoothScrollerStopped()
    }

    interface Listener {
        fun onPivotAttached(adapterPosition: Int)
        fun onPivotFound(pivotView: View)
        fun onPivotLaidOut(pivotView: View)
        fun onPivotNotFound(targetPosition: Int)
        fun onSmoothScrollerStopped()
    }

}
