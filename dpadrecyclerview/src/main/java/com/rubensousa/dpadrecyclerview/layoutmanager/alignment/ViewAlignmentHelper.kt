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

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ViewAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams

internal object ViewAlignmentHelper {

    private val tmpRect = Rect()

    @JvmStatic
    fun getAlignmentPosition(
        itemView: View,
        alignmentView: View,
        layoutParams: DpadLayoutParams,
        alignment: ViewAlignment,
        orientation: Int
    ): Int {
        return if (orientation == RecyclerView.HORIZONTAL) {
            getHorizontalAlignment(itemView, layoutParams, alignmentView, alignment)
        } else {
            getVerticalAlignment(itemView, layoutParams, alignmentView, alignment)
        }
    }

    @JvmStatic
    private fun getVerticalAlignment(
        itemView: View,
        layoutParams: DpadLayoutParams,
        alignmentView: View,
        config: ViewAlignment,
    ): Int {
        var alignPos = -config.offset
        if (config.includePadding) {
            if (config.offsetRatio == 0f) {
                alignPos += alignmentView.paddingTop
            } else if (config.offsetRatio == 1f) {
                alignPos -= alignmentView.paddingBottom
            }
        }
        if (config.isOffsetRatioEnabled) {
            val height = if (alignmentView === itemView) {
                layoutParams.getOpticalHeight(alignmentView)
            } else {
                alignmentView.height
            }
            alignPos += (height * config.offsetRatio).toInt()
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

    @JvmStatic
    private fun getHorizontalAlignment(
        itemView: View,
        layoutParams: DpadLayoutParams,
        alignmentView: View,
        alignment: ViewAlignment
    ): Int {
        var offset = -alignment.offset
        if (itemView.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
            offset = if (alignmentView === itemView) {
                layoutParams.getOpticalWidth(alignmentView) - offset
            } else {
                alignmentView.width - offset
            }
            if (alignment.includePadding) {
                if (alignment.offsetRatio == 0f) {
                    offset -= alignmentView.paddingRight
                } else if (alignment.offsetRatio == 1f) {
                    offset += alignmentView.paddingLeft
                }
            }
            if (alignment.isOffsetRatioEnabled) {
                val width = if (alignmentView === itemView) {
                    layoutParams.getOpticalWidth(alignmentView)
                } else {
                    alignmentView.width
                }
                offset -= (width * alignment.offsetRatio).toInt()
            }
            if (itemView !== alignmentView) {
                tmpRect.right = offset
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                offset = tmpRect.right + layoutParams.rightInset
            }
        } else {
            if (alignment.includePadding) {
                if (alignment.offsetRatio == 0f) {
                    offset += alignmentView.paddingLeft
                } else if (alignment.offsetRatio == 1f) {
                    offset -= alignmentView.paddingRight
                }
            }
            if (alignment.isOffsetRatioEnabled) {
                val width = if (alignmentView === itemView) {
                    layoutParams.getOpticalWidth(alignmentView)
                } else {
                    alignmentView.width
                }
                offset += (width * alignment.offsetRatio).toInt()
            }
            if (itemView !== alignmentView) {
                tmpRect.left = offset
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                offset = tmpRect.left - layoutParams.leftInset
            }
        }
        return offset
    }

}
