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
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import io.mockk.every
import io.mockk.mockk

class ViewMock(
    private val futureWidth: Int = 0,
    private val futureHeight: Int = 0,
) {

    private val mock = mockk<View>()
    private val layoutParams = mockk<DpadLayoutParams>(relaxed = true)

    var isRemoved = false
    var layoutPosition = 0
    var measuredWidth = 0
    var measuredHeight = 0
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0

    init {
        every { layoutParams.isItemRemoved }.answers { isRemoved }
        every { layoutParams.viewLayoutPosition }.answers { layoutPosition }
        every { mock.layoutParams }.answers { layoutParams }
        every { mock.measuredWidth }.answers { measuredWidth }
        every { mock.measuredHeight }.answers { measuredHeight }
        every { mock.left = any() }.answers { left = args[0] as Int }
        every { mock.top = any() }.answers { top = args[0] as Int }
        every { mock.right = any() }.answers { right = args[0] as Int }
        every { mock.bottom = any() }.answers { bottom = args[0] as Int }
        every { mock.left }.answers { left }
        every { mock.top }.answers { top }
        every { mock.right }.answers { right }
        every { mock.bottom }.answers { bottom }
        every { mock.measure(any(), any()) }.answers {
            measuredWidth = futureWidth
            measuredHeight = futureHeight
        }
        every { mock.offsetTopAndBottom(any()) }.answers {
            val offset = it.invocation.args.first() as Int
            top += offset
            bottom += offset
        }
        every { mock.offsetLeftAndRight(any()) }.answers {
            val offset = it.invocation.args.first() as Int
            left += offset
            right += offset
        }
    }

    fun get(): View = mock

}