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

package com.rubensousa.dpadrecyclerview.layoutmanager.alignment

import android.view.View
import com.rubensousa.dpadrecyclerview.ViewHolderAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams

internal class ViewHolderScrollAlignment {

    fun updateAlignments(
        view: View,
        layoutParams: DpadLayoutParams,
        alignments: List<ViewHolderAlignment>,
        orientation: Int
    ) {
        // Calculate item alignments for each sub position
        val subAlignments = getSubAlignmentPositions(
            view, layoutParams, alignments, layoutParams.getAlignmentPositions(), orientation
        )
        layoutParams.setAlignments(subAlignments, orientation)
    }

    private fun getSubAlignmentPositions(
        itemView: View,
        layoutParams: DpadLayoutParams,
        alignments: List<ViewHolderAlignment>,
        currentPositions: IntArray?,
        orientation: Int
    ): IntArray? {
        if (alignments.isEmpty()) {
            return null
        }
        val alignmentCache =
            if (currentPositions == null || currentPositions.size != alignments.size) {
                IntArray(alignments.size)
            } else {
                currentPositions
            }
        alignments.forEachIndexed { index, alignment ->
            alignmentCache[index] =
                getAlignmentPosition(itemView, layoutParams, alignment, orientation)
        }
        return alignmentCache
    }

    private fun getAlignmentPosition(
        itemView: View,
        layoutParams: DpadLayoutParams,
        alignment: ViewHolderAlignment,
        orientation: Int
    ): Int {
        val alignmentView = getAlignmentView(itemView, alignment)
        return ViewAlignmentHelper.getAlignmentPosition(
            itemView, alignmentView, layoutParams, alignment, orientation
        )
    }

    private fun getAlignmentView(itemView: View, alignment: ViewHolderAlignment): View {
        if (alignment.alignmentViewId != View.NO_ID) {
            val alignmentView: View? = itemView.findViewById(alignment.alignmentViewId)
            if (alignmentView != null) {
                return alignmentView
            }
        }
        return itemView
    }

}
