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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DpadLayoutParams : GridLayoutManager.LayoutParams {

    // TODO For custom placement
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

    private var alignmentPositions: IntArray? = null

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(width: Int, height: Int) : super(width, height)
    constructor(source: ViewGroup.MarginLayoutParams) : super(source)
    constructor(source: ViewGroup.LayoutParams) : super(source)
    constructor(source: RecyclerView.LayoutParams) : super(source)
    constructor(source: DpadLayoutParams) : super(source)

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

    fun getAlignmentPositions(): IntArray? {
        return alignmentPositions
    }

    fun setAlignX(value: Int) {
        alignX = value
    }

    fun setAlignY(value: Int) {
        alignY = value
    }

    fun setAlignments(newAlignments: IntArray?, orientation: Int) {
        alignmentPositions = newAlignments
        if (newAlignments == null) {
            alignX = 0
            alignY = 0
            return
        }
        if (orientation == RecyclerView.HORIZONTAL) {
            alignX = newAlignments[0]
        } else {
            alignY = newAlignments[0]
        }
    }

}
