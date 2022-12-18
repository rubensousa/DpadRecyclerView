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
    private val recyclerView: RecyclerView,
    private val layoutInfo: LayoutInfo,
    private val pivotSelector: PivotSelector,
    private val alignment: LayoutAlignment,
    private val listener: Listener
) : LinearSmoothScroller(recyclerView.context) {

    companion object {
        // Forces smooth scroller to run until target is actually set
        const val UNDEFINED_TARGET = -2

        private const val TAG = "SearchPivotScroller"
    }

    private var isCanceled = false
    private val movements = ScrollMovements(layoutInfo)

    init {
        targetPosition = UNDEFINED_TARGET
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        val smoothScrollSpeedFactor = layoutInfo.getConfiguration().smoothScrollSpeedFactor
        return super.calculateSpeedPerPixel(displayMetrics) * smoothScrollSpeedFactor
    }

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        Log.i(TAG, "onTargetFound")
        val scrollOffset = alignment.calculateScrollOffset(recyclerView, targetView, 0)
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
        if (movements.getPendingMoves() == 0) {
            return null
        }
        val direction = if (movements.getPendingMoves() < 0) -1f else 1f
        return if (layoutInfo.isHorizontal()) {
            PointF(direction, 0f)
        } else {
            PointF(0f, direction)
        }
    }

    fun onChildCreated(child: View) {
        val viewHolder = layoutInfo.getChildViewHolder(child) ?: return
        val adapterPosition = viewHolder.absoluteAdapterPosition
        Log.i(TAG, "View attached: $adapterPosition")
        if (layoutInfo.isViewFocusable(child)
            && adapterPosition != RecyclerView.NO_POSITION
            && movements.consume()
        ) {
            listener.onPivotAttached(adapterPosition)
        }
    }

    fun onChildLaidOut(view: View) {
        val viewHolder = layoutInfo.getChildViewHolder(view)
        if (viewHolder?.absoluteAdapterPosition == pivotSelector.position) {
            listener.onPivotLaidOut(view)
        }
        if (movements.shouldStopScrolling() && isRunning) {
            Log.i(TAG, "Requested stop scrolling")
            targetPosition = pivotSelector.position
            stop()
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
        Log.i(TAG, "onStop")
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
