package com.rubensousa.dpadrecyclerview.internal


import android.view.View
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadLayoutParams

@MainThread
internal class ChildScrollAlignment {

    private var alignment = ChildAlignment(offset = 0)

    fun setAlignment(alignmentConfig: ChildAlignment) {
        alignment = alignmentConfig
    }

    fun getAlignment() = alignment

    fun updateAlignments(view: View, layoutParams: DpadLayoutParams, orientation: Int) {
        val alignmentPosition = ViewAlignmentHelper.getAlignmentPosition(
            itemView = view,
            alignmentView = view,
            layoutParams, alignment, orientation
        )
        if (orientation == RecyclerView.HORIZONTAL) {
            layoutParams.setAlignX(alignmentPosition)
        } else {
            layoutParams.setAlignY(alignmentPosition)
        }
    }

}
