package com.rubensousa.dpadrecyclerview.internal

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadLayoutParams

internal class DpadLayoutDelegate {

    var gravity = Gravity.TOP
    var orientation = RecyclerView.VERTICAL
    var spanCount = 1

    fun checkLayoutParams(layoutParams: RecyclerView.LayoutParams?): Boolean {
        return layoutParams is DpadLayoutParams
    }

    fun generateLayoutParams(context: Context, attrs: AttributeSet): RecyclerView.LayoutParams {
        return DpadLayoutParams(context, attrs)
    }

    fun generateLayoutParams(layoutParams: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return when (layoutParams) {
            is DpadLayoutParams -> DpadLayoutParams(layoutParams)
            is RecyclerView.LayoutParams -> DpadLayoutParams(layoutParams)
            is ViewGroup.MarginLayoutParams -> DpadLayoutParams(layoutParams)
            else -> DpadLayoutParams(layoutParams)
        }
    }

    fun generateDefaultLayoutParams(orientation: Int): RecyclerView.LayoutParams {
        return if (orientation == RecyclerView.HORIZONTAL) {
            DpadLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            DpadLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    fun layoutDecoratedWithMargins(
        left: Int, top: Int, right: Int, bottom: Int,
        width: Int, height: Int, viewBounds: Rect
    ) {
        var viewLeft = left
        var viewTop = top
        var viewRight = right
        var viewBottom = bottom
        if (isHorizontal() && gravity != Gravity.TOP && spanCount == 1) {
            if (gravity == Gravity.CENTER) {
                val viewHeight = viewBottom - viewTop
                viewTop = height / 2 - viewHeight / 2
                viewBottom = viewTop + viewHeight
            } else if (gravity == Gravity.BOTTOM) {
                val viewHeight = viewBottom - viewTop
                viewBottom = height
                viewTop = viewBottom - viewHeight
            }
        } else if (isVertical() && gravity != Gravity.START && spanCount == 1) {
            if (gravity == Gravity.CENTER) {
                val viewWidth = viewRight - viewLeft
                viewLeft = width / 2 - viewWidth / 2
                viewRight = viewLeft + viewWidth
            } else if (gravity == Gravity.END) {
                val viewWidth = viewRight - viewLeft
                viewRight = width
                viewLeft = viewRight - viewWidth
            }
        }
        viewBounds.set(viewLeft, viewTop, viewRight, viewBottom)
    }

    fun getDecoratedLeft(child: View, decoratedLeft: Int): Int {
        return decoratedLeft + getLayoutParams(child).leftInset
    }

    fun getDecoratedTop(child: View, decoratedTop: Int): Int {
        return decoratedTop + getLayoutParams(child).topInset
    }

    fun getDecoratedRight(child: View, decoratedRight: Int): Int {
        return decoratedRight - getLayoutParams(child).rightInset
    }

    fun getDecoratedBottom(child: View, decoratedBottom: Int): Int {
        return decoratedBottom - getLayoutParams(child).bottomInset
    }

    /* TODO Figure out optical insets
    override fun getDecoratedBoundsWithMargins(view: View, outBounds: Rect) {
        super.getDecoratedBoundsWithMargins(view, outBounds)
        val layoutParams = view.layoutParams as DpadGridLayoutParams
        scrollAlignment?.updateOpticalInsets(view, layoutParams, outBounds)
    }*/

    fun isHorizontal() = orientation == RecyclerView.HORIZONTAL
    fun isVertical() = orientation == RecyclerView.VERTICAL

    fun getLayoutParams(child: View): DpadLayoutParams {
        return child.layoutParams as DpadLayoutParams
    }


}
