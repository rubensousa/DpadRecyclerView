/*
 * Copyright 2023 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

internal class LayoutPrefetchCollector(private val layoutInfo: LayoutInfo) {

    fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        prefetchItemCount: Int,
        pivotPosition: Int,
        layoutPrefetchRegistry: RecyclerView.LayoutManager.LayoutPrefetchRegistry
    ) {
        if (prefetchItemCount == 0 || adapterItemCount == 0) {
            // Nothing to prefetch, exit
            return
        }

        // Prefetch items centered around the pivot
        val prefetchStartPosition = max(
            0, min(
                pivotPosition - (prefetchItemCount - 1) / 2,
                adapterItemCount - prefetchItemCount
            )
        )
        var i = prefetchStartPosition
        while (i < adapterItemCount && i < prefetchStartPosition + prefetchItemCount) {
            layoutPrefetchRegistry.addPosition(i, 0)
            i++
        }
    }

    fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State,
        layoutPrefetchRegistry: RecyclerView.LayoutManager.LayoutPrefetchRegistry
    ) {
        val offset = if (layoutInfo.isVertical()) dy else dx
        if (layoutInfo.getChildCount() == 0 || offset == 0) {
            return
        }
        val layoutDirection = calculateLayoutDirection(offset)
        val itemDirection = calculateItemDirection(layoutDirection)
        val edgeView = getViewAtLayoutEdge(layoutDirection) ?: return

        var nextLayoutPosition = layoutInfo.getLayoutPositionOf(edgeView) + itemDirection.value
        val spanCount = layoutInfo.getSpanCount()
        var remainingSpans = spanCount
        val availableScrollSpace = calculateAvailableScrollSpace(edgeView, layoutDirection)
        var count = 0

        while (count < spanCount
            && hasMoreItemsToLayout(nextLayoutPosition, state)
            && remainingSpans > 0
        ) {
            layoutPrefetchRegistry.addPosition(nextLayoutPosition, max(0, availableScrollSpace))
            remainingSpans -= layoutInfo.getSpanSize(nextLayoutPosition)
            nextLayoutPosition += itemDirection.value
            count++
        }
    }

    private fun getViewAtLayoutEdge(layoutDirection: LayoutRequest.LayoutDirection): View? {
        return if (layoutDirection == LayoutRequest.LayoutDirection.END) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
    }

    private fun calculateAvailableScrollSpace(
        view: View,
        layoutDirection: LayoutRequest.LayoutDirection
    ): Int {
        return if (layoutDirection == LayoutRequest.LayoutDirection.END) {
            layoutInfo.getDecoratedEnd(view) - layoutInfo.getEndAfterPadding()
        } else {
            -layoutInfo.getDecoratedStart(view) + layoutInfo.getStartAfterPadding()
        }
    }

    private fun calculateItemDirection(
        layoutDirection: LayoutRequest.LayoutDirection
    ): LayoutRequest.ItemDirection {
        var itemDirection = if (layoutDirection == LayoutRequest.LayoutDirection.END) {
            LayoutRequest.ItemDirection.TAIL
        } else {
            LayoutRequest.ItemDirection.HEAD
        }
        if (layoutInfo.shouldReverseLayout()) {
            itemDirection = itemDirection.opposite()
        }
        return itemDirection
    }

    private fun calculateLayoutDirection(scrollOffset: Int): LayoutRequest.LayoutDirection {
        return if (scrollOffset > 0) {
            LayoutRequest.LayoutDirection.END
        } else {
            LayoutRequest.LayoutDirection.START
        }
    }

    private fun hasMoreItemsToLayout(position: Int, state: RecyclerView.State): Boolean {
        return position >= 0 && position < state.itemCount
    }

}
