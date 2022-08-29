package com.rubensousa.dpadrecyclerview.internal


import android.view.View
import androidx.annotation.MainThread
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadLayoutParams

// TODO: Add unit tests
@MainThread
internal class ChildScrollAlignment {

    private var config = ChildAlignment(offset = 0)

    fun setAlignmentConfiguration(alignmentConfig: ChildAlignment) {
        config = alignmentConfig
    }

    fun updateAlignments(view: View, layoutParams: DpadLayoutParams, orientation: Int) {
        val alignmentPosition = ViewAlignmentHelper.getAlignmentPosition(
            itemView = view,
            alignmentView = view,
            layoutParams, config, orientation
        )
        if (orientation == RecyclerView.HORIZONTAL) {
            layoutParams.setAlignX(alignmentPosition)
        } else {
            layoutParams.setAlignY(alignmentPosition)
        }
    }

}
