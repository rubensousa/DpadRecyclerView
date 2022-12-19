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

class LayoutRow(width: Int, height: Int) : LayoutMatrix(width, height) {

    override fun scrollBy(offset: Int) {
        forEachView { view ->
            view.offsetVertically(offset)
        }
    }

    fun prepend(
        fillSpace: Int,
        itemWidth: Int,
        itemHeight: Int,
        insets: Rect = EMPTY_INSETS
    ): List<ViewItem> {
        return fill(fillSpace, itemWidth, itemHeight, insets, false)
    }

    fun append(
        fillSpace: Int,
        itemWidth: Int,
        itemHeight: Int,
        insets: Rect = EMPTY_INSETS
    ): List<ViewItem> {
        return fill(fillSpace, itemWidth, itemHeight, insets, true)
    }

    fun recycleStart(extraLayoutSpaceStart: Int) {
        val limit = -extraLayoutSpaceStart
        val childCount = getNumberOfViewsInLayout()
        for (i in 0 until childCount) {
            val child = getViewAt(i)
            if (child.getDecoratedRight() > limit) {
                recycleFromStart(i)
                return
            }
        }
    }

    fun recycleEnd(extraLayoutSpaceEnd: Int) {
        val limit = width + extraLayoutSpaceEnd
        val childCount = getNumberOfViewsInLayout()
        for (i in childCount - 1 downTo 0) {
            val child = getViewAt(i)
            if (child.getDecoratedLeft() < limit) {
                recycleFromEnd(childCount - 1 - i)
                return
            }
        }
    }

    private fun fill(
        fillSpace: Int,
        itemWidth: Int,
        itemHeight: Int,
        insets: Rect,
        append: Boolean
    ): List<ViewItem> {
        val views = ArrayList<ViewItem>()
        var remainingSpace = fillSpace
        while (remainingSpace > 0) {
            val viewItem = if (append) {
                append(itemWidth, itemHeight, insets)
            } else {
                prepend(itemWidth, itemHeight, insets)
            }
            views.add(viewItem)
            remainingSpace -= viewItem.getDecoratedWidth()
        }
        return views
    }

    fun append(itemWidth: Int, itemHeight: Int, insets: Rect = EMPTY_INSETS): ViewItem {
        val lastItem = getLastView()
        val bounds = Rect()

        if (lastItem != null) {
            bounds.left = lastItem.getDecoratedRight()
            bounds.top = 0
            bounds.right = bounds.left + itemWidth
            bounds.bottom = itemHeight
        } else {
            bounds.right = itemWidth
            bounds.bottom = itemHeight
        }

        val newItem = ViewItem(
            bounds = bounds,
            insets = insets
        )
        append(newItem)
        return newItem
    }

    fun prepend(itemWidth: Int, itemHeight: Int, insets: Rect = EMPTY_INSETS): ViewItem {
        val firstItem = getFirstView()
            ?: throw IllegalStateException("Can't prepend when layout is empty")
        val right = firstItem.getDecoratedLeft()
        val item = ViewItem(
            bounds = Rect(right - itemWidth, 0, right, itemHeight),
            insets = insets
        )
        prepend(item)
        return item
    }

}
