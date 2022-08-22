package com.rubensousa.dpadrecyclerview.internal


import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadGridLayoutParams

// TODO: Add unit tests
internal class ChildScrollAlignment {

    companion object {
        private val tmpRect = Rect()

        @JvmStatic
        fun getHorizontalAlignment(
            itemView: View,
            layoutParams: DpadGridLayoutParams,
            alignmentView: View,
            alignment: ChildAlignment
        ): Int {
            var offset = -alignment.offset
            if (itemView.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                offset = if (alignmentView === itemView) {
                    layoutParams.getOpticalWidth(alignmentView) - offset
                } else {
                    alignmentView.width - offset
                }
                if (alignment.includePadding) {
                    if (alignment.offsetStartRatio == 0f) {
                        offset -= alignmentView.paddingRight
                    } else if (alignment.offsetStartRatio == 1f) {
                        offset += alignmentView.paddingLeft
                    }
                }
                if (alignment.isOffsetRatioEnabled) {
                    val width = if (alignmentView === itemView) {
                        layoutParams.getOpticalWidth(alignmentView)
                    } else {
                        alignmentView.width
                    }
                    offset -= (width * alignment.offsetStartRatio).toInt()
                }
                if (itemView !== alignmentView) {
                    tmpRect.right = offset
                    (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                    offset = tmpRect.right + layoutParams.rightInset
                }
            } else {
                if (alignment.includePadding) {
                    if (alignment.offsetStartRatio == 0f) {
                        offset += alignmentView.paddingLeft
                    } else if (alignment.offsetStartRatio == 1f) {
                        offset -= alignmentView.paddingRight
                    }
                }
                if (alignment.isOffsetRatioEnabled) {
                    val width = if (alignmentView === itemView) {
                        layoutParams.getOpticalWidth(alignmentView)
                    } else {
                        alignmentView.width
                    }
                    offset += (width * alignment.offsetStartRatio).toInt()
                }
                if (itemView !== alignmentView) {
                    tmpRect.left = offset
                    (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                    offset = tmpRect.left - layoutParams.leftInset
                }
            }
            return offset
        }

        @JvmStatic
        fun getVerticalAlignment(
            itemView: View,
            layoutParams: DpadGridLayoutParams,
            alignmentView: View,
            config: ChildAlignment,
        ): Int {
            var alignPos = -config.offset
            if (config.includePadding) {
                if (config.offsetStartRatio == 0f) {
                    alignPos += alignmentView.paddingTop
                } else if (config.offsetStartRatio == 1f) {
                    alignPos -= alignmentView.paddingBottom
                }
            }
            if (config.isOffsetRatioEnabled) {
                val height = if (alignmentView === itemView) {
                    layoutParams.getOpticalHeight(alignmentView)
                } else {
                    alignmentView.height
                }
                alignPos += (height * config.offsetStartRatio).toInt()
            }
            if (itemView !== alignmentView) {
                tmpRect.top = alignPos
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                alignPos = tmpRect.top - layoutParams.topInset
            }
            if (config.alignToBaseline) {
                alignPos += alignmentView.baseline
            }
            return alignPos
        }

        /**
         * get alignment position relative to optical left/top of itemView.
         */
        @JvmStatic
        fun getAlignmentPosition(
            itemView: View, alignment: ChildAlignment, orientation: Int
        ): Int {
            val layoutParams = itemView.layoutParams as DpadGridLayoutParams
            val alignmentView = getAlignmentView(itemView, alignment)
            return if (orientation == RecyclerView.HORIZONTAL) {
                getHorizontalAlignment(itemView, layoutParams, alignmentView, alignment)
            } else {
                getVerticalAlignment(itemView, layoutParams, alignmentView, alignment)
            }
        }

        private fun getAlignmentView(itemView: View, alignment: ChildAlignment): View {
            if (alignment.alignmentViewId != View.NO_ID) {
                val alignmentView: View? = itemView.findViewById(alignment.alignmentViewId)
                if (alignmentView != null) {
                    return alignmentView
                }
            }
            return itemView
        }

    }

    var config = ChildAlignment(offset = 0)
    var orientation = RecyclerView.HORIZONTAL
        private set

    fun setOrientation(newOrientation: Int) {
        orientation = newOrientation
    }

    fun getHorizontalAlignmentPosition(itemView: View): Int {
        return getAlignmentPosition(itemView, config, RecyclerView.HORIZONTAL)
    }

    fun getVerticalAlignmentPosition(itemView: View): Int {
        return getAlignmentPosition(itemView, config, RecyclerView.VERTICAL)
    }

}
