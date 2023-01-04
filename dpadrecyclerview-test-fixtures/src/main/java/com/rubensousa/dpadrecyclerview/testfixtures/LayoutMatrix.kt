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

package com.rubensousa.dpadrecyclerview.testfixtures

import androidx.collection.CircularArray
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import kotlin.math.max

abstract class LayoutMatrix(
    protected val config: LayoutConfig
) {
    var selectedPosition = RecyclerView.NO_POSITION
        private set

    protected val layoutRequest = LayoutRequest()
    private var itemCount: Int = config.defaultItemCount
    private val views = CircularArray<ViewItem>()
    private var extraLayoutSpaceStart = 0
    private var extraLayoutSpaceEnd = 0

    abstract fun isVertical(): Boolean
    abstract fun layoutBlock(request: LayoutRequest): LayoutResult
    abstract fun scrollBy(offset: Int)
    abstract fun getLayoutStartOffset(): Int
    abstract fun getLayoutEndOffset(): Int
    abstract fun getViewCenter(view: ViewItem): Int

    /**
     * Starts the layout with the view at [position] aligned in its final location
     */
    fun init(position: Int) {
        clear()
        selectedPosition = position

        val view = layoutPivot(position)

        // Layout from the pivot until the start limit of the matrix
        layoutFromPivotToStart(view)

        // Layout from the pivot until the end limit of the matrix
        layoutFromPivotToEnd(view)
    }

    fun getItemCount() = itemCount

    fun setExtraLayoutSpace(start: Int = 0, end: Int = 0) {
        if (start != extraLayoutSpaceStart) {
            val increased = start > extraLayoutSpaceStart
            extraLayoutSpaceStart = start
            if (increased) {
                layoutExtraStart()
            } else {
                recycleStart()
            }
        }

        if (end != extraLayoutSpaceEnd) {
            val increased = end > extraLayoutSpaceEnd
            extraLayoutSpaceEnd = end
            if (increased) {
                layoutExtraEnd()
            } else {
                recycleEnd()
            }
        }
    }

    fun getExtraLayoutSpaceStart() = extraLayoutSpaceStart

    fun getExtraLayoutSpaceEnd() = extraLayoutSpaceEnd

    fun getFirstView(): ViewItem? {
        if (views.isEmpty) {
            return null
        }
        return views.first
    }

    fun getLastView(): ViewItem? {
        if (views.isEmpty) {
            return null
        }
        return views.last
    }

    fun getViewAt(index: Int): ViewItem {
        return views.get(index)
    }

    fun getChildCount(): Int {
        return views.size()
    }

    fun getChildren(): List<ViewItem> {
        return List(views.size()) { index -> views.get(index) }
    }

    fun getSize(): Int {
        return if (isVertical()) {
            config.parentHeight
        } else {
            config.parentWidth
        }
    }

    fun assertViewBounds(bounds: List<ViewBounds>) {
        assertThat(getChildCount()).isEqualTo(bounds.size)
        val actualBounds = getChildren().map { it.bounds }
        assertThat(actualBounds).isEqualTo(bounds)
    }

    protected fun updateSelectedPosition(position: Int) {
        selectedPosition = position
    }

    protected fun offsetChildren(offset: Int) {
        views.forEach { view ->
            if (isVertical()) {
                view.offsetVertically(offset)
            } else {
                view.offsetHorizontally(offset)
            }
        }
    }

    private fun layoutPivot(pivotPosition: Int): ViewItem {
        layoutRequest.reset()
        layoutRequest.apply {
            setTowardsEnd()
            position = pivotPosition
            checkpoint = 0
            space = 1
        }
        val view = layoutBlock(layoutRequest).views.first()
        views.addFirst(view)

        // Align the pivot using the alignment configuration
        val scrollOffset = getViewCenter(view) - config.parentKeyline
        offsetChildren(-scrollOffset)

        return view
    }

    private fun layoutExtraStart() {
        val firstView = getFirstView() ?: return
        layoutRequest.reset()
        layoutRequest.apply {
            setTowardsStart()
            position = firstView.position - 1
            checkpoint = getLayoutStartOffset()
            space = max(0, checkpoint + extraLayoutSpaceStart)
        }
        fill(layoutRequest)
    }

    private fun layoutExtraEnd() {
        val lastView = getLastView() ?: return
        layoutRequest.reset()
        layoutRequest.apply {
            setTowardsEnd()
            position = lastView.position + 1
            checkpoint = getLayoutEndOffset()
            space = max(0, getVisibleSpace() - checkpoint + extraLayoutSpaceEnd)
        }
        fill(layoutRequest)
    }

    private fun layoutFromPivotToStart(pivotView: ViewItem) {
        layoutRequest.reset()
        layoutRequest.apply {
            setTowardsStart()
            position = pivotView.position - 1
            checkpoint = getDecoratedStart(pivotView)
            space = max(0, checkpoint + extraLayoutSpaceStart)
        }
        fill(layoutRequest)
    }

    private fun layoutFromPivotToEnd(pivotView: ViewItem) {
        layoutRequest.reset()
        layoutRequest.apply {
            setTowardsEnd()
            position = pivotView.position + 1
            checkpoint = getDecoratedEnd(pivotView)
            space = max(0, getVisibleSpace() - checkpoint + extraLayoutSpaceEnd)
        }
        fill(layoutRequest)
    }

    protected fun fill(request: LayoutRequest) {
        var remainingSpace = request.space
        while (canContinueLayout(remainingSpace, request)) {
            val result = layoutBlock(request)
            if (request.isTowardsEnd()) {
                request.checkpoint += result.consumedSpace
            } else {
                request.checkpoint -= result.consumedSpace
            }
            if (!result.skipConsumption) {
                remainingSpace -= result.consumedSpace
            }
            result.views.forEach { view ->
                if (request.isTowardsEnd()) {
                    views.addLast(view)
                } else {
                    views.addFirst(view)
                }
            }
        }
    }

    private fun clear() {
        views.clear()
        extraLayoutSpaceEnd = 0
        extraLayoutSpaceStart = 0
        selectedPosition = RecyclerView.NO_POSITION
    }

    private fun canContinueLayout(remainingSpace: Int, request: LayoutRequest): Boolean {
        return remainingSpace > 0 && request.position >= 0 && request.position < itemCount
    }

    protected fun getDecoratedStart(view: ViewItem): Int {
        return if (isVertical()) {
            view.getDecoratedTop()
        } else {
            view.getDecoratedLeft()
        }
    }

    protected fun getDecoratedEnd(view: ViewItem): Int {
        return if (isVertical()) {
            view.getDecoratedBottom()
        } else {
            view.getDecoratedRight()
        }
    }

    private fun getVisibleSpace(): Int {
        return if (isVertical()) {
            config.parentHeight
        } else {
            config.parentWidth
        }
    }

    protected fun recycleStart() {
        val limit = -extraLayoutSpaceStart
        val childCount = getChildCount()
        for (i in 0 until childCount) {
            val child = getViewAt(i)
            if (getDecoratedEnd(child) > limit) {
                views.removeFromStart(i)
                return
            }
        }
    }

    protected fun recycleEnd() {
        val limit = getVisibleSpace() + extraLayoutSpaceEnd
        val childCount = getChildCount()
        for (i in childCount - 1 downTo 0) {
            val child = getViewAt(i)
            if (getDecoratedStart(child) < limit) {
                val numberOfViews = childCount - 1 - i
                views.removeFromEnd(numberOfViews)
                return
            }
        }
    }

    private inline fun CircularArray<ViewItem>.forEach(action: (item: ViewItem) -> Unit) {
        for (i in 0 until size()) {
            action(get(i))
        }
    }

}
