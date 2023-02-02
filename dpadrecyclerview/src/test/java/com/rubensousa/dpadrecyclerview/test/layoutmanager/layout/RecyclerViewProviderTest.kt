/*
 * Copyright 2023 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.layout

import android.view.Gravity
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ItemDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider.RecyclerViewProvider
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerViewStateMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.TestViewAdapter
import org.junit.Before
import org.junit.Test

class RecyclerViewProviderTest {

    private val viewProvider = RecyclerViewProvider()
    private val recyclerMock = RecyclerMock(
        TestViewAdapter(
            viewWidth = 1000,
            viewHeight = 1000,
            numberOfItems = 20
        )
    )
    private val recycler = recyclerMock.get()
    private val stateMock = RecyclerViewStateMock()
    private val state = stateMock.get()
    private val layoutRequest = LayoutRequest()

    @Before
    fun setup() {
        layoutRequest.init(
            gravity = Gravity.START,
            isVertical = true,
            reverseLayout = false,
            infinite = false
        )
        stateMock.itemCount = recyclerMock.getItemCount()
        viewProvider.updateRecycler(recycler)
    }

    @Test
    fun `recycler reference is cleared`() {
        assertThat(viewProvider.next(layoutRequest, state)).isNotNull()

        viewProvider.clearRecycler()

        assertThat(viewProvider.next(layoutRequest, state)).isNull()
    }

    @Test
    fun `layout request is updated with primary direction`() {
        layoutRequest.setCurrentPosition(0)
        var currentPosition = layoutRequest.currentPosition
        var view = viewProvider.next(layoutRequest, state)!!
        var layoutParams = view.layoutParams as DpadLayoutParams

        assertThat(layoutParams.viewLayoutPosition).isEqualTo(currentPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(currentPosition + 1)

        layoutRequest.prepend(referencePosition = recyclerMock.getItemCount()) {}
        currentPosition = layoutRequest.currentPosition

        view = viewProvider.next(layoutRequest, state)!!
        layoutParams = view.layoutParams as DpadLayoutParams

        assertThat(layoutParams.viewLayoutPosition).isEqualTo(currentPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(currentPosition - 1)
    }

    @Test
    fun `layout request is updated with reverse direction`() {
        layoutRequest.prepend(referencePosition = -1, itemDirection = ItemDirection.TAIL) {}
        var currentPosition = layoutRequest.currentPosition
        var view = viewProvider.next(layoutRequest, state)!!
        var layoutParams = view.layoutParams as DpadLayoutParams

        assertThat(layoutParams.viewLayoutPosition).isEqualTo(currentPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(currentPosition + 1)

        layoutRequest.append(
            referencePosition = recyclerMock.getItemCount(),
            itemDirection = ItemDirection.HEAD
        ) {}
        currentPosition = layoutRequest.currentPosition

        view = viewProvider.next(layoutRequest, state)!!
        layoutParams = view.layoutParams as DpadLayoutParams

        assertThat(layoutParams.viewLayoutPosition).isEqualTo(currentPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(currentPosition - 1)
    }

    @Test
    fun `layout request is not updated if views are not found`() {
        stateMock.itemCount = 0
        val currentPosition = layoutRequest.currentPosition

        assertThat(viewProvider.hasNext(layoutPosition = 0, state)).isFalse()
        assertThat(viewProvider.next(layoutRequest, state)).isNull()
        assertThat(layoutRequest.currentPosition).isEqualTo(currentPosition)
    }

    @Test
    fun `views are bound to state count`() {
        stateMock.itemCount = 1

        layoutRequest.setCurrentPosition(position = 0)

        assertThat(viewProvider.hasNext(layoutRequest.currentPosition, state)).isTrue()
        assertThat(viewProvider.next(layoutRequest, state)).isNotNull()
        assertThat(layoutRequest.currentPosition).isEqualTo(1)

        assertThat(viewProvider.hasNext(layoutRequest.currentPosition, state)).isFalse()
        assertThat(viewProvider.next(layoutRequest, state)).isNull()

        layoutRequest.setCurrentPosition(position = -1)

        assertThat(viewProvider.hasNext(layoutRequest.currentPosition, state)).isFalse()
        assertThat(viewProvider.next(layoutRequest, state)).isNull()
        assertThat(layoutRequest.currentPosition).isEqualTo(-1)
    }
}