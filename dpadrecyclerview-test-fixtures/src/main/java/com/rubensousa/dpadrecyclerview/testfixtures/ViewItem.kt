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

package com.rubensousa.dpadrecyclerview.testfixtures

import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds

data class ViewItem(
    val position: Int,
    val bounds: ViewBounds,
    val insets: ViewBounds,
) {

    val left: Int
        get() = bounds.left

    val top: Int
        get() = bounds.top

    val right: Int
        get() = bounds.right

    val bottom: Int
        get() = bounds.bottom

    val width: Int
        get() = bounds.width

    val height: Int
        get() = bounds.height

    fun getDecoratedLeft() = bounds.left - insets.left
    fun getDecoratedTop() = bounds.top - insets.top
    fun getDecoratedRight() = bounds.right + insets.right
    fun getDecoratedBottom() = bounds.bottom + insets.bottom

    fun getDecoratedWidth() = bounds.width + insets.left + insets.right
    fun getDecoratedHeight() = bounds.height + insets.top + insets.bottom

    fun getDecoratedBounds(): ViewBounds {
        return ViewBounds(
            getDecoratedLeft(),
            getDecoratedTop(),
            getDecoratedRight(),
            getDecoratedBottom()
        )
    }

    fun offsetHorizontally(offset: Int) {
        bounds.offsetHorizontal(offset)
    }

    fun offsetVertically(offset: Int) {
        bounds.offsetVertical(offset)
    }

}
