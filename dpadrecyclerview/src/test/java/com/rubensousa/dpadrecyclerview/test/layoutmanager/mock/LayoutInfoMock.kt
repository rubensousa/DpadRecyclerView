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
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import io.mockk.every
import io.mockk.mockk

internal class LayoutInfoMock(
    layoutManager: LayoutManager,
    private val configuration: LayoutConfiguration
) {

    private val mock = mockk<LayoutInfo>()
    private val realInstance = LayoutInfo(layoutManager, configuration)
    var isInfinite = false
    var reverseLayout = false
    var isScrolling = false
    var isLoopingAllowed = false
    var isLoopingStart = false
    var isScrollingToTarget = false
    var hasCreatedFirstItem = false
    var hasCreatedLastItem = false

    init {
        every { mock.isLoopingStart }.answers { isLoopingStart }
        every { mock.isLoopingAllowed }.answers { isLoopingAllowed }
        every { mock.updateLoopingState(any(), any()) }.answers {
            isLoopingStart = it.invocation.args[0] as Boolean
            isLoopingAllowed = it.invocation.args[1] as Boolean
        }
        every { mock.isVertical() }.answers { realInstance.isVertical() }
        every { mock.isHorizontal() }.answers { realInstance.isHorizontal() }
        every { mock.isInfinite() }.answers { isInfinite }
        every { mock.isViewFocusable(any()) }.answers { true }
        every { mock.isScrollingToTarget }.answers { isScrollingToTarget }
        every { mock.getDecoratedStart(any()) }.answers {
            realInstance.getDecoratedStart(it.invocation.args.first() as View)
        }
        every { mock.getDecoratedEnd(any()) }.answers {
            realInstance.getDecoratedEnd(it.invocation.args.first() as View)
        }
        every { mock.getDecoratedSize(any()) }.answers {
            realInstance.getDecoratedSize(it.invocation.args.first() as View)
        }
        every { mock.getStartAfterPadding() }.answers { realInstance.getStartAfterPadding() }
        every { mock.getEndAfterPadding() }.answers { realInstance.getEndAfterPadding() }
        every { mock.isScrolling }.answers { isScrolling }
        every { mock.getTotalSpace() }.answers { realInstance.getTotalSpace() }
        every { mock.getConfiguration() }.answers { configuration }
        every { mock.hasCreatedFirstItem() }.answers { hasCreatedFirstItem }
        every { mock.hasCreatedLastItem() }.answers { hasCreatedLastItem }
        every { mock.getChildViewHolder(any()) }.answers {
            val view = it.invocation.args.first() as View
            ViewHolderMock(view).get()
        }
        every { mock.getChildClosestToEnd() }.answers {
            realInstance.getChildClosestToEnd()
        }
        every { mock.getChildClosestToStart() }.answers {
            realInstance.getChildClosestToStart()
        }
        every { mock.findFirstAddedPosition() }.answers {
            realInstance.findFirstAddedPosition()
        }
        every { mock.findLastAddedPosition() }.answers {
            realInstance.findLastAddedPosition()
        }
        every { mock.findViewByPosition(any()) }.answers {
            realInstance.findViewByPosition(it.invocation.args.first() as Int)
        }
        every { mock.getChildAt(any()) }.answers {
            realInstance.getChildAt(it.invocation.args.first() as Int)
        }
        every { mock.getChildCount() }.answers { realInstance.getChildCount() }
        every { mock.getSpanCount() }.answers {  realInstance.getSpanCount() }
        every { mock.shouldReverseLayout() }.answers { reverseLayout }
        every { mock.getLayoutPositionOf(any()) }.answers {
            realInstance.getLayoutPositionOf(it.invocation.args.first() as View)
        }
        every { mock.getSpanSize(any()) }.answers {
            realInstance.getSpanSize(it.invocation.args.first() as Int)
        }
        every { mock.getPerpendicularDecoratedSize(any()) }.answers {
            realInstance.getPerpendicularDecoratedSize(it.invocation.args.first() as View)
        }
        every { mock.orientationHelper }.answers {
            realInstance.orientationHelper
        }
    }

    fun get(): LayoutInfo = mock

    fun setOrientation(orientation: Int) {
        configuration.setOrientation(orientation)
        realInstance.updateOrientation()
    }


}
