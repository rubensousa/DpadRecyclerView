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
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.ParentAlignmentCalculator
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.LayoutManagerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.RecyclerMock
import com.rubensousa.dpadrecyclerview.test.layoutmanager.mock.TestViewAdapter
import org.junit.Test

class ParentAlignmentCalculatorTest {

    private val alignmentCalculator = ParentAlignmentCalculator()
    private val width = 1920
    private val height = 1080
    private val verticalCenterKeyline = height / 2
    private val horizontalCenterKeyline = width / 2
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
    private val centerParentAlignment = ParentAlignment(
        edge = ParentAlignment.Edge.MIN_MAX,
        offset = 0,
        offsetRatio = 0.5f
    )

    @Test
    fun `keyline for child near start edge is the start edge`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignmentCalculator.updateStartLimit(
            edge = 0,
            viewAnchor = verticalViewHeight / 2,
            alignment = centerParentAlignment
        )

        // Any child before the keyline should align to the start edge
        val keyline = alignmentCalculator.calculateKeyline(centerParentAlignment)

        val viewCenter = keyline - 100
        assertThat(alignmentCalculator.calculateScrollOffset(viewCenter, centerParentAlignment))
            .isEqualTo(0)
    }

    @Test
    fun `keyline for child near end edge is the end edge`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignmentCalculator.updateEndLimit(
            edge = height,
            viewAnchor = height - verticalViewHeight / 2,
            alignment = centerParentAlignment
        )

        val viewCenter =
            alignmentCalculator.calculateKeyline(centerParentAlignment) + verticalViewHeight / 2
        assertThat(alignmentCalculator.calculateScrollOffset(viewCenter, centerParentAlignment))
            .isEqualTo(0)
    }

    @Test
    fun `keyline is set correctly for regular order layout`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        var minAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 10,
            offsetRatio = 0.5f
        )

        assertThat(alignmentCalculator.calculateKeyline(minAlignment)).isEqualTo(
            (height * minAlignment.offsetRatio + minAlignment.offset).toInt()
        )

        minAlignment = minAlignment.copy(offset = 10, offsetRatio = 0.0f)

        assertThat(alignmentCalculator.calculateKeyline(minAlignment)).isEqualTo(minAlignment.offset)
    }

    @Test
    fun `keyline is set correctly for reverse layout`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = true)

        var minAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 10,
            offsetRatio = 0.0f
        )

        assertThat(alignmentCalculator.calculateKeyline(minAlignment)).isEqualTo(height - minAlignment.offset)

        minAlignment = minAlignment.copy(offsetRatio = 0.5f)

        assertThat(alignmentCalculator.calculateKeyline(minAlignment))
            .isEqualTo(
                (height * minAlignment.offsetRatio - minAlignment.offset).toInt()
            )
    }

    @Test
    fun `child is aligned to end edge in regular layout direction`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignmentCalculator.updateEndLimit(
            edge = height / 2 + verticalViewHeight / 2,
            viewAnchor = verticalViewHeight / 2,
            alignment = centerParentAlignment
        )
        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = height / 2,
                alignment = centerParentAlignment
            )
        ).isEqualTo(alignmentCalculator.endScrollLimit)

    }

    @Test
    fun `child is aligned to start edge in regular layout direction`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignmentCalculator.updateStartLimit(
            edge = height / 2 - verticalViewHeight / 2,
            viewAnchor = verticalViewHeight / 2,
            alignment = centerParentAlignment
        )

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = height / 2,
                alignment = centerParentAlignment
            )
        ).isEqualTo(alignmentCalculator.startScrollLimit)
    }

    @Test
    fun `child is aligned to end edge in reverse layout`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = true)

        alignmentCalculator.updateEndLimit(
            edge = height / 2 + verticalViewHeight / 2,
            viewAnchor = verticalViewHeight / 2,
            alignment = centerParentAlignment
        )

        assertThat(alignmentCalculator.endScrollLimit)
            .isEqualTo(height / 2 + verticalViewHeight / 2 - height)

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = height / 2,
                alignment = centerParentAlignment
            )
        ).isEqualTo(alignmentCalculator.endScrollLimit)
    }

    @Test
    fun `child is aligned to start edge in reverse layout`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = true)

        alignmentCalculator.updateStartLimit(
            edge = height / 2 - verticalViewHeight / 2,
            viewAnchor = verticalViewHeight / 2,
            alignment = centerParentAlignment
        )

        assertThat(alignmentCalculator.startScrollLimit).isEqualTo(height / 2 - verticalViewHeight / 2)

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = height / 2,
                alignment = centerParentAlignment
            )
        ).isEqualTo(alignmentCalculator.startScrollLimit)
    }

    @Test
    fun `child is aligned to start edge if client does not prefer keyline`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 0,
            offsetRatio = 0.5f,
            preferKeylineOverEdge = false
        )
        alignmentCalculator.updateStartLimit(
            edge = verticalCenterKeyline - verticalViewHeight / 2,
            viewAnchor = verticalCenterKeyline,
            alignment = alignment
        )
        alignmentCalculator.updateEndLimit(
            edge = verticalCenterKeyline + verticalViewHeight / 2,
            viewAnchor = verticalCenterKeyline,
            alignment = alignment
        )

        val distanceToStart = verticalCenterKeyline - verticalViewHeight / 2

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = verticalCenterKeyline,
                alignment = alignment
            )
        ).isEqualTo(distanceToStart)
    }

    @Test
    fun `child is aligned to end edge if client does not prefer keyline`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MAX,
            offset = 0,
            offsetRatio = 0.5f,
            preferKeylineOverEdge = false
        )
        alignmentCalculator.updateStartLimit(
            edge = -verticalViewHeight / 2,
            viewAnchor = verticalViewHeight / 2,
            alignment = alignment
        )
        alignmentCalculator.updateEndLimit(
            edge = verticalCenterKeyline + verticalViewHeight,
            viewAnchor = verticalCenterKeyline + verticalViewHeight / 2,
            alignment = alignment
        )

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = verticalCenterKeyline + verticalViewHeight / 2,
                alignment = alignment
            )
        ).isEqualTo(alignmentCalculator.endScrollLimit)
    }

    @Test
    fun `child is not aligned to start edge if client prefers keyline`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 0,
            offsetRatio = 0.5f,
            preferKeylineOverEdge = true
        )
        alignmentCalculator.updateStartLimit(
            edge = verticalCenterKeyline - verticalViewHeight / 2,
            viewAnchor = verticalCenterKeyline,
            alignment = alignment
        )
        alignmentCalculator.updateEndLimit(
            edge = verticalCenterKeyline + verticalViewHeight / 2,
            viewAnchor = verticalCenterKeyline,
            alignment = alignment
        )

        val distanceToKeyline = 0

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = height / 2,
                alignment = alignment
            )
        ).isEqualTo(distanceToKeyline)
    }

    @Test
    fun `child is not aligned to end edge if client prefers keyline`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MAX,
            offset = 0,
            offsetRatio = 0.5f,
            preferKeylineOverEdge = true
        )
        alignmentCalculator.updateEndLimit(
            edge = verticalCenterKeyline + verticalViewHeight / 2,
            viewAnchor = verticalCenterKeyline,
            alignment = alignment
        )

        val distanceToKeyline = 0

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = verticalCenterKeyline,
                alignment = alignment
            )
        ).isEqualTo(distanceToKeyline)
    }

    private fun setLayoutProperties(orientation: Int, reverseLayout: Boolean) {
        if (orientation == RecyclerView.VERTICAL) {
            alignmentCalculator.updateLayoutInfo(
                verticalLayoutManager.get(),
                true,
                reverseLayout
            )
        } else {
            alignmentCalculator.updateLayoutInfo(
                horizontalLayoutManager.get(),
                false,
                reverseLayout
            )
        }
    }

}
