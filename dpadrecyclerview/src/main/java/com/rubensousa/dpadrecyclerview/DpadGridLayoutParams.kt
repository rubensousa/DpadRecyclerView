package com.rubensousa.dpadrecyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.internal.ChildScrollAlignment

open class DpadGridLayoutParams : GridLayoutManager.LayoutParams {

    // For placement
    var leftInset = 0
        private set
    var topInset = 0
        private set
    var rightInset = 0
        private set
    var bottomInset = 0
        private set

    // For alignment
    var alignX = 0
        private set
    var alignY = 0
        private set

    private var alignmentCache: IntArray? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(width: Int, height: Int) : super(width, height)
    constructor(source: ViewGroup.MarginLayoutParams) : super(source)
    constructor(source: ViewGroup.LayoutParams) : super(source)
    constructor(source: RecyclerView.LayoutParams) : super(source)
    constructor(source: DpadGridLayoutParams) : super(source)

    fun getOpticalLeft(view: View): Int {
        return view.left + leftInset
    }

    fun getOpticalTop(view: View): Int {
        return view.top + topInset
    }

    fun getOpticalRight(view: View): Int {
        return view.right - rightInset
    }

    fun getOpticalBottom(view: View): Int {
        return view.bottom - bottomInset
    }

    fun getOpticalWidth(view: View): Int {
        return view.width - leftInset - rightInset
    }

    fun getOpticalHeight(view: View): Int {
        return view.height - topInset - bottomInset
    }

    fun getAlignments(): IntArray? {
        return alignmentCache
    }

    fun setAlignX(value: Int) {
        alignX = value
    }

    fun setAlignY(value: Int) {
        alignY = value
    }

    fun calculateItemAlignments(
        childAlignments: List<ChildAlignment>,
        orientation: Int,
        view: View
    ) {
        if (childAlignments.isEmpty()) {
            return
        }
        val cache = if (alignmentCache == null || alignmentCache?.size != childAlignments.size) {
            IntArray(childAlignments.size)
        } else {
            alignmentCache!!
        }
        alignmentCache = cache
        childAlignments.forEachIndexed { index, childAlignment ->
            cache[index] = ChildScrollAlignment.getAlignmentPosition(
                view, childAlignment, orientation
            )
        }
        if (orientation == RecyclerView.HORIZONTAL) {
            alignX = cache[0]
        } else {
            alignY = cache[0]
        }
    }

}
