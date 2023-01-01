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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

internal class GridRow(
    private val numberOfSpans: Int,
    var width: Int
) {

    var startSpanIndex = RecyclerView.NO_POSITION
    var endSpanIndex = RecyclerView.NO_POSITION

    var height = 0
        private set

    var top = 0
        private set

    private val heights = IntArray(numberOfSpans)

    fun setTop(top: Int) {
        this.top = top
    }

    fun fitsEnd(spanSize: Int): Boolean {
        return endSpanIndex + spanSize < numberOfSpans
    }

    fun isEndComplete(): Boolean {
        return endSpanIndex == numberOfSpans - 1
    }

    fun isStartComplete(): Boolean {
        return startSpanIndex == 0
    }

    fun fitsStart(spanSize: Int): Boolean {
        return startSpanIndex - spanSize >= 0
    }

    fun getSpanSpace(): Int {
        return width / numberOfSpans
    }

    fun append(viewSize: Int, spanSize: Int): Int {
        val viewSpanIndex = endSpanIndex + 1
        val viewStart = getSpanSpace() * viewSpanIndex
        updateHeight(viewSize, viewSpanIndex, spanSize)
        endSpanIndex += spanSize
        return viewStart
    }

    fun prepend(viewSize: Int, spanSize: Int): Int {
        startSpanIndex -= spanSize
        updateHeight(viewSize, startSpanIndex, spanSize)
        return getSpanSpace() * startSpanIndex
    }

    fun moveToNextRow(viewSize: Int, spanSize: Int, newTop: Int) {
        consume()
        startSpanIndex = 0
        endSpanIndex = spanSize - 1
        top = newTop
        updateHeight(viewSize, startSpanIndex, spanSize)
    }

    fun moveToPreviousRow(viewSize: Int, spanSize: Int, newTop: Int) {
        consume()
        endSpanIndex = numberOfSpans - 1
        startSpanIndex = endSpanIndex + 1 - spanSize
        top = newTop
        updateHeight(viewSize, startSpanIndex, spanSize)
    }

    fun consume(): Int {
        heights.fill(0)
        val previousHeight = height
        height = 0
        return previousHeight
    }

    fun reset() {
        consume()
        top = 0
    }

    fun updateHeight(viewSize: Int, spanIndex: Int, spanSize: Int) {
        height = max(viewSize, height)
        for (i in spanIndex until spanIndex + spanSize) {
            heights[i] = viewSize
        }
    }

}