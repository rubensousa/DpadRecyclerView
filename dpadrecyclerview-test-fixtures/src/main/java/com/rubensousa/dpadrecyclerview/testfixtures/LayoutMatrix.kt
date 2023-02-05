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
import kotlin.math.min

abstract class LayoutMatrix(val config: LayoutConfig) {
    var selectedPosition = RecyclerView.NO_POSITION
        private set

    protected val layoutRequest = LayoutBlockRequest()
    private var itemCount: Int = config.defaultItemCount
    private val views = CircularArray<ViewItem>()
    private var extraLayoutSpaceStart = 0
    private var extraLayoutSpaceEnd = 0

    abstract fun isVertical(): Boolean
    abstract fun scrollBy(offset: Int)
    abstract fun getLayoutStartOffset(): Int
    abstract fun getLayoutEndOffset(): Int

    protected abstract fun initializeLayout(pivotPosition: Int)
    protected abstract fun layoutBlock(request: LayoutBlockRequest): LayoutBlockResult
    protected abstract fun layoutExtraStart()
    protected abstract fun layoutExtraEnd()
    protected abstract fun getViewCenter(view: ViewItem): Int

    /**
     * Starts the layout with the view at [position] aligned in its final location
     */
    fun init(position: Int) {
        layoutRequest.init(config.reversed)
        clear()
        selectedPosition = position
        initializeLayout(position)
        // Ensure layout starts from the start if needed
        if (config.alignToStartEdge) {
            ensureStartAlignment()
        }
    }

    private fun ensureStartAlignment() {
        val startView = getFirstView() ?: return
        val endView = getLastView() ?: return
        val startEdge = getDecoratedStart(startView)
        val endEdge = getDecoratedEnd(endView)
        val currentLayoutSpace = endEdge - startEdge

        // If the current layout already fills the entire space, skip this
        if (currentLayoutSpace >= getVisibleSpace()) {
            return
        }

        val emptyLayoutSpace = getVisibleSpace() - currentLayoutSpace

        if (!config.reversed) {
            if (startEdge > 0) {
                scrollBy(startEdge)
            } else {
                layoutRequest.prepend(startView.position) {
                    checkpoint = startEdge
                    space = emptyLayoutSpace + startEdge
                }
                val scrollSpace = min(fill(layoutRequest), emptyLayoutSpace)
                scrollBy(startEdge - scrollSpace)
            }
        } else if (endEdge < getVisibleSpace()) {
            val distanceToEnd = getVisibleSpace() - endEdge
            scrollBy(-distanceToEnd)
        } else {
            layoutRequest.append(endView.position) {
                checkpoint = endEdge
                space = emptyLayoutSpace
            }
            val scrollSpace = min(fill(layoutRequest), emptyLayoutSpace)
            val availableScrollSpace = endEdge - getVisibleSpace()
            scrollBy(availableScrollSpace + scrollSpace)
        }
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
        if (views.isEmpty()) {
            return null
        }
        return views.first
    }

    fun getLastView(): ViewItem? {
        if (views.isEmpty()) {
            return null
        }
        return views.last
    }

    fun getViewAt(index: Int): ViewItem {
        return views.get(index)
    }

    fun findViewFromLayoutPosition(position: Int): ViewItem? {
        views.forEach { view ->
            if (view.position == position) {
                return view
            }
        }
        return null
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

    protected fun fill(request: LayoutBlockRequest): Int {
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
        return layoutRequest.space - remainingSpace
    }

    protected fun clear() {
        views.clear()
        extraLayoutSpaceEnd = 0
        extraLayoutSpaceStart = 0
        selectedPosition = RecyclerView.NO_POSITION
    }

    private fun canContinueLayout(remainingSpace: Int, request: LayoutBlockRequest): Boolean {
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

    protected fun getVisibleSpace(): Int {
        return if (isVertical()) {
            config.parentHeight
        } else {
            config.parentWidth
        }
    }

    protected fun append(view: ViewItem) {
        views.addLast(view)
    }

    protected fun prepend(view: ViewItem) {
        views.addFirst(view)
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
