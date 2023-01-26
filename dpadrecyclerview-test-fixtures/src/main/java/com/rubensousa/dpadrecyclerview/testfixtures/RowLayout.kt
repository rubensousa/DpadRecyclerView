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

class RowLayout(config: LayoutConfig) : SingleSpanLayout(config) {

    override fun isVertical(): Boolean = false

    override fun getViewCenter(view: ViewItem): Int {
        return (view.getDecoratedLeft() + view.getDecoratedWidth() * config.childKeyline).toInt()
    }

    override fun append(position: Int, endOffset: Int): ViewItem {
        return ViewItem(
            position,
            bounds = ViewBounds(
                left = endOffset,
                top = 0,
                right = endOffset + config.viewWidth,
                bottom = config.viewHeight
            ),
            insets = config.decorInsets,
        )
    }

    override fun prepend(position: Int, startOffset: Int): ViewItem {
        return ViewItem(
            position,
            bounds = ViewBounds(
                left = startOffset - config.viewWidth,
                top = 0,
                right = startOffset,
                bottom = config.viewHeight
            ),
            insets = config.decorInsets
        )
    }

    fun scrollRight() {
        if (selectedPosition == getItemCount() - 1) {
            return
        }
        updateSelectedPosition(selectedPosition + 1)
        scrollBy(config.viewWidth)
    }

    fun scrollLeft() {
        if (selectedPosition == 0) {
            return
        }
        updateSelectedPosition(selectedPosition - 1)
        scrollBy(-config.viewWidth)
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("[")
        val views = getChildren()
        views.forEachIndexed { index, view ->
            stringBuilder.append("{${view.getDecoratedLeft()}, ${view.getDecoratedRight()}}")
            if (index < views.size - 1) {
                stringBuilder.append(" -> ")
            }
        }
        stringBuilder.append("]")
        return stringBuilder.toString()
    }

}
