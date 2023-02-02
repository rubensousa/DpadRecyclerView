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

package com.rubensousa.dpadrecyclerview.layoutmanager

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DpadLayoutParams : RecyclerView.LayoutParams {

    companion object {

        /**
         * Views that haven't been laid out yet will have this in [spanIndex] and [spanGroupIndex]
         */
        const val INVALID_SPAN_ID = -1
    }

    // TODO For custom placement
    var leftInset = 0
        private set

    var topInset = 0
        private set

    var rightInset = 0
        private set

    var bottomInset = 0
        private set

    /**
     * Anchor alignment position. Always measured from start to end
     */
    var absoluteAnchor: Int = 0
        private set

    /**
     * Current span size as of the latest layout pass
     */
    var spanSize = 1
        private set

    /**
     * Current span index (column index) as of the latest layout pass
     */
    var spanIndex = INVALID_SPAN_ID
        private set

    /**
     * Current span group index (row index) as of the latest layout pass
     */
    var spanGroupIndex = INVALID_SPAN_ID
        private set

    private var alignmentPositions: IntArray? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(width: Int, height: Int) : super(width, height)
    constructor(source: ViewGroup.MarginLayoutParams) : super(source)
    constructor(source: ViewGroup.LayoutParams) : super(source)
    constructor(source: RecyclerView.LayoutParams) : super(source)
    constructor(source: DpadLayoutParams) : super(source)

    internal fun updateSpan(index: Int, groupIndex: Int, size: Int) {
        spanIndex = index
        spanGroupIndex = groupIndex
        spanSize = size
    }

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
        val width = if (view.isLaidOut) {
            view.width
        } else {
            view.measuredWidth
        }
        return width - leftInset - rightInset
    }

    fun getOpticalHeight(view: View): Int {
        val height = if (view.isLaidOut) {
            view.height
        } else {
            view.measuredHeight
        }
        return height - topInset - bottomInset
    }

    internal fun getAlignmentPositions(): IntArray? {
        return alignmentPositions
    }

    internal fun setAbsoluteAnchor(anchor: Int) {
        absoluteAnchor = anchor
    }

    internal fun setAlignments(newAlignments: IntArray?) {
        alignmentPositions = newAlignments
        if (newAlignments == null) {
            absoluteAnchor = 0
            return
        }
        absoluteAnchor = newAlignments[0]
    }

}
