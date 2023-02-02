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
import android.view.View
import androidx.collection.forEach
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ItemDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider.ScrapViewProvider
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerViewStateMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.ViewHolderMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.ViewMock
import org.junit.Before
import org.junit.Test

class ScrapViewProviderTest {

    private val scrapViewProvider = ScrapViewProvider()
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
        stateMock.itemCount = 20
    }

    @Test
    fun `scrap does not return anything if it is empty`() {
        scrapViewProvider.update(null)
        assertThat(scrapViewProvider.next(layoutRequest, state)).isNull()
    }

    @Test
    fun `scrap does not contain removed ViewHolders`() {
        val scrap = ArrayList<RecyclerView.ViewHolder>()
        repeat(10) { layoutPosition ->
            scrap.add(createViewHolder(layoutPosition, isRemoved = true))
        }
        scrapViewProvider.update(scrap)

        var foundViewHolder = false
        scrapViewProvider.getScrap()!!.forEach { _, _ ->
            foundViewHolder = true
        }
        assertThat(foundViewHolder).isFalse()
        layoutRequest.setCurrentPosition(0)
        assertThat(scrapViewProvider.next(layoutRequest, state)).isNull()
    }

    @Test
    fun `scrap returns next view in standard layout direction`() {
        val scrap = ArrayList<RecyclerView.ViewHolder>()
        val currentLayoutPosition = 5

        val startScrapCount = 3
        val endScrapCount = 3

        repeat(startScrapCount) { index ->
            scrap.add(createViewHolder(layoutPosition = currentLayoutPosition - 1 - index))
        }

        repeat(endScrapCount) { index ->
            scrap.add(createViewHolder(layoutPosition = currentLayoutPosition + 1 + index))
        }

        scrapViewProvider.update(scrap)

        // Prepend should only find views after the current layout position
        layoutRequest.prepend(currentLayoutPosition) {}

        repeat(startScrapCount) { index ->
            val view = scrapViewProvider.next(layoutRequest, state)!!
            assertViewLayoutPosition(view, currentLayoutPosition - 1 - index)
        }

        // Append should only find views after the current layout position
        layoutRequest.append(currentLayoutPosition) {}

        repeat(endScrapCount) { index ->
            val view = scrapViewProvider.next(layoutRequest, state)!!
            assertViewLayoutPosition(view, currentLayoutPosition + 1 + index)
        }

    }

    @Test
    fun `layout request is updated with the previous scrap position`() {
        val scrap = ArrayList<RecyclerView.ViewHolder>()
        val currentLayoutPosition = 10
        val firstScrapLayoutPosition = 8
        val secondScrapLayoutPosition = 6

        scrap.add(createViewHolder(firstScrapLayoutPosition))
        scrap.add(createViewHolder(secondScrapLayoutPosition))

        layoutRequest.prepend(currentLayoutPosition) {}

        scrapViewProvider.update(scrap)

        var view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, firstScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(secondScrapLayoutPosition)

        view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, secondScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun `layout request is updated with the previous scrap position in reverse layout`() {
        val scrap = ArrayList<RecyclerView.ViewHolder>()
        val currentLayoutPosition = 10
        val firstScrapLayoutPosition = 8
        val secondScrapLayoutPosition = 6

        scrap.add(createViewHolder(firstScrapLayoutPosition))
        scrap.add(createViewHolder(secondScrapLayoutPosition))

        layoutRequest.append(currentLayoutPosition, itemDirection = ItemDirection.HEAD) {}

        scrapViewProvider.update(scrap)

        var view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, firstScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(secondScrapLayoutPosition)

        view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, secondScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun `layout request is updated with the next scrap position`() {
        val scrap = ArrayList<RecyclerView.ViewHolder>()
        val currentLayoutPosition = 10
        val firstScrapLayoutPosition = 12
        val secondScrapLayoutPosition = 14

        scrap.add(createViewHolder(firstScrapLayoutPosition))
        scrap.add(createViewHolder(secondScrapLayoutPosition))

        layoutRequest.append(currentLayoutPosition) {}

        scrapViewProvider.update(scrap)

        var view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, firstScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(secondScrapLayoutPosition)

        view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, secondScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun `layout request is updated with the next scrap position in reverse layout`() {
        val scrap = ArrayList<RecyclerView.ViewHolder>()
        val currentLayoutPosition = 10
        val firstScrapLayoutPosition = 12
        val secondScrapLayoutPosition = 14

        scrap.add(createViewHolder(firstScrapLayoutPosition))
        scrap.add(createViewHolder(secondScrapLayoutPosition))

        layoutRequest.prepend(currentLayoutPosition, itemDirection = ItemDirection.TAIL) {}

        scrapViewProvider.update(scrap)

        var view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, firstScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(secondScrapLayoutPosition)

        view = scrapViewProvider.next(layoutRequest, state)!!

        assertViewLayoutPosition(view, secondScrapLayoutPosition)
        assertThat(layoutRequest.currentPosition).isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun `layout request is updated with next scrap position`() {
        val viewHolders = ArrayList<RecyclerView.ViewHolder>()
        val currentLayoutPosition = 10
        val scrapPosition = currentLayoutPosition + 2
        viewHolders.add(createViewHolder(scrapPosition))

        scrapViewProvider.update(viewHolders)

        layoutRequest.setCurrentPosition(currentLayoutPosition)

        scrapViewProvider.updateLayoutPosition(layoutRequest)

        assertThat(layoutRequest.currentPosition).isEqualTo(scrapPosition)
    }

    private fun assertViewLayoutPosition(view: View, layoutPosition: Int) {
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        assertThat(layoutParams.viewLayoutPosition).isEqualTo(layoutPosition)
    }

    private fun createViewHolder(
        layoutPosition: Int,
        isRemoved: Boolean = false
    ): RecyclerView.ViewHolder {
        val viewMock = ViewMock()
        viewMock.layoutPosition = layoutPosition
        viewMock.isRemoved = isRemoved
        val viewHolderMock = ViewHolderMock(viewMock.get())
        return viewHolderMock.get()
    }

}
