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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.grid

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid.GridRow
import org.junit.Test

class GridRowTest {

    private val defaultWidth = 1920
    private val defaultNumberOfSpans = 5
    private val defaultViewSize = 400
    private val defaultTop = 0

    @Test
    fun `default state is correct`() {
        val row = createRow()

        assertThat(row.startIndex).isEqualTo(RecyclerView.NO_POSITION)
        assertThat(row.endIndex).isEqualTo(RecyclerView.NO_POSITION)
        assertThat(row.height).isEqualTo(0)
        assertThat(row.top).isEqualTo(0)
        repeat(5) { index ->
            assertThat(row.getHeightAt(index)).isEqualTo(0)
        }
    }

    @Test
    fun `init sets up initial state of a row`() {
        val row = createRow()
        val spanSize = 1
        // Test init at different span indexes
        for (spanIndex in 0 until defaultNumberOfSpans) {

            row.init(
                newTop = defaultTop,
                viewSize = defaultViewSize,
                spanIndex = spanIndex,
                spanSize = spanSize
            )

            assertThat(row.height).isEqualTo(defaultViewSize)
            assertThat(row.startIndex).isEqualTo(spanIndex)
            assertThat(row.endIndex).isEqualTo(spanIndex)
            assertThat(row.top).isEqualTo(defaultTop)

            for (i in 0 until spanIndex) {
                assertThat(row.getHeightAt(i)).isEqualTo(0)
            }

            for (i in row.startIndex until row.endIndex + 1) {
                assertThat(row.getHeightAt(i)).isEqualTo(defaultViewSize)
            }

            for (i in row.endIndex + 1 until defaultNumberOfSpans) {
                assertThat(row.getHeightAt(i)).isEqualTo(0)
            }

        }
    }

    @Test
    fun `init from copies the state of another row`() {
        val startRow = createRow()
        startRow.init(
            newTop = defaultTop,
            viewSize = defaultViewSize,
            spanIndex = 1,
            spanSize = 2
        )
        val endRow = createRow()
        endRow.initFrom(startRow)

        assertThat(endRow.top).isEqualTo(startRow.top)
        assertThat(endRow.height).isEqualTo(startRow.height)
        assertThat(endRow.startIndex).isEqualTo(startRow.startIndex)
        assertThat(endRow.endIndex).isEqualTo(startRow.endIndex)
        assertThat(endRow).isEqualTo(startRow)

    }

    @Test
    fun `offset moves the top of a row`() {
        val startRow = createAndInitRow(spanIndex = 0, spanSize = 1, top = 0)

        startRow.offsetBy(500)

        assertThat(startRow.top).isEqualTo(500)

        startRow.offsetBy(-500)

        assertThat(startRow.top).isEqualTo(0)
    }

    @Test
    fun `fits end only returns true if item can be appended to the row`() {
        var row = createAndInitRow(spanIndex = 0, spanSize = 1, top = 0)
        assertThat(row.fitsEnd(spanSize = 1)).isTrue()
        assertThat(row.fitsEnd(spanSize = 4)).isTrue()
        assertThat(row.fitsEnd(spanSize = 5)).isFalse()

        row = createAndInitRow(spanIndex = 2, spanSize = 1, top = 0)

        assertThat(row.fitsEnd(spanSize = 1)).isTrue()
        assertThat(row.fitsEnd(spanSize = 3)).isFalse()

        row = createAndInitRow(spanIndex = defaultNumberOfSpans - 1, spanSize = 1, top = 0)

        assertThat(row.fitsEnd(spanSize = 1)).isFalse()
    }

    @Test
    fun `fits start only returns true if item can be prepended to the row`() {
        var row = createAndInitRow(spanIndex = 0, spanSize = 1, top = 0)
        assertThat(row.fitsStart(spanSize = 1)).isFalse()

        row = createAndInitRow(spanIndex = 2, spanSize = 1, top = 0)

        assertThat(row.fitsStart(spanSize = 1)).isTrue()
        assertThat(row.fitsStart(spanSize = 2)).isTrue()
        assertThat(row.fitsStart(spanSize = 3)).isFalse()

        row = createAndInitRow(spanIndex = defaultNumberOfSpans - 1, spanSize = 1, top = 0)

        assertThat(row.fitsStart(spanSize = 4)).isTrue()
        assertThat(row.fitsStart(spanSize = 5)).isFalse()
    }

    @Test
    fun `getSpanSpace returns the minimum size of a span`() {
        val row = createRow()
        assertThat(row.getSpanSpace()).isEqualTo(defaultWidth / defaultNumberOfSpans)
    }

    @Test
    fun `getStartOffset returns the current start position`() {
        var row =  createAndInitRow(spanIndex = 0, spanSize = 1)
        assertThat(row.getStartOffset()).isEqualTo(0)

        row =  createAndInitRow(spanIndex = 1, spanSize = 1)
        assertThat(row.getStartOffset()).isEqualTo(row.getSpanSpace())

        row =  createAndInitRow(spanIndex = 4, spanSize = 1)
        assertThat(row.getStartOffset()).isEqualTo(row.getSpanSpace() * 4)
    }

    @Test
    fun `getEndOffset returns the current end position`() {
        var row = createAndInitRow(spanIndex = 0, spanSize = 1)
        assertThat(row.getEndOffset()).isEqualTo(row.getSpanSpace())

        row =  createAndInitRow(spanIndex = 1, spanSize = 1)
        assertThat(row.getEndOffset()).isEqualTo(row.getSpanSpace() * 2)

        row =  createAndInitRow(spanIndex = 4, spanSize = 1)
        assertThat(row.getEndOffset()).isEqualTo(row.getSpanSpace() * 5)
    }

    @Test
    fun `append inserts item at the end of a row and updates row height`() {
        val row = createAndInitRow(spanIndex = 0, spanSize = 1)

        var viewLeft = row.append(viewSize = defaultViewSize, spanSize = 1)
        assertThat(viewLeft).isEqualTo(row.getSpanSpace())
        assertThat(row.getHeightAt(1)).isEqualTo(defaultViewSize)
        assertThat(row.height).isEqualTo(defaultViewSize)

        val newHeight = defaultViewSize + 50
        viewLeft = row.append(viewSize = newHeight, spanSize = 1)
        assertThat(viewLeft).isEqualTo(row.getSpanSpace() * 2)
        assertThat(row.getHeightAt(2)).isEqualTo(newHeight)
        assertThat(row.height).isEqualTo(newHeight)
    }

    @Test
    fun `prepend inserts item at the start of a row and updates row height`() {
        val row = createAndInitRow(spanIndex = 2, spanSize = 1)

        var viewLeft = row.prepend(viewSize = defaultViewSize, spanSize = 1)
        assertThat(viewLeft).isEqualTo(row.getSpanSpace())
        assertThat(row.getHeightAt(1)).isEqualTo(defaultViewSize)
        assertThat(row.height).isEqualTo(defaultViewSize)

        val newHeight = defaultViewSize + 50
        viewLeft = row.prepend(viewSize = newHeight, spanSize = 1)
        assertThat(viewLeft).isEqualTo(0)
        assertThat(row.getHeightAt(0)).isEqualTo(newHeight)
        assertThat(row.height).isEqualTo(newHeight)
    }

    private fun createRow(
        numberOfSpans: Int = defaultNumberOfSpans,
        width: Int = defaultWidth
    ): GridRow {
        return GridRow(numberOfSpans, width)
    }

    private fun createAndInitRow(
        spanIndex: Int,
        spanSize: Int,
        top: Int = defaultTop,
        viewSize: Int = defaultViewSize,
        numberOfSpans: Int = defaultNumberOfSpans,
        width: Int = defaultWidth
    ): GridRow {
        val row = createRow(numberOfSpans, width)
        row.init(top, viewSize, spanIndex, spanSize)
        return row
    }
}