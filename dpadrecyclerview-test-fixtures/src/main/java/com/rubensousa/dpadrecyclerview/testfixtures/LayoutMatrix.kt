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

import androidx.collection.CircularArray
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds

abstract class LayoutMatrix(val width: Int, val height: Int) {

    companion object {
        val EMPTY_INSETS = ViewBounds()
    }

    private val circularArray = CircularArray<ViewItem>()
    private var extraLayoutSpaceStart = 0
    private var extraLayoutSpaceEnd = 0

    abstract fun scrollBy(offset: Int)

    fun setExtraLayoutSpace(start: Int = 0, end: Int = 0) {
        extraLayoutSpaceStart = start
        extraLayoutSpaceEnd = end
    }

    fun getExtraLayoutSpaceStart() = extraLayoutSpaceStart

    fun getExtraLayoutSpaceEnd() = extraLayoutSpaceEnd

    fun getFirstView(): ViewItem? {
        if (circularArray.isEmpty) {
            return null
        }
        return circularArray.first
    }

    fun getLastView(): ViewItem? {
        if (circularArray.isEmpty) {
            return null
        }
        return circularArray.last
    }

    fun getViewAt(index: Int): ViewItem {
        return circularArray.get(index)
    }

    fun getNumberOfViewsInLayout(): Int {
        return circularArray.size()
    }

    fun getViewsInLayout(): List<ViewItem> {
        return List(circularArray.size()) { index -> circularArray.get(index) }
    }

    fun clear() {
        circularArray.clear()
    }

    protected fun append(item: ViewItem) {
        circularArray.addLast(item)
    }

    protected fun prepend(item: ViewItem) {
        circularArray.addFirst(item)
    }

    protected fun recycleFromStart(count: Int) {
        circularArray.removeFromStart(count)
    }

    protected fun recycleFromEnd(count: Int) {
        circularArray.removeFromEnd(count)
    }

    protected fun forEachView(action: (item: ViewItem) -> Unit) {
        circularArray.forEach(action)
    }

    private inline fun CircularArray<ViewItem>.forEach(action: (item: ViewItem) -> Unit) {
        for (i in 0 until size()) {
            action(get(i))
        }
    }

}
