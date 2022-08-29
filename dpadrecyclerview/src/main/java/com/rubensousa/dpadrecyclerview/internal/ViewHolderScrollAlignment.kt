package com.rubensousa.dpadrecyclerview.internal

import android.view.View
import com.rubensousa.dpadrecyclerview.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.ViewHolderAlignment

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
