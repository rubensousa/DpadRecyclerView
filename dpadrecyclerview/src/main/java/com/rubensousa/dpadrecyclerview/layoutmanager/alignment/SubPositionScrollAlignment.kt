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
import com.rubensousa.dpadrecyclerview.SubPositionAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams

internal class SubPositionScrollAlignment {

    fun updateAlignments(
        view: View,
        layoutParams: DpadLayoutParams,
        alignments: List<SubPositionAlignment>,
        isVertical: Boolean,
        reverseLayout: Boolean,
    ) {
        // Calculate item alignments for each sub position
        val subAlignments = getSubPositionAnchors(
            view, alignments, layoutParams.getSubPositionAnchors(), isVertical, reverseLayout
        )
        layoutParams.setSubPositionAnchors(subAlignments)
    }

    private fun getSubPositionAnchors(
        itemView: View,
        alignments: List<SubPositionAlignment>,
        currentAnchors: IntArray?,
        isVertical: Boolean,
        reverseLayout: Boolean
    ): IntArray? {
        if (alignments.isEmpty()) {
            return null
        }
        val alignmentCache = if (currentAnchors == null || currentAnchors.size != alignments.size) {
            IntArray(alignments.size)
        } else {
            currentAnchors
        }
        alignments.forEachIndexed { index, alignment ->
            alignmentCache[index] = calculateAnchor(itemView, alignment, isVertical, reverseLayout)
        }
        return alignmentCache
    }

    private fun calculateAnchor(
        itemView: View,
        alignment: SubPositionAlignment,
        isVertical: Boolean,
        reverseLayout: Boolean
    ): Int {
        val alignmentView = getAlignmentView(itemView, alignment)
        return ViewAnchorHelper.calculateAnchor(
            itemView, alignmentView, alignment, isVertical, reverseLayout
        )
    }

    private fun getAlignmentView(itemView: View, alignment: SubPositionAlignment): View {
        if (alignment.alignmentViewId != View.NO_ID) {
            val alignmentView: View? = itemView.findViewById(alignment.alignmentViewId)
            if (alignmentView != null) {
                return alignmentView
            }
        }
        return itemView
    }

}
