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
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class DpadLayoutParams : RecyclerView.LayoutParams {

    companion object {

        /**
         * Views that haven't been laid out yet will have this in [spanIndex] and [spanGroupIndex]
         */
        const val INVALID_SPAN_ID = -1
    }

    /**
     * Anchor alignment position. Always applied from start to end
     */
    var alignmentAnchor: Int = 0
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

    private var subPositionAnchors: IntArray? = null

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

    internal fun getSubPositionAnchors(): IntArray? {
        return subPositionAnchors
    }

    internal fun setAlignmentAnchor(anchor: Int) {
        alignmentAnchor = anchor
    }

    internal fun setSubPositionAnchors(newAlignments: IntArray?) {
        subPositionAnchors = newAlignments
        if (newAlignments == null) {
            alignmentAnchor = 0
            return
        }
        alignmentAnchor = newAlignments[0]
    }

}
