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
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.SubPositionAlignment
import com.rubensousa.dpadrecyclerview.ViewAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams

internal class ChildAlignmentCalculator {

    private val tmpRect = Rect()

    fun updateAlignments(
        view: View,
        alignment: ChildAlignment,
        layoutParams: DpadLayoutParams,
        isVertical: Boolean,
        reverseLayout: Boolean
    ) {
        val anchor = calculateAnchor(
            itemView = view,
            alignmentView = view,
            alignment, isVertical, reverseLayout
        )
        layoutParams.setAlignmentAnchor(anchor)
    }

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
        return calculateAnchor(
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

    private fun calculateAnchor(
        itemView: View,
        alignmentView: View,
        alignment: ViewAlignment,
        isVertical: Boolean,
        reverseLayout: Boolean
    ): Int {
        return if (isVertical) {
            getVerticalAnchor(itemView, reverseLayout, alignmentView, alignment)
        } else {
            getHorizontalAnchor(itemView, reverseLayout, alignmentView, alignment)
        }
    }

    private fun getVerticalAnchor(
        itemView: View,
        reverseLayout: Boolean,
        alignmentView: View,
        alignment: ViewAlignment,
    ): Int {
        var anchor = 0
        val height = if (alignmentView === itemView) {
            if (itemView.isLaidOut) {
                itemView.height
            } else {
                itemView.measuredHeight
            }
        } else {
            alignmentView.height
        }

        if (alignment.alignToBaseline && alignmentView.baseline != -1) {
            anchor = alignmentView.baseline
        }

        if (!reverseLayout) {

            if (alignment.isFractionEnabled) {
                anchor = (height * alignment.fraction).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.fraction == 0f) {
                    anchor += alignmentView.paddingTop
                } else if (alignment.fraction == 1f) {
                    anchor -= alignmentView.paddingBottom
                }
            }

            anchor += alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.top = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.top // - layoutParams.topInset
            }

        } else {
            if (alignment.isFractionEnabled) {
                anchor = (height * (1.0f - alignment.fraction)).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.fraction == 0f) {
                    anchor -= alignmentView.paddingBottom
                } else if (alignment.fraction == 1f) {
                    anchor += alignmentView.paddingTop
                }
            }

            anchor -= alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.bottom = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.bottom //- layoutParams.bottomInset
            }

        }

        return anchor
    }

    private fun getHorizontalAnchor(
        itemView: View,
        reverseLayout: Boolean,
        alignmentView: View,
        alignment: ViewAlignment
    ): Int {
        var anchor = 0
        val alignmentWidth = if (alignmentView === itemView) {
            if (itemView.isLaidOut) {
                itemView.width
            } else {
                itemView.measuredWidth
            }
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

            if (alignment.isFractionEnabled) {
                anchor = (alignmentWidth * alignment.fraction).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.fraction == 0f) {
                    anchor += alignmentView.paddingLeft
                } else if (alignment.fraction == 1f) {
                    anchor -= alignmentView.paddingRight
                }
            }

            anchor += alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.left = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.left // - layoutParams.leftInset
            }

        } else {
            if (alignment.isFractionEnabled) {
                anchor = (alignmentWidth * (1.0f - alignment.fraction)).toInt()
            }

            if (alignment.includePadding) {
                if (alignment.fraction == 0f) {
                    anchor -= alignmentView.paddingRight
                } else if (alignment.fraction == 1f) {
                    anchor += alignmentView.paddingLeft
                }
            }

            anchor -= alignment.offset

            if (itemView !== alignmentView) {
                tmpRect.right = anchor
                (itemView as ViewGroup).offsetDescendantRectToMyCoords(alignmentView, tmpRect)
                anchor = tmpRect.right // + layoutParams.rightInset
            }
        }
        return anchor
    }

}
