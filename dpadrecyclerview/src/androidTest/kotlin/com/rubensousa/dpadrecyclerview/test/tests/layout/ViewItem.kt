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

package com.rubensousa.dpadrecyclerview.test.tests.layout

import android.graphics.Rect

data class ViewItem(
    val bounds: Rect,
    val insets: Rect
) {

    fun getLeft() = bounds.left
    fun getTop() = bounds.top
    fun getRight() = bounds.right
    fun getBottom() = bounds.bottom

    fun getWidth() = bounds.width()
    fun getHeight() = bounds.height()

    fun getDecoratedLeft() = bounds.left - insets.left
    fun getDecoratedTop() = bounds.top - insets.top
    fun getDecoratedRight() = bounds.right + insets.right
    fun getDecoratedBottom() = bounds.bottom + insets.bottom

    fun getDecoratedWidth() = bounds.width() + insets.left + insets.right
    fun getDecoratedHeight() = bounds.height() + insets.top + insets.bottom

    fun offsetHorizontally(offset: Int) {
        bounds.offset(offset, 0)
    }

    fun offsetVertically(offset: Int) {
        bounds.offset(0, offset)
    }

}
