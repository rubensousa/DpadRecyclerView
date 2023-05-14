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
import com.rubensousa.dpadrecyclerview.DpadLoopDirection
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

/**
 * An item decoration that applies a spacing to all sides of a view part of a Column or Row layout.
 *
 * @param itemSpacing spacing between items in the layout direction
 *
 * @param edgeSpacing spacing between the start and end edges in the layout orientation.
 * Default is [itemSpacing] if not specified.
 *
 * @param perpendicularEdgeSpacing spacing between the edges perpendicular to the layout orientation.
 * Default is 0.
 *
 */
class DpadLinearSpacingDecoration private constructor(
    @Px private val itemSpacing: Int,
    @Px private val edgeSpacing: Int,
    @Px private val perpendicularEdgeSpacing: Int
) : DpadSpacingDecoration() {

    companion object {

        @JvmStatic
        fun create(
            @Px itemSpacing: Int,
            @Px edgeSpacing: Int = itemSpacing,
            @Px perpendicularEdgeSpacing: Int = 0,
        ): DpadLinearSpacingDecoration {
            return DpadLinearSpacingDecoration(
                itemSpacing = itemSpacing,
                edgeSpacing = edgeSpacing,
                perpendicularEdgeSpacing = perpendicularEdgeSpacing
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
        val itemCount = state.itemCount
        val reverseLayout = parent.isLayoutReversed()

        val isAtStartEdge = if (!reverseLayout) {
            if (parent.getLoopDirection() == DpadLoopDirection.MAX) {
                layoutPosition == 0 && parent.findFirstVisibleItemPosition() == 0
            } else {
                layoutPosition == 0 && parent.getLoopDirection() == DpadLoopDirection.NONE
            }
        } else {
            layoutPosition == itemCount - 1 && parent.getLoopDirection() != DpadLoopDirection.MIN_MAX
        }

        val isAtEndEdge = if (!reverseLayout) {
            layoutPosition == itemCount - 1 && parent.getLoopDirection() == DpadLoopDirection.NONE
        } else {
            layoutPosition == 0 && parent.getLoopDirection() == DpadLoopDirection.NONE
        }

        if (parent.getOrientation() == RecyclerView.VERTICAL) {
            applyVertically(outRect, isAtStartEdge, isAtEndEdge, reverseLayout)
        } else {
            applyHorizontally(outRect, isAtStartEdge, isAtEndEdge, reverseLayout)
        }
    }

    private fun applyVertically(
        outRect: Rect,
        isAtStartEdge: Boolean,
        isAtEndEdge: Boolean,
        reverseLayout: Boolean
    ) {
        outRect.left = perpendicularEdgeSpacing
        outRect.right = perpendicularEdgeSpacing

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.top = edgeSpacing
                outRect.bottom = itemSpacing
            } else {
                outRect.bottom = edgeSpacing
                outRect.top = itemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.bottom = edgeSpacing
            } else {
                outRect.top = edgeSpacing
            }
        } else if (!reverseLayout) {
            outRect.bottom = itemSpacing
        } else {
            outRect.top = itemSpacing
        }
    }

    private fun applyHorizontally(
        outRect: Rect,
        isAtStartEdge: Boolean,
        isAtEndEdge: Boolean,
        reverseLayout: Boolean
    ) {
        outRect.top = perpendicularEdgeSpacing
        outRect.bottom = perpendicularEdgeSpacing

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.left = edgeSpacing
                outRect.right = itemSpacing
            } else {
                outRect.right = edgeSpacing
                outRect.left = itemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.right = edgeSpacing
            } else {
                outRect.left = edgeSpacing
            }
        } else if (!reverseLayout) {
            outRect.right = itemSpacing
        } else {
            outRect.left = itemSpacing
        }
    }

}
