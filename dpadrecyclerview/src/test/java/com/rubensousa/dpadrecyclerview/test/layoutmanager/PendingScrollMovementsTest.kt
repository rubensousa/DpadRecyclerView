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

package com.rubensousa.dpadrecyclerview.test.layoutmanager

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.PendingScrollMovements
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.LayoutInfoMock
import org.junit.Before
import org.junit.Test

class PendingScrollMovementsTest {

    private val configuration = LayoutConfiguration(RecyclerView.LayoutManager.Properties())
    private val mockLayoutInfo = LayoutInfoMock(configuration)
    private lateinit var scrollMovements: PendingScrollMovements

    @Before
    fun setup() {
        scrollMovements = PendingScrollMovements(mockLayoutInfo.get())
    }

    @Test
    fun `increasing pending moves does not go over the limit`() {
        scrollMovements.setMaxPendingMoves(5)

        repeat(10) {
            scrollMovements.increase()
        }

        assertThat(scrollMovements.pendingMoves).isEqualTo(5)
    }

    @Test
    fun `setMaxPendingMoves does not allow invalid values`() {
        scrollMovements.setMaxPendingMoves(2)
        assertThat(scrollMovements.maxPendingMoves).isEqualTo(2)

        scrollMovements.setMaxPendingMoves(-1)
        assertThat(scrollMovements.maxPendingMoves).isEqualTo(1)

        scrollMovements.setMaxPendingMoves(0)
        assertThat(scrollMovements.maxPendingMoves).isEqualTo(1)
    }

    @Test
    fun `decreasing pending moves does not go under the limit`() {
        scrollMovements.setMaxPendingMoves(5)

        repeat(10) {
            scrollMovements.decrease()
        }

        assertThat(scrollMovements.pendingMoves).isEqualTo(-5)
    }

    @Test
    fun `shouldStopScrolling is true if we don't have pending moves`() {
        scrollMovements.setMaxPendingMoves(5)
        repeat(10) {
            scrollMovements.increase()
        }
        // Consume all events
        while (scrollMovements.consume()) {
        }

        assertThat(scrollMovements.shouldStopScrolling()).isTrue()
    }

    @Test
    fun `shouldStopScrolling is true if layout is complete in scroll direction`() {
        scrollMovements.setMaxPendingMoves(5)

        repeat(10) {
            scrollMovements.increase()
        }

        mockLayoutInfo.hasCreatedLastItem = true

        assertThat(scrollMovements.shouldStopScrolling()).isTrue()

        repeat(20) {
            scrollMovements.decrease()
        }

        mockLayoutInfo.hasCreatedFirstItem = true

        assertThat(scrollMovements.shouldStopScrolling()).isTrue()
    }

    @Test
    fun `consume only consumes one event`() {
        assertThat(scrollMovements.consume()).isFalse()

        scrollMovements.increase()

        assertThat(scrollMovements.consume()).isTrue()
        assertThat(scrollMovements.consume()).isFalse()

        scrollMovements.decrease()
        assertThat(scrollMovements.consume()).isTrue()
        assertThat(scrollMovements.consume()).isFalse()
    }

    @Test
    fun `shouldScrollToView returns true if view is in scrolling direction or is already the pivot`() {
        scrollMovements.increase()
        assertThat(
            scrollMovements.shouldScrollToView(
                viewPosition = 5,
                pivotPosition = 6
            )
        ).isFalse()
        assertThat(scrollMovements.shouldScrollToView(viewPosition = 7, pivotPosition = 6)).isTrue()
        assertThat(scrollMovements.shouldScrollToView(viewPosition = 6, pivotPosition = 6)).isTrue()

        scrollMovements.decrease()
        scrollMovements.decrease()
        assertThat(
            scrollMovements.shouldScrollToView(
                viewPosition = 7,
                pivotPosition = 6
            )
        ).isFalse()
        assertThat(scrollMovements.shouldScrollToView(viewPosition = 5, pivotPosition = 6)).isTrue()
        assertThat(scrollMovements.shouldScrollToView(viewPosition = 6, pivotPosition = 6)).isTrue()
    }


}