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
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import kotlin.math.sqrt

/**
 * Smooth scrolls until the pivot at [pivotPosition] is laid out and then aligns it
 */
internal class PivotSmoothScroller(
    private val recyclerView: RecyclerView,
    private val pivotPosition: Int,
    private val layoutInfo: LayoutInfo,
    private val alignment: LayoutAlignment,
    private val listener: PivotListener
) : LinearSmoothScroller(recyclerView.context) {

    private var isCanceled = false

    init {
        targetPosition = pivotPosition
    }

    fun cancel() {
        isCanceled = true
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) {
            return null
        }
        val firstChild = recyclerView.getChildAt(0) ?: return null
        val firstChildPosition = layoutInfo.getLayoutPositionOf(firstChild)
        val isStart = if (layoutInfo.isRTL() && layoutInfo.isHorizontal()) {
            targetPosition > firstChildPosition
        } else {
            targetPosition < firstChildPosition
        }
        val direction = if (isStart) -1f else 1f
        return if (layoutInfo.isHorizontal()) {
            PointF(direction, 0f)
        } else {
            PointF(0f, direction)
        }
    }

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        val smoothScrollSpeedFactor = layoutInfo.getConfiguration().smoothScrollSpeedFactor
        return super.calculateSpeedPerPixel(displayMetrics) * smoothScrollSpeedFactor
    }

    override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
        // TODO Handle sub position focus alignment
        val offset = alignment.updateScroll(recyclerView, targetView, null) ?: return
        var dx = 0
        var dy = 0
        if (layoutInfo.isHorizontal()) {
            dx = offset
        } else {
            dy = offset
        }
        val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
        val time = calculateTimeForDeceleration(distance)
        action.update(dx, dy, time, mDecelerateInterpolator)
    }

    override fun onStop() {
        super.onStop()
        if (!isCanceled) {
            dispatchEvents()
        }
    }

    private fun dispatchEvents() {
        val pivotView = findViewByPosition(pivotPosition)
        if (pivotView != null) {
            listener.onPivotFound(pivotView, pivotPosition)
        } else if (pivotPosition >= 0) {
            listener.onPivotNotFound(pivotPosition)
        }
    }

    interface PivotListener {
        fun onPivotFound(targetView: View, pivotPosition: Int)
        fun onPivotNotFound(pivotPosition: Int)
    }

}
