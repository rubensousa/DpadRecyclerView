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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.mock

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import io.mockk.every
import io.mockk.mockk

internal class LayoutManagerMock(
    val recyclerMock: RecyclerMock,
    val parentWidth: Int = 1920,
    val parentHeight: Int = 1080
) {

    private val mock = mockk<LayoutManager>()
    private val views = ArrayList<ViewEntry>()
    private val hiddenViews = ArrayList<View>()

    var leftInset: Int = 0
    var topInset: Int = 0
    var rightInset: Int = 0
    var bottomInset: Int = 0

    var leftPadding = 0
    var topPadding = 0
    var rightPadding = 0
    var bottomPadding = 0

    init {
        every { mock.itemCount }.answers { recyclerMock.getItemCount() }
        every { mock.addView(any()) }.answers {
            addView(it.invocation.args.first() as View, false)
        }
        every { mock.addView(any(), any()) }.answers {
            val args = it.invocation.args
            addViewAt(args[0] as View, args[1] as Int, false)
        }
        every { mock.addDisappearingView(any()) }.answers {
            addView(it.invocation.args.first() as View, true)
        }
        every { mock.addDisappearingView(any(), any()) }.answers {
            val args = it.invocation.args
            addViewAt(args[0] as View, args[1] as Int, true)
        }
        every { mock.measureChildWithMargins(any(), any(), any()) }.answers {
            val view = it.invocation.args.first() as View
            view.measure(it.invocation.args[1] as Int, it.invocation.args[2] as Int)
        }
        every { mock.findViewByPosition(any()) }.answers {
            val position = it.invocation.args.first() as Int
            views.firstOrNull { viewEntry ->
                (viewEntry.view.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition == position
            }?.view
        }
        every { mock.paddingLeft }.answers { leftPadding }
        every { mock.paddingTop }.answers { topPadding }
        every { mock.paddingRight }.answers { rightPadding }
        every { mock.paddingBottom }.answers { bottomPadding }
        every { mock.height }.answers { parentHeight }
        every { mock.width }.answers { parentWidth }
        every { mock.childCount }.answers { views.size + hiddenViews.size }
        every { mock.getChildAt(any()) }.answers { views[it.invocation.args.first() as Int].view }

        mockDecorations()
        every { mock.layoutDecoratedWithMargins(any(), any(), any(), any(), any()) }.answers {
            val args = it.invocation.args
            val view = args[0] as View
            view.left = args[1] as Int
            view.top = args[2] as Int
            view.right = args[3] as Int
            view.bottom = args[4] as Int
        }
        every { mock.offsetChildrenVertical(any()) }.answers {
            val offset = it.invocation.args.first() as Int
            views.forEach { entry ->
                entry.offsetVertical(offset)
            }
        }
        every { mock.offsetChildrenHorizontal(any()) }.answers {
            val offset = it.invocation.args.first() as Int
            views.forEach { entry ->
                entry.offsetHorizontal(offset)
            }
        }
        every { mock.removeAndRecycleView(any(), any()) }.answers {
            val view = it.invocation.args.first() as View
            views.removeIf { entry -> entry.view === view }
        }
        every { mock.detachAndScrapAttachedViews(any()) }.answers {
            recyclerMock.setScrap(views.map { it.view })
            views.clear()
        }
    }

    fun get(): LayoutManager = mock

    private fun mockDecorations() {
        every { mock.getLeftDecorationWidth(any()) }.answers { leftInset }
        every { mock.getTopDecorationHeight(any()) }.answers { topInset }
        every { mock.getRightDecorationWidth(any()) }.answers { rightInset }
        every { mock.getBottomDecorationHeight(any()) }.answers { bottomInset }
        every { mock.getDecoratedMeasuredHeight(any()) }.answers {
            val view = it.invocation.args.first() as View
            view.measuredHeight + topInset + bottomInset
        }
        every { mock.getDecoratedMeasuredWidth(any()) }.answers {
            val view = it.invocation.args.first() as View
            view.measuredWidth + leftInset + rightInset
        }
        every { mock.getDecoratedLeft(any()) }.answers {
            val view = it.invocation.args.first() as View
            view.left - mock.getLeftDecorationWidth(view)
        }
        every { mock.getDecoratedTop(any()) }.answers {
            val view = it.invocation.args.first() as View
            view.top - mock.getTopDecorationHeight(view)
        }
        every { mock.getDecoratedRight(any()) }.answers {
            val view = it.invocation.args.first() as View
            view.right + mock.getRightDecorationWidth(view)
        }
        every { mock.getDecoratedBottom(any()) }.answers {
            val view = it.invocation.args.first() as View
            view.bottom + mock.getBottomDecorationHeight(view)
        }
        every { mock.getTransformedBoundingBox(any(), any(), any()) } answers {
            val view = it.invocation.args.first() as View
            val rect = it.invocation.args.last() as Rect
            rect.left = mock.getDecoratedLeft(view)
            rect.top = mock.getDecoratedTop(view)
            rect.right = mock.getDecoratedRight(view)
            rect.bottom = mock.getDecoratedBottom(view)
        }
    }

    fun addView(view: View, disappearing: Boolean = false) {
        views.add(ViewEntry(view, disappearing))
    }

    fun addViewAt(view: View, index: Int, disappearing: Boolean = false) {
        views.add(index, ViewEntry(view, disappearing))
    }

    data class ViewEntry(
        val view: View,
        val isDisappearing: Boolean
    ) {

        fun offsetVertical(dy: Int) {
            view.offsetTopAndBottom(dy)
        }

        fun offsetHorizontal(dx: Int) {
            view.offsetLeftAndRight(dx)
        }

    }

}
