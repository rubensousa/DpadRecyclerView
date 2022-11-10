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

package com.rubensousa.dpadrecyclerview.internal.layout

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.DpadLayoutParams
import kotlin.math.max
import kotlin.math.min

internal class LayoutArchitect(
    private val layoutManager: LayoutManager,
    private val configuration: TvLayoutConfiguration,
    private val selectionState: TvSelectionState,
    private val layoutInfo: TvLayoutInfo
) {

    private val rowArchitect = RowArchitect(configuration, layoutManager, layoutInfo)
    private var dpadRecyclerView: RecyclerView? = null

    fun setRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
    }

    fun checkLayoutParams(layoutParams: RecyclerView.LayoutParams?): Boolean {
        return layoutParams is DpadLayoutParams
    }

    fun generateLayoutParams(context: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        return DpadLayoutParams(context, attrs)
    }

    fun generateLayoutParams(layoutParams: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return when (layoutParams) {
            is DpadLayoutParams -> DpadLayoutParams(layoutParams)
            is RecyclerView.LayoutParams -> DpadLayoutParams(layoutParams)
            is ViewGroup.MarginLayoutParams -> DpadLayoutParams(layoutParams)
            else -> DpadLayoutParams(layoutParams)
        }
    }

    fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return if (configuration.isHorizontal()) {
            DpadLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            DpadLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    // TODO
    fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        // If we don't have any items, recycle them all
        if (state.itemCount == 0) {
            layoutManager.removeAndRecycleAllViews(recycler)
            return
        }

        // Detach all existing views
        layoutManager.detachAndScrapAttachedViews(recycler)

        // TODO Use specific parent keyline alignment + child alignment
        val keyline: Int
        if (configuration.isHorizontal()) {
            keyline = layoutManager.width / 2
        } else {
            keyline = layoutManager.height / 2
        }

        // TODO Use extraSpace configuration instead
        val layoutLimit = keyline * 2
        val anchorPosition = selectionState.position
        rowArchitect.layout(anchorPosition, keyline, layoutLimit, state.itemCount, recycler)

        if (!state.isPreLayout) {
            layoutInfo.update()
        }
    }

    // TODO
    fun onLayoutCompleted(state: RecyclerView.State?) {

    }

    // TODO
    fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State?,
        layoutPrefetchRegistry: LayoutManager.LayoutPrefetchRegistry
    ) {

    }

    fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: LayoutManager.LayoutPrefetchRegistry
    ) {
        val prefetchCount: Int = configuration.initialPrefetchItemCount
        if (adapterItemCount != 0 && prefetchCount != 0) {
            // Prefetch items centered around the selected position
            val initialPosition = max(
                0, min(
                    selectionState.position - (prefetchCount - 1) / 2,
                    adapterItemCount - prefetchCount
                )
            )
            var i = initialPosition
            while (i < adapterItemCount && i < initialPosition + prefetchCount) {
                layoutPrefetchRegistry.addPosition(i, 0)
                i++
            }
        }
    }

    fun getDecoratedLeft(child: View, decoratedLeft: Int): Int {
        return decoratedLeft + getLayoutParams(child).leftInset
    }

    fun getDecoratedTop(child: View, decoratedTop: Int): Int {
        return decoratedTop + getLayoutParams(child).topInset
    }

    fun getDecoratedRight(child: View, decoratedRight: Int): Int {
        return decoratedRight - getLayoutParams(child).rightInset
    }

    fun getDecoratedBottom(child: View, decoratedBottom: Int): Int {
        return decoratedBottom - getLayoutParams(child).bottomInset
    }

    fun getDecoratedBoundsWithMargins(view: View, outBounds: Rect) {
        val params = view.layoutParams as DpadLayoutParams
        outBounds.left += params.leftInset
        outBounds.top += params.topInset
        outBounds.right -= params.rightInset
        outBounds.bottom -= params.bottomInset
    }

    private fun getLayoutParams(child: View): DpadLayoutParams {
        return child.layoutParams as DpadLayoutParams
    }


}
