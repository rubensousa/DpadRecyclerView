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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.alignment

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.ParentScrollAlignment
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.LayoutManagerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.TestViewAdapter
import org.junit.Before
import org.junit.Test

class ParentScrollAlignmentTest {

    private val alignment = ParentScrollAlignment()
    private val width = 1920
    private val height = 1080
    private val verticalViewWidth = width
    private val verticalViewHeight = 400
    private val horizontalViewWidth = 400
    private val horizontalViewHeight = height
    private val verticalViewAdapter = TestViewAdapter(
        viewWidth = verticalViewWidth,
        viewHeight = verticalViewHeight,
    )
    private val horizontalViewAdapter = TestViewAdapter(
        viewWidth = horizontalViewWidth,
        viewHeight = horizontalViewHeight
    )
    private val verticalLayoutManager = LayoutManagerMock(
        parentWidth = width,
        parentHeight = height,
        recyclerMock = RecyclerMock(verticalViewAdapter),
    )
    private val horizontalLayoutManager = LayoutManagerMock(
        parentWidth = width,
        parentHeight = height,
        recyclerMock = RecyclerMock(horizontalViewAdapter),
    )

    @Before
    fun setup() {
        alignment.defaultAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN_MAX,
            offset = 0,
            offsetRatio = 0.5f
        )
    }

    @Test
    fun `keyline for child near start edge is the start edge`() {
        updateLayoutInfo(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignment.updateStartLimit(
            edge = 0,
            viewAnchor = verticalViewHeight / 2
        )

        // Any child before the keyline should align to the start edge
        val keyline = alignment.calculateKeyline()

        val viewCenter = keyline - 100
        assertThat(alignment.calculateScrollOffset(viewCenter)).isEqualTo(0)
    }

    @Test
    fun `keyline for child near end edge is the end edge`() {
        updateLayoutInfo(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignment.updateEndLimit(
            edge = height,
            viewAnchor = height - verticalViewHeight / 2
        )

        val viewCenter = alignment.calculateKeyline() + verticalViewHeight / 2
        assertThat(alignment.calculateScrollOffset(viewCenter)).isEqualTo(0)
    }

    @Test
    fun `keyline is set correctly for regular order layout`() {
        updateLayoutInfo(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignment.defaultAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 10,
            offsetRatio = 0.5f
        )

        assertThat(alignment.calculateKeyline()).isEqualTo(
            (height * alignment.defaultAlignment.offsetRatio + alignment.defaultAlignment.offset)
                .toInt()
        )

        alignment.defaultAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 10,
            offsetRatio = 0.0f
        )

        assertThat(alignment.calculateKeyline()).isEqualTo(alignment.defaultAlignment.offset)
    }

    @Test
    fun `keyline is set correctly for reverse layout`() {
        updateLayoutInfo(orientation = RecyclerView.VERTICAL, reverseLayout = true)

        alignment.defaultAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 10,
            offsetRatio = 0.0f
        )

        assertThat(alignment.calculateKeyline())
            .isEqualTo(height - alignment.defaultAlignment.offset)

        alignment.defaultAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 10,
            offsetRatio = 0.5f
        )

        assertThat(alignment.calculateKeyline())
            .isEqualTo(
                (height * alignment.defaultAlignment.offsetRatio - alignment.defaultAlignment.offset).toInt()
            )
    }

    private fun updateLayoutInfo(orientation: Int, reverseLayout: Boolean) {
        if (orientation == RecyclerView.VERTICAL) {
            alignment.updateLayoutInfo(verticalLayoutManager.get(), orientation, reverseLayout)
        } else {
            alignment.updateLayoutInfo(horizontalLayoutManager.get(), orientation, reverseLayout)
        }
    }

}
