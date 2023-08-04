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

package com.rubensousa.dpadrecyclerview.spacing

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams

/**
 * An item decoration that applies a spacing to all sides of a view part of a grid.
 *
 * @param itemSpacing default spacing between items that share a span group.
 *
 * @param perpendicularItemSpacing spacing between items across different span groups.
 * Default is [itemSpacing] if not specified.
 *
 * @param edgeSpacing spacing between the start and end edges in the layout orientation.
 * Default is [itemSpacing] if not specified.
 */
class DpadGridSpacingDecoration private constructor(
    @Px private val itemSpacing: Int,
    @Px private val perpendicularItemSpacing: Int,
    @Px private val edgeSpacing: Int
) : DpadSpacingDecoration() {

    companion object {

        @JvmStatic
        fun create(
            @Px itemSpacing: Int,
            @Px perpendicularItemSpacing: Int = itemSpacing,
            @Px edgeSpacing: Int = itemSpacing,
        ): DpadGridSpacingDecoration {
            return DpadGridSpacingDecoration(
                itemSpacing = itemSpacing,
                perpendicularItemSpacing = perpendicularItemSpacing,
                edgeSpacing = edgeSpacing
            )
        }

    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        layoutPosition: Int,
        parent: DpadRecyclerView,
        state: RecyclerView.State
    ) {
        val layoutParams = view.layoutParams as DpadLayoutParams
        val spanIndex = layoutParams.spanIndex
        if (spanIndex == DpadLayoutParams.INVALID_SPAN_ID) {
            return
        }

        val spanCount = parent.getSpanCount()
        val reverseLayout = parent.isLayoutReversed()
        val itemCount = state.itemCount
        val spanSize = layoutParams.spanSize
        val realSpanIndex = if (!reverseLayout) {
            spanIndex
        } else {
            spanCount - 1 - spanIndex
        }

        val isAtStartEdge = layoutParams.spanGroupIndex == 0
        val isAtEndEdge = layoutPosition + (spanCount - spanIndex - spanSize) >= itemCount - 1

        if (parent.getOrientation() == RecyclerView.VERTICAL) {
            applyVertically(
                outRect, realSpanIndex, spanSize, spanCount, isAtStartEdge, isAtEndEdge, reverseLayout
            )
        } else {
            applyHorizontally(
                outRect, realSpanIndex, spanSize, spanCount, isAtStartEdge, isAtEndEdge, reverseLayout
            )
        }
    }

    private fun applyVertically(
        outRect: Rect,
        spanIndex: Int,
        spanSize: Int,
        spanCount: Int,
        isAtStartEdge: Boolean,
        isAtEndEdge: Boolean,
        reverseLayout: Boolean
    ) {
        val startSpanSpace = spanCount - spanIndex
        val endSpanSpace = spanIndex + spanSize
        val startSpacing = itemSpacing * (startSpanSpace / spanCount.toFloat())
        val endSpacing = itemSpacing * (endSpanSpace / spanCount.toFloat())

        outRect.left = startSpacing.toInt()
        outRect.right = endSpacing.toInt()

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.top = edgeSpacing
                outRect.bottom = perpendicularItemSpacing
            } else {
                outRect.bottom = edgeSpacing
                outRect.top = perpendicularItemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.bottom = edgeSpacing
            } else {
                outRect.top = edgeSpacing
            }
        } else if (!reverseLayout) {
            outRect.bottom = perpendicularItemSpacing
        } else {
            outRect.top = perpendicularItemSpacing
        }
    }

    private fun applyHorizontally(
        outRect: Rect,
        spanIndex: Int,
        spanSize: Int,
        spanCount: Int,
        isAtStartEdge: Boolean,
        isAtEndEdge: Boolean,
        reverseLayout: Boolean
    ) {
        val startSpanSpace = spanCount - spanIndex
        val endSpanSpace = spanIndex + spanSize
        val startSpacing = itemSpacing * (startSpanSpace / spanCount.toFloat())
        val endSpacing = itemSpacing * (endSpanSpace / spanCount.toFloat())

        outRect.top = startSpacing.toInt()
        outRect.bottom = endSpacing.toInt()

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.left = edgeSpacing
                outRect.right = perpendicularItemSpacing
            } else {
                outRect.right = edgeSpacing
                outRect.left = perpendicularItemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.right = edgeSpacing
            } else {
                outRect.left = edgeSpacing
            }
        } else if (!reverseLayout) {
            outRect.right = perpendicularItemSpacing
        } else {
            outRect.left = perpendicularItemSpacing
        }
    }
}
