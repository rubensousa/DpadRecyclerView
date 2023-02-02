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

/**
 * An item decoration that applies a spacing to all sides of an grid item.
 *
 * Check [setSpacingLookup] to customise weather individual ViewHolders
 *
 * @param horizontalItemSpacing horizontal spacing between items
 *
 * @param horizontalEdgeSpacing horizontal spacing for the start and end edges
 *
 * @param verticalItemSpacing vertical spacing between items
 *
 * @param verticalEdgeSpacing vertical spacing for the top and bottom edges
 *
 */
class DpadLinearSpacingDecoration private constructor(
    @Px private var horizontalItemSpacing: Int,
    @Px private var horizontalEdgeSpacing: Int,
    @Px private var verticalItemSpacing: Int,
    @Px private var verticalEdgeSpacing: Int
) : DpadSpacingDecoration() {

    companion object {

        @JvmStatic
        fun createVertical(
            @Px verticalItemSpacing: Int,
            @Px verticalEdgeSpacing: Int = verticalItemSpacing,
            @Px horizontalEdgeSpacing: Int = 0, // Start and end spacing
        ): DpadLinearSpacingDecoration {
            return DpadLinearSpacingDecoration(
                verticalItemSpacing = verticalItemSpacing,
                verticalEdgeSpacing = verticalEdgeSpacing,
                horizontalItemSpacing = 0,
                horizontalEdgeSpacing = horizontalEdgeSpacing,
            )
        }

        @JvmStatic
        fun createHorizontal(
            @Px horizontalItemSpacing: Int,
            @Px horizontalEdgeSpacing: Int = horizontalItemSpacing,
            @Px verticalEdgeSpacing: Int = 0, // Top and bottom spacing
        ): DpadLinearSpacingDecoration {
            return DpadLinearSpacingDecoration(
                horizontalItemSpacing = horizontalItemSpacing,
                horizontalEdgeSpacing = horizontalEdgeSpacing,
                verticalItemSpacing = 0,
                verticalEdgeSpacing = verticalEdgeSpacing
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
            layoutPosition == 0
        } else {
            layoutPosition == itemCount - 1
        }

        val isAtEndEdge = if (!reverseLayout) {
            layoutPosition == itemCount - 1
        } else {
            layoutPosition == 0
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
        outRect.left = horizontalEdgeSpacing
        outRect.right = horizontalEdgeSpacing

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
        isAtStartEdge: Boolean,
        isAtEndEdge: Boolean,
        reverseLayout: Boolean
    ) {
        outRect.top = verticalEdgeSpacing
        outRect.bottom = verticalEdgeSpacing

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
