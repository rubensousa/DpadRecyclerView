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
 * @param minEdgeSpacing spacing between the start edge and the first item.
 * Default is [itemSpacing] if not specified.
 *
 * @param maxEdgeSpacing spacing between the last item and the end edge
 * Default is [itemSpacing] if not specified.
 *
 * @param perpendicularEdgeSpacing spacing between the edges perpendicular to the layout orientation.
 * Default is 0.
 *
 */
class DpadLinearSpacingDecoration private constructor(
    @Px val itemSpacing: Int,
    @Px val minEdgeSpacing: Int,
    @Px val maxEdgeSpacing: Int,
    @Px val perpendicularEdgeSpacing: Int,
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
                minEdgeSpacing = edgeSpacing,
                maxEdgeSpacing = edgeSpacing,
                perpendicularEdgeSpacing = perpendicularEdgeSpacing
            )
        }

        @JvmStatic
        fun create(
            @Px itemSpacing: Int,
            @Px minEdgeSpacing: Int = itemSpacing,
            @Px maxEdgeSpacing: Int = itemSpacing,
            @Px perpendicularEdgeSpacing: Int = 0,
        ): DpadLinearSpacingDecoration {
            return DpadLinearSpacingDecoration(
                itemSpacing = itemSpacing,
                minEdgeSpacing = minEdgeSpacing,
                maxEdgeSpacing = maxEdgeSpacing,
                perpendicularEdgeSpacing = perpendicularEdgeSpacing
            )
        }

    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        layoutPosition: Int,
        parent: DpadRecyclerView,
        state: RecyclerView.State,
    ) {
        val itemCount = state.itemCount
        val reverseLayout = parent.isLayoutReversed()

        val isAtStartEdge = layoutPosition == 0
                && parent.getLoopDirection() == DpadLoopDirection.NONE

        val isAtEndEdge = layoutPosition == itemCount - 1
                && parent.getLoopDirection() == DpadLoopDirection.NONE

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
        reverseLayout: Boolean,
    ) {
        outRect.left = perpendicularEdgeSpacing
        outRect.right = perpendicularEdgeSpacing

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.top = minEdgeSpacing
                outRect.bottom = itemSpacing
            } else {
                outRect.bottom = minEdgeSpacing
                outRect.top = itemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.bottom = maxEdgeSpacing
            } else {
                outRect.top = maxEdgeSpacing
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
        reverseLayout: Boolean,
    ) {
        outRect.top = perpendicularEdgeSpacing
        outRect.bottom = perpendicularEdgeSpacing

        if (isAtStartEdge) {
            if (!reverseLayout) {
                outRect.left = minEdgeSpacing
                outRect.right = itemSpacing
            } else {
                outRect.right = minEdgeSpacing
                outRect.left = itemSpacing
            }
        } else if (isAtEndEdge) {
            if (!reverseLayout) {
                outRect.right = maxEdgeSpacing
            } else {
                outRect.left = maxEdgeSpacing
            }
        } else if (!reverseLayout) {
            outRect.right = itemSpacing
        } else {
            outRect.left = itemSpacing
        }
    }

    internal fun withItemSpacing(spacing: Int): DpadLinearSpacingDecoration {
        return DpadLinearSpacingDecoration(
            itemSpacing = spacing,
            minEdgeSpacing = minEdgeSpacing,
            maxEdgeSpacing = maxEdgeSpacing,
            perpendicularEdgeSpacing = perpendicularEdgeSpacing
        )
    }

    internal fun withMinEdgeSpacing(spacing: Int): DpadLinearSpacingDecoration {
        return DpadLinearSpacingDecoration(
            itemSpacing = itemSpacing,
            minEdgeSpacing = spacing,
            maxEdgeSpacing = maxEdgeSpacing,
            perpendicularEdgeSpacing = perpendicularEdgeSpacing
        )
    }

    internal fun withEdgeSpacing(spacing: Int): DpadLinearSpacingDecoration {
        return DpadLinearSpacingDecoration(
            itemSpacing = itemSpacing,
            minEdgeSpacing = spacing,
            maxEdgeSpacing = spacing,
            perpendicularEdgeSpacing = perpendicularEdgeSpacing
        )
    }

    internal fun withMaxEdgeSpacing(spacing: Int): DpadLinearSpacingDecoration {
        return DpadLinearSpacingDecoration(
            itemSpacing = itemSpacing,
            minEdgeSpacing = minEdgeSpacing,
            maxEdgeSpacing = spacing,
            perpendicularEdgeSpacing = perpendicularEdgeSpacing
        )
    }

    override fun equals(other: Any?): Boolean {
        return other is DpadLinearSpacingDecoration
                && this.itemSpacing == other.itemSpacing
                && this.minEdgeSpacing == other.minEdgeSpacing
                && this.maxEdgeSpacing == other.maxEdgeSpacing
                && this.perpendicularEdgeSpacing == other.perpendicularEdgeSpacing
    }

    override fun hashCode(): Int {
        var result = itemSpacing
        result = 31 * result + minEdgeSpacing
        result = 31 * result + maxEdgeSpacing
        result = 31 * result + perpendicularEdgeSpacing
        return result
    }

}
