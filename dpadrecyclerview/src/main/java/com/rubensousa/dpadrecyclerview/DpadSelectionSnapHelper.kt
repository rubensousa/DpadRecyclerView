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

import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller.ScrollVectorProvider
import androidx.recyclerview.widget.SnapHelper
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutManager
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * A [SnapHelper] that scrolls Views to their alignment configuration
 * and performs selections automatically.
 * Use this only if you need to support touch event handling,
 * as [DpadRecyclerView] by default does not handle selection on touch events.
 * @param updateSelectionOnScrollChanges true if selection should change based
 * on the scrolling target view, false otherwise
 */
class DpadSelectionSnapHelper(
    private val updateSelectionOnScrollChanges: Boolean = true
) : LinearSnapHelper() {

    private val maxScrollOnFlingDurationMs = 500
    private val millisecondsPerInch = 100f
    private var currentRecyclerView: DpadRecyclerView? = null

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
        if (recyclerView is DpadRecyclerView) {
            currentRecyclerView = recyclerView
            return
        }
        currentRecyclerView = null
        if (recyclerView != null) {
            throw IllegalArgumentException("Only DpadRecyclerView can be used with DpadSnapHelper")
        }
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager, targetView: View
    ): IntArray {
        val distance = intArrayOf(0, 0)
        if (layoutManager !is PivotLayoutManager) {
            return distance
        }
        val scrollOffset = layoutManager.getScrollOffset(targetView)
        if (updateSelectionOnScrollChanges) {
            layoutManager.select(targetView)
        }
        if (layoutManager.isHorizontal()) {
            distance[0] = scrollOffset
        } else {
            distance[1] = scrollOffset
        }
        return distance
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        if (layoutManager !is PivotLayoutManager) {
            return null
        }
        var nearestView: View? = null
        var nearestOffset: Int = Int.MAX_VALUE
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i) ?: continue
            val offset = abs(layoutManager.getScrollOffset(child))
            if (offset < nearestOffset && child.hasFocusable()) {
                nearestOffset = offset
                nearestView = child
            }
        }
        return nearestView
    }

    override fun createScroller(
        layoutManager: RecyclerView.LayoutManager
    ): RecyclerView.SmoothScroller? {
        val recyclerView = currentRecyclerView ?: return null
        if (layoutManager !is ScrollVectorProvider) {
            return null
        }
        return object : LinearSmoothScroller(recyclerView.context) {

            override fun onTargetFound(
                targetView: View, state: RecyclerView.State, action: Action
            ) {
                val snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(
                    max(abs(dx.toDouble()), abs(dy.toDouble())).toInt()
                )
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return millisecondsPerInch / displayMetrics.densityDpi
            }

            override fun calculateTimeForScrolling(dx: Int): Int {
                return min(
                    maxScrollOnFlingDurationMs.toDouble(),
                    super.calculateTimeForScrolling(dx).toDouble()
                ).toInt()
            }
        }
    }

}
