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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotSelector
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.SpanFocusFinder
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
    layoutInfo: LayoutInfo,
    private val spanFocusFinder: SpanFocusFinder,
    private val pivotSelector: PivotSelector,
    private val alignment: LayoutAlignment,
    private val listener: Listener
) : BaseSmoothScroller(recyclerView, layoutInfo) {

    companion object {
        // Forces smooth scroller to run until target is actually set
        const val UNDEFINED_TARGET = -2
    }

    private val movements = PendingScrollMovements(maxPendingMoves, layoutInfo)

    init {
        targetPosition = UNDEFINED_TARGET
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
            return
        }
        val viewHolder = layoutInfo.getChildViewHolder(view)
        if (viewHolder?.absoluteAdapterPosition == pivotSelector.position) {
            listener.onPivotLaidOut(view)
        }
        if (movements.shouldStopScrolling()) {
            targetPosition = pivotSelector.position
            stop()
        }
    }

    fun consumeOneMovement() {
        val targetView = if (layoutInfo.isGrid()) {
            consumeGridMovements(pivotSelector.position, spanFocusFinder, consumeAll = false)
        } else {
            consumeOneLinearMovement(pivotSelector.position)
        }
        if (targetView != null) {
            listener.onPivotAttached(layoutInfo.getAdapterPositionOf(targetView))
            listener.onPivotLaidOut(targetView)
        }
    }

    fun onBlockLaidOut() {
        if (!layoutInfo.isGrid()) {
            return
        }
        val newPivotView = consumeGridMovements(pivotSelector.position, spanFocusFinder)
        if (newPivotView != null) {
            listener.onPivotAttached(layoutInfo.getAdapterPositionOf(newPivotView))
            listener.onPivotLaidOut(newPivotView)
        }
        if (movements.shouldStopScrolling()) {
            targetPosition = pivotSelector.position
            stop()
        }
    }

    fun addScrollMovement(forward: Boolean) {
        movements.add(forward)
    }

    override fun onStop() {
        super.onStop()
        if (isCanceled()) {
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

    private fun consumeOneLinearMovement(
        pivotPosition: Int
    ): View? {
        if (!movements.hasPendingMoves()) {
            return null
        }
        val reverseLayout = layoutInfo.shouldReverseLayout()
        val isScrollingForwards = if (!reverseLayout) {
            movements.pendingMoves > 0
        } else {
            movements.pendingMoves < 0
        }
        val edgeView = if (isScrollingForwards != reverseLayout) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
        if (edgeView == null) {
            return null
        }
        val edgePosition = layoutInfo.getLayoutPositionOf(edgeView)
        var targetView: View? = null
        val positionIncrement = if (isScrollingForwards) 1 else -1
        var targetPosition = pivotPosition
        while (targetPosition != edgePosition && movements.hasPendingMoves()) {
            val view = layoutInfo.findViewByPosition(targetPosition)
            targetPosition += positionIncrement
            if (view == null || !layoutInfo.isViewFocusable(view)) {
                continue
            }
            targetView = view
            movements.consume()
            break
        }
        return targetView
    }

    private fun consumeGridMovements(
        pivotPosition: Int,
        spanFocusFinder: SpanFocusFinder,
        consumeAll: Boolean = true
    ): View? {
        if (!movements.hasPendingMoves()) {
            return null
        }
        val reverseLayout = layoutInfo.shouldReverseLayout()
        val isScrollingForwards = if (!reverseLayout) {
            movements.pendingMoves > 0
        } else {
            movements.pendingMoves < 0
        }
        val edgeView = if (isScrollingForwards != reverseLayout) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
        if (edgeView == null) {
            return null
        }
        val spanSizeLookup = layoutInfo.getConfiguration().spanSizeLookup
        val edgePosition = layoutInfo.getLayoutPositionOf(edgeView)
        var targetView: View? = null
        var targetPosition: Int = pivotPosition

        while (targetPosition != edgePosition && movements.hasPendingMoves()) {
            targetPosition = spanFocusFinder.findNextSpanPosition(
                focusedPosition = targetPosition,
                spanSizeLookup = spanSizeLookup,
                forward = isScrollingForwards,
                edgePosition = edgePosition,
                reverseLayout = reverseLayout
            )
            if (targetPosition == RecyclerView.NO_POSITION) {
                break
            }
            spanFocusFinder.save(targetPosition, spanSizeLookup)
            val view = layoutInfo.findViewByPosition(targetPosition)
            if (view == null || !layoutInfo.isViewFocusable(view)) {
                continue
            }
            targetView = view
            movements.consume()
            if (!consumeAll) {
                break
            }
        }
        // Reset the focus back to the original position
        if (targetView == null) {
            spanFocusFinder.save(pivotPosition, spanSizeLookup)
        }
        return targetView
    }

    interface Listener {
        fun onPivotAttached(adapterPosition: Int)
        fun onPivotFound(pivotView: View)
        fun onPivotLaidOut(pivotView: View)
        fun onPivotNotFound(targetPosition: Int)
        fun onSmoothScrollerStopped()
    }

}
