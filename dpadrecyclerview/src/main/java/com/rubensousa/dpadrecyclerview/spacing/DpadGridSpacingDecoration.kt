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
 * An item decoration that applies a spacing to all sides of an grid item.
 *
 * Check [setSpacingLookup] to customise weather individual ViewHolders
 *
 * @param horizontalItemSpacing horizontal spacing between items
 *
 * @param horizontalEdgeSpacing horizontal spacing in items bordering an edge.
 * Only applied to horizontal grids
 *
 * @param verticalItemSpacing vertical spacing between items
 *
 * @param verticalEdgeSpacing vertical spacing in items bordering an edge.
 * Only applied to vertical grids
 *
 */
class DpadGridSpacingDecoration constructor(
    @Px private var horizontalItemSpacing: Int,
    @Px private var verticalItemSpacing: Int,
    @Px private var horizontalEdgeSpacing: Int = horizontalItemSpacing,
    @Px private var verticalEdgeSpacing: Int = verticalItemSpacing
) : DpadSpacingDecoration() {

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

        val isAtStartEdge = layoutParams.spanGroupIndex == 0

        val isAtEndEdge = if (!reverseLayout) {
            (layoutPosition + spanCount - spanIndex - spanSize) >= itemCount - 1
        } else {
            (layoutPosition + spanSize + spanIndex) >= itemCount
        }

        if (parent.getOrientation() == RecyclerView.VERTICAL) {
            applyVertically(
                outRect, spanIndex, spanSize, spanCount, isAtStartEdge, isAtEndEdge, reverseLayout
            )
        } else {
            applyHorizontally(
                outRect, spanIndex, spanSize, spanCount, isAtStartEdge, isAtEndEdge, reverseLayout
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
        val startSpacing = horizontalItemSpacing * (startSpanSpace / spanCount.toFloat())
        val endSpacing = horizontalItemSpacing * (endSpanSpace / spanCount.toFloat())

        outRect.left = startSpacing.toInt()
        outRect.right = endSpacing.toInt()

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.top = verticalEdgeSpacing
                outRect.bottom = verticalItemSpacing
            } else {
                outRect.bottom = verticalEdgeSpacing
                outRect.top = verticalItemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.bottom = verticalEdgeSpacing
            } else {
                outRect.top = verticalEdgeSpacing
            }
        } else if (!reverseLayout) {
            outRect.bottom = verticalItemSpacing
        } else {
            outRect.top = verticalItemSpacing
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
        val startSpacing = verticalItemSpacing * (startSpanSpace / spanCount.toFloat())
        val endSpacing = verticalItemSpacing * (endSpanSpace / spanCount.toFloat())

        outRect.top = startSpacing.toInt()
        outRect.bottom = endSpacing.toInt()

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.left = horizontalEdgeSpacing
                outRect.right = horizontalItemSpacing
            } else {
                outRect.right = horizontalEdgeSpacing
                outRect.left = horizontalItemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.right = horizontalEdgeSpacing
            } else {
                outRect.left = horizontalEdgeSpacing
            }
        } else if (!reverseLayout) {
            outRect.right = horizontalItemSpacing
        } else {
            outRect.left = horizontalItemSpacing
        }
    }
}
