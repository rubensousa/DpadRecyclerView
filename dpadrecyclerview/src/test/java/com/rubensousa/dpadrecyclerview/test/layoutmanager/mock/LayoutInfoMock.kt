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

import android.view.View
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import io.mockk.every
import io.mockk.mockk

internal class LayoutInfoMock(
    private val configuration: LayoutConfiguration
) {

    private val mock = mockk<LayoutInfo>()
    private val realInstance = LayoutInfo(mockk(), configuration)
    var spanCount = 1
    var childCount = 0
    var isVertical = true
    var reverseLayout = false
    var isScrolling = false
    var totalSpace = 0
    var hasCreatedFirstItem = false
    var hasCreatedLastItem = false
    var endAfterPadding = 0
    var startAfterPadding = 0
    var childClosestToStart: View? = null
    var childClosestToEnd: View? = null

    init {
        every { mock.isVertical() }.answers { isVertical }
        every { mock.getDecoratedStart(any()) }.answers {
            val view = it.invocation.args.first() as View
            if (isVertical) {
                view.top
            } else {
                view.left
            }
        }
        every { mock.getDecoratedEnd(any()) }.answers {
            val view = it.invocation.args.first() as View
            if (isVertical) {
                view.bottom
            } else {
                view.right
            }
        }
        every { mock.getStartAfterPadding() }.answers { startAfterPadding }
        every { mock.getEndAfterPadding() }.answers { endAfterPadding }
        every { mock.isScrolling }.answers { isScrolling }
        every { mock.getTotalSpace() }.answers { totalSpace }
        every { mock.getConfiguration() }.answers { configuration }
        every { mock.hasCreatedFirstItem() }.answers { hasCreatedFirstItem }
        every { mock.hasCreatedLastItem() }.answers { hasCreatedLastItem }
        every { mock.getChildClosestToEnd() }.answers { childClosestToEnd }
        every { mock.getChildClosestToStart() }.answers { childClosestToStart }
        every { mock.getChildCount() }.answers { childCount }
        every { mock.getSpanCount() }.answers { spanCount }
        every { mock.shouldReverseLayout() }.answers { reverseLayout }
        every { mock.getLayoutPositionOf(any()) }.answers {
            realInstance.getLayoutPositionOf(it.invocation.args.first() as View)
        }
        every { mock.getSpanSize(any()) }.answers {
            realInstance.getSpanSize(it.invocation.args.first() as Int)
        }
    }

    fun get(): LayoutInfo = mock


}
