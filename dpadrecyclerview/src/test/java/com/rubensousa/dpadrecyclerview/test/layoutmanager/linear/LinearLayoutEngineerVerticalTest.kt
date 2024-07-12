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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.linear

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ItemChanges
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.OnChildLayoutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutEngineer
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.LayoutInfoMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.LayoutManagerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerViewStateMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.TestViewAdapter
import com.rubensousa.dpadrecyclerview.testfixtures.ColumnLayout
import com.rubensousa.dpadrecyclerview.testfixtures.LayoutConfig
import com.rubensousa.dpadrecyclerview.testfixtures.LayoutManagerAssertions
import org.junit.Before
import org.junit.Test

class LinearLayoutEngineerVerticalTest {

    private val screenWidth = 1920
    private val screenHeight = 1080
    private val viewWidth = screenWidth
    private val viewHeight = 280
    private val itemCount = 100
    private val viewCenter = screenHeight / 2 - viewHeight / 2
    private val viewAdapter = TestViewAdapter(viewWidth, viewHeight, numberOfItems = itemCount)
    private val listener = Listener()
    private val recyclerMock = RecyclerMock(viewAdapter)
    private val layoutManagerMock = LayoutManagerMock(recyclerMock)
    private val layoutManager: RecyclerView.LayoutManager = layoutManagerMock.get()
    private val recyclerViewStateMock = RecyclerViewStateMock()
    private val layoutInfoMock = LayoutInfoMock(
        layoutManager,
        LayoutConfiguration(RecyclerView.LayoutManager.Properties()).also {
            it.setOrientation(RecyclerView.VERTICAL)
        }
    )
    private val itemChanges = ItemChanges()

    private val column = ColumnLayout(
        LayoutConfig(
            parentWidth = screenWidth,
            parentHeight = screenHeight,
            viewWidth = viewWidth,
            viewHeight = viewHeight,
            defaultItemCount = itemCount,
            parentKeyline = screenHeight / 2,
            alignToStartEdge = true,
            childKeyline = 0.5f
        )
    )
    private lateinit var engineer: LinearLayoutEngineer

    @Before
    fun setup() {
        recyclerViewStateMock.itemCount = itemCount
        engineer = LinearLayoutEngineer(
            layoutManagerMock.get(), layoutInfoMock.get(), LayoutAlignment(
                layoutManager, layoutInfoMock.get()
            ), listener
        )
    }

    @Test
    fun `initial layout pass places views in their correct places and in order`() {
        column.init(position = 0)
        layout(pivotPosition = 0)

        LayoutManagerAssertions.assertChildrenBounds(layoutManager, column)

        assertThat(listener.created).hasSize(column.getChildCount())
        assertThat(listener.laidOut).hasSize(column.getChildCount())
    }

    @Test
    fun `second layout pass keeps layout integrity`() {
        column.init(position = 0)
        layout(pivotPosition = 0)
        LayoutManagerAssertions.assertChildrenBounds(layoutManager, column)

        column.init(position = 3)
        layout(pivotPosition = 3)
        LayoutManagerAssertions.assertChildrenBounds(layoutManager, column)
    }

    @Test
    fun `pivot moves against remaining scroll for a future alignment`() {
        column.init(position = 0)
        column.scrollBy(viewHeight / 2)

        recyclerViewStateMock.remainingScrollVertical = -viewHeight / 2
        layout(pivotPosition = 1)

        LayoutManagerAssertions.assertChildrenBounds(layoutManager, column)
    }

    @Test
    fun `do not allow values of remainingScroll too high`() {
        column.init(position = 3)
        recyclerViewStateMock.remainingScrollVertical = column.getSize() * 2
        layout(pivotPosition = 3)
        LayoutManagerAssertions.assertChildrenBounds(layoutManager, column)
    }

    private fun layout(pivotPosition: Int) {
        engineer.onLayoutStarted(recyclerViewStateMock.get())
        engineer.layoutChildren(
            pivotPosition,
            recyclerMock.get(),
            recyclerViewStateMock.get()
        )
    }


    class Listener : OnChildLayoutListener {

        val created = ArrayList<View>()
        val laidOut = ArrayList<View>()

        override fun onChildCreated(view: View) {
            created.add(view)
        }

        override fun onChildLaidOut(view: View) {
            laidOut.add(view)
        }

        override fun onBlockLaidOut() {

        }

    }

}
