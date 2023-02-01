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

internal object ViewAnchorHelper {

    private val tmpRect = Rect()

    @JvmStatic
    fun calculateAnchor(
        itemView: View,
        alignmentView: View,
        layoutParams: DpadLayoutParams,
        alignment: ViewAlignment,
        orientation: Int,
        reverseLayout: Boolean
    ): Int {
        return if (orientation == RecyclerView.HORIZONTAL) {
            getHorizontalAnchor(itemView, reverseLayout, layoutParams, alignmentView, alignment)
        } else {
            getVerticalAnchor(itemView, reverseLayout, layoutParams, alignmentView, alignment)
        }
    }

    @JvmStatic
    private fun getVerticalAnchor(
        itemView: View,
        reverseLayout: Boolean,
        layoutParams: DpadLayoutParams,
        alignmentView: View,
        alignment: ViewAlignment,
    ): Int {
        var anchor = 0
        val height = if (alignmentView === itemView) {
            layoutParams.getOpticalHeight(alignmentView)
        } else {
            alignmentView.height
        }

        if (alignment.alignToBaseline && alignmentView.baseline != -1) {
            anchor = alignmentView.baseline
        }

        if (!reverseLayout) {

            if (alignment.isOffsetRatioEnabled) {
                anchor = (height * alignment.offsetRatio).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.offsetRatio == 0f) {
                    anchor += alignmentView.paddingTop
                } else if (alignment.offsetRatio == 1f) {
                    anchor -= alignmentView.paddingBottom
                }
            }

            anchor += alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.top = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.top - layoutParams.topInset
            }

        } else {
            if (alignment.isOffsetRatioEnabled) {
                anchor = (height * (1.0f - alignment.offsetRatio)).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.offsetRatio == 0f) {
                    anchor -= alignmentView.paddingBottom
                } else if (alignment.offsetRatio == 1f) {
                    anchor += alignmentView.paddingTop
                }
            }

            anchor -= alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.bottom = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.bottom - layoutParams.bottomInset
            }

        }

        return anchor
    }

    @JvmStatic
    private fun getHorizontalAnchor(
        itemView: View,
        reverseLayout: Boolean,
        layoutParams: DpadLayoutParams,
        alignmentView: View,
        alignment: ViewAlignment
    ): Int {
        var anchor = 0
        val alignmentWidth = if (alignmentView === itemView) {
            layoutParams.getOpticalWidth(alignmentView)
        } else {
            alignmentView.width
        }
        /**
         * Order:
         * 1. Offset ratio
         * 2. View padding
         * 3. Manual offset
         */
        if (!reverseLayout) {

            if (alignment.isOffsetRatioEnabled) {
                anchor = (alignmentWidth * alignment.offsetRatio).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.offsetRatio == 0f) {
                    anchor += alignmentView.paddingLeft
                } else if (alignment.offsetRatio == 1f) {
                    anchor -= alignmentView.paddingRight
                }
            }

            anchor += alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.left = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.left - layoutParams.leftInset
            }

        } else {
            if (alignment.isOffsetRatioEnabled) {
                anchor = (alignmentWidth * (1.0f - alignment.offsetRatio)).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.offsetRatio == 0f) {
                    anchor -= alignmentView.paddingRight
                } else if (alignment.offsetRatio == 1f) {
                    anchor += alignmentView.paddingLeft
                }
            }

            anchor -= alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.right = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.right + layoutParams.rightInset
            }
        }
        return anchor
    }

}
