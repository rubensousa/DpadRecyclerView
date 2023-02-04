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

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutPrefetchCollector
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.LayoutInfoMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.LayoutManagerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerViewStateMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.TestViewAdapter
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.ViewMock
import org.junit.Before
import org.junit.Test

class LayoutPrefetchCollectorTest {

    private val screenHeight = 1080
    private val screenWidth = 1920
    private val parentKeyline = screenHeight / 2
    private val viewHeight = 400
    private val childKeyline = 200
    private val childTopCentered = parentKeyline - childKeyline
    private val childBottomCentered = parentKeyline + childKeyline
    private val topView = ViewMock(futureHeight = viewHeight, futureWidth = screenWidth)
    private val bottomView = ViewMock(futureHeight = viewHeight, futureWidth = screenWidth)
    private val recyclerViewStateMock = RecyclerViewStateMock()
    private val configuration = LayoutConfiguration(RecyclerView.LayoutManager.Properties())
    private val layoutManagerMock = LayoutManagerMock(
       RecyclerMock(
            TestViewAdapter(
                viewWidth = screenWidth,
                viewHeight = viewHeight
            )
        )
    )
    private val layoutInfoMock = LayoutInfoMock(layoutManagerMock.get(), configuration)
    private val prefetchCollector = LayoutPrefetchCollector(layoutInfoMock.get())
    private val registry = PrefetchRegistry()

    @Before
    fun setup() {
        layoutInfoMock.setOrientation(RecyclerView.VERTICAL)
        topView.top = childTopCentered - viewHeight
        topView.bottom = childTopCentered
        bottomView.top = childBottomCentered
        bottomView.bottom = childBottomCentered + viewHeight
        layoutManagerMock.addView(topView.get())
        layoutManagerMock.addView(bottomView.get())
    }

    @Test
    fun `collect initial prefetch does nothing for empty data`() {
        prefetchCollector.collectInitialPrefetchPositions(
            adapterItemCount = 0,
            prefetchItemCount = 2,
            pivotPosition = 0,
            layoutPrefetchRegistry = registry
        )
        assertThat(registry.entries).isEmpty()

        prefetchCollector.collectInitialPrefetchPositions(
            adapterItemCount = 2,
            prefetchItemCount = 0,
            pivotPosition = 0,
            layoutPrefetchRegistry = registry
        )
        assertThat(registry.entries).isEmpty()
    }

    @Test
    fun `collect initial prefetch positions around pivot rounding down`() {
        prefetchCollector.collectInitialPrefetchPositions(
            adapterItemCount = 10,
            prefetchItemCount = 4,
            pivotPosition = 3,
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(layoutPosition = 2),
                PrefetchEntry(layoutPosition = 3),
                PrefetchEntry(layoutPosition = 4),
                PrefetchEntry(layoutPosition = 5)
            )
        )
    }

    @Test
    fun `collect initial prefetch positions around pivot exactly at middle`() {
        prefetchCollector.collectInitialPrefetchPositions(
            adapterItemCount = 10,
            prefetchItemCount = 5,
            pivotPosition = 3,
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(layoutPosition = 1),
                PrefetchEntry(layoutPosition = 2),
                PrefetchEntry(layoutPosition = 3),
                PrefetchEntry(layoutPosition = 4),
                PrefetchEntry(layoutPosition = 5)
            )
        )
    }

    @Test
    fun `collect initial prefetch positions limited by adapter start`() {
        prefetchCollector.collectInitialPrefetchPositions(
            adapterItemCount = 10,
            prefetchItemCount = 5,
            pivotPosition = 1,
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(layoutPosition = 0),
                PrefetchEntry(layoutPosition = 1),
                PrefetchEntry(layoutPosition = 2),
                PrefetchEntry(layoutPosition = 3),
                PrefetchEntry(layoutPosition = 4)
            )
        )
    }

    @Test
    fun `collect initial prefetch positions limited by adapter item count`() {
        prefetchCollector.collectInitialPrefetchPositions(
            adapterItemCount = 2,
            prefetchItemCount = 5,
            pivotPosition = 10,
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(layoutPosition = 0),
                PrefetchEntry(layoutPosition = 1)
            )
        )
    }

    @Test
    fun `collect adjacent start for single row`() {
        topView.layoutPosition = 1

        prefetchCollector.collectAdjacentPrefetchPositions(
            dx = 0,
            dy = -viewHeight,
            state = recyclerViewStateMock.get(),
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(
                    layoutPosition = topView.layoutPosition - 1,
                    distance = -topView.top
                )
            )
        )
    }

    @Test
    fun `collect adjacent end for single row`() {
        bottomView.layoutPosition = 2

        prefetchCollector.collectAdjacentPrefetchPositions(
            dx = 0,
            dy = viewHeight,
            state = recyclerViewStateMock.get(),
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(
                    layoutPosition = bottomView.layoutPosition + 1,
                    distance = bottomView.bottom - screenHeight
                )
            )
        )
    }

    @Test
    fun `collect adjacent start for grid`() {
        configuration.setSpanCount(3)
        topView.layoutPosition = 3

        prefetchCollector.collectAdjacentPrefetchPositions(
            dx = 0,
            dy = -viewHeight,
            state = recyclerViewStateMock.get(),
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(
                    layoutPosition = topView.layoutPosition - 1,
                    distance = -topView.top
                ),
                PrefetchEntry(
                    layoutPosition = topView.layoutPosition - 2,
                    distance = -topView.top
                ),
                PrefetchEntry(
                    layoutPosition = topView.layoutPosition - 3,
                    distance = -topView.top
                )
            )
        )
    }

    @Test
    fun `collect adjacent end for grid`() {
        configuration.setSpanCount(3)
        bottomView.layoutPosition = 11

        prefetchCollector.collectAdjacentPrefetchPositions(
            dx = 0,
            dy = viewHeight,
            state = recyclerViewStateMock.get(),
            layoutPrefetchRegistry = registry
        )

        assertThat(registry.entries).isEqualTo(
            listOf(
                PrefetchEntry(
                    layoutPosition = bottomView.layoutPosition + 1,
                    distance = bottomView.bottom - screenHeight
                ),
                PrefetchEntry(
                    layoutPosition = bottomView.layoutPosition + 2,
                    distance = bottomView.bottom - screenHeight
                ),
                PrefetchEntry(
                    layoutPosition = bottomView.layoutPosition + 3,
                    distance = bottomView.bottom - screenHeight
                )
            )
        )
    }

    private data class PrefetchEntry(val layoutPosition: Int, val distance: Int = 0)

    private class PrefetchRegistry : RecyclerView.LayoutManager.LayoutPrefetchRegistry {

        val entries = ArrayList<PrefetchEntry>()

        override fun addPosition(layoutPosition: Int, pixelDistance: Int) {
            entries.add(PrefetchEntry(layoutPosition, pixelDistance))
        }

    }

}