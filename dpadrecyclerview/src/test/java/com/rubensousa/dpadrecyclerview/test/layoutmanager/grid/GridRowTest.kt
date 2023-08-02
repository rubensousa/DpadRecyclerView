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
        assertThat(row.startOffset).isEqualTo(0)
        repeat(defaultNumberOfSpans) { index ->
            assertThat(row.getPositionAt(index)).isEqualTo(RecyclerView.NO_POSITION)
        }
    }

    @Test
    fun `constructor copies the state of another row`() {
        val startRow = createRow()
        startRow.append(
            viewSize = 500,
            viewPosition = 0,
            spanIndex = 0,
            spanSize = defaultNumberOfSpans
        )
        val endRow = GridRow(startRow)

        assertThat(endRow.startOffset).isEqualTo(startRow.startOffset)
        assertThat(endRow.endOffset).isEqualTo(startRow.endOffset)
        assertThat(endRow.height).isEqualTo(startRow.height)
        assertThat(endRow.startIndex).isEqualTo(startRow.startIndex)
        assertThat(endRow.endIndex).isEqualTo(startRow.endIndex)
        assertThat(endRow).isEqualTo(startRow)

    }

    @Test
    fun `offset moves the top of a row`() {
        val startRow = createRow(spanSize = 1, top = 0)

        startRow.offsetBy(500)

        assertThat(startRow.startOffset).isEqualTo(500)

        startRow.offsetBy(-500)

        assertThat(startRow.startOffset).isEqualTo(0)
    }

    @Test
    fun `getSpanSpace returns the minimum size of a span`() {
        val row = createRow()
        assertThat(row.getSpanSpace()).isEqualTo(defaultWidth / defaultNumberOfSpans)
    }

    @Test
    fun `correct start offset is returned for the row state`() {
        val row = createRow()
        // Row is empty, so offset should be 0
        assertThat(row.getSpanStartOffset()).isEqualTo(0)

        row.prepend(viewSize = defaultViewSize, viewPosition = 3, spanIndex = 3, spanSize = 1)
        assertThat(row.getSpanStartOffset()).isEqualTo(row.getSpanSpace() * 3)

        row.prepend(viewSize = defaultViewSize, viewPosition = 2, spanIndex = 0, spanSize = 4)
        assertThat(row.getSpanStartOffset()).isEqualTo(0)
    }

    @Test
    fun `correct end offset is returned for the row state`() {
        val row = createRow()
        // Row is empty, so offset should be 0
        assertThat(row.getSpanEndOffset()).isEqualTo(0)

        // Appending an item moves the end offset
        row.append(viewSize = defaultViewSize, viewPosition = 0, spanIndex = 0, spanSize = 1)
        assertThat(row.getSpanEndOffset()).isEqualTo(row.getSpanSpace())

        row.append(viewSize = defaultViewSize, viewPosition = 1, spanIndex = 1, spanSize = 4)
        assertThat(row.getSpanEndOffset()).isEqualTo(row.getWidth())
    }

    @Test
    fun `append inserts item at the end of a row and updates row height`() {
        val row = createRow()

        row.append(viewSize = defaultViewSize, viewPosition = 0, spanIndex = 0, spanSize = 1)
        assertThat(row.startIndex).isEqualTo(0)
        assertThat(row.endIndex).isEqualTo(0)
        assertThat(row.height).isEqualTo(defaultViewSize)

        val newHeight = defaultViewSize + 50
        row.append(viewSize = newHeight, viewPosition = 1, spanIndex = 1, spanSize = 1)
        assertThat(row.height).isEqualTo(newHeight)
        assertThat(row.startIndex).isEqualTo(0)
        assertThat(row.endIndex).isEqualTo(1)
    }

    @Test
    fun `prepend inserts item at the start of a row and updates row height`() {
        val row = createRow()

        row.prepend(
            viewSize = defaultViewSize,
            viewPosition = 0,
            spanIndex = 0,
            spanSize = 1
        )

        assertThat(row.getSpanStartOffset()).isEqualTo(0)
        assertThat(row.height).isEqualTo(defaultViewSize)

        val newHeight = defaultViewSize + 50
        row.prepend(viewSize = newHeight, viewPosition = 1, spanIndex = 1, spanSize = 1)
        assertThat(row.height).isEqualTo(newHeight)
    }

    private fun createRow(
        numberOfSpans: Int = defaultNumberOfSpans,
        width: Int = defaultWidth
    ): GridRow {
        return GridRow(numberOfSpans, width)
    }

    private fun createRow(
        spanSize: Int,
        top: Int = defaultTop,
        viewSize: Int = defaultViewSize,
        numberOfSpans: Int = defaultNumberOfSpans,
        width: Int = defaultWidth
    ): GridRow {
        val row = createRow(numberOfSpans, width)
        row.offsetBy(top)
        row.append(viewSize, viewPosition = 0, spanIndex = 0, spanSize)
        return row
    }
}