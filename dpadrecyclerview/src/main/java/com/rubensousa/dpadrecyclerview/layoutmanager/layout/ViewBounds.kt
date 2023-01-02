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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout

import android.graphics.Rect

/**
 * Replaces Rect for unit testing purposes
 */
class ViewBounds(
    var left: Int = 0,
    var top: Int = 0,
    var right: Int = 0,
    var bottom: Int = 0
) {

    val height: Int
        get() = bottom - top

    val width: Int
        get() = right - left

    fun setEmpty() {
        left = 0
        top = 0
        right = 0
        bottom = 0
    }

    fun offsetVertical(dy: Int) {
        top += dy
        bottom += dy
    }

    fun offsetHorizontal(dx: Int) {
        left += dx
        right += dx
    }

    fun previousVertical(size: Int): ViewBounds {
        return ViewBounds(
            left = left,
            top = top - size,
            right = right,
            bottom = top
        )
    }

    fun nextVertical(size: Int): ViewBounds {
        return ViewBounds(
            left = left,
            top = bottom,
            right = right,
            bottom = bottom + size
        )
    }

    fun asRect(): Rect {
        return Rect(left, top, right, bottom)
    }

    override fun toString(): String {
        return "ViewBounds(left=$left, top=$top, right=$right, bottom=$bottom, " +
                "height=$height, width=$width)"
    }


}
