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
        fraction = 0.5f
    )

    @Test
    fun `keyline for child near start edge is the start edge`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignmentCalculator.updateScrollLimits(
            startEdge = 0,
            endEdge = height,
            startViewAnchor = verticalViewHeight / 2,
            endViewAnchor = verticalViewHeight / 2,
            startAlignment = centerParentAlignment,
            endAlignment = centerParentAlignment
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

        alignmentCalculator.updateScrollLimits(
            startEdge = 0,
            endEdge = height,
            startViewAnchor = 0,
            endViewAnchor = height - verticalViewHeight / 2,
            startAlignment = centerParentAlignment,
            endAlignment = centerParentAlignment
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
            fraction = 0.5f
        )

        assertThat(alignmentCalculator.calculateKeyline(minAlignment)).isEqualTo(
            (height * minAlignment.fraction + minAlignment.offset).toInt()
        )

        minAlignment = minAlignment.copy(offset = 10, fraction = 0.0f)

        assertThat(alignmentCalculator.calculateKeyline(minAlignment)).isEqualTo(minAlignment.offset)
    }

    @Test
    fun `keyline is set correctly for reverse layout`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = true)

        var minAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 10,
            fraction = 0.0f
        )

        assertThat(alignmentCalculator.calculateKeyline(minAlignment)).isEqualTo(height - minAlignment.offset)

        minAlignment = minAlignment.copy(fraction = 0.5f)

        assertThat(alignmentCalculator.calculateKeyline(minAlignment))
            .isEqualTo(
                (height * minAlignment.fraction - minAlignment.offset).toInt()
            )
    }

    @Test
    fun `child is aligned to end edge in regular layout direction`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignmentCalculator.updateScrollLimits(
            startEdge = 0,
            endEdge = height + verticalViewHeight,
            startViewAnchor = verticalViewHeight / 2,
            endViewAnchor = height + verticalViewHeight / 2,
            startAlignment = centerParentAlignment,
            endAlignment = centerParentAlignment
        )
        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = height + verticalViewHeight / 2,
                alignment = centerParentAlignment
            )
        ).isEqualTo(alignmentCalculator.endScrollLimit)

    }

    @Test
    fun `child is aligned to start edge in regular layout direction`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        alignmentCalculator.updateScrollLimits(
            startEdge = height / 2 - verticalViewHeight / 2,
            endEdge = height / 2 + verticalViewHeight / 2,
            startViewAnchor = verticalViewHeight / 2,
            endViewAnchor = verticalViewHeight / 2,
            startAlignment = centerParentAlignment,
            endAlignment = centerParentAlignment
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

        alignmentCalculator.updateScrollLimits(
            startEdge = height / 2 - verticalViewHeight / 2,
            endEdge = height / 2 + verticalViewHeight / 2,
            startViewAnchor = verticalViewHeight / 2,
            endViewAnchor = verticalViewHeight / 2,
            startAlignment = centerParentAlignment,
            endAlignment = centerParentAlignment
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

        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MAX,
            fraction = 1.0f
        )

        alignmentCalculator.updateScrollLimits(
            startEdge = height,
            endEdge = verticalViewHeight,
            startViewAnchor = verticalViewHeight,
            endViewAnchor = verticalViewHeight,
            startAlignment = alignment,
            endAlignment = alignment
        )

        assertThat(alignmentCalculator.startScrollLimit).isEqualTo(
            verticalViewHeight
        )

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = verticalViewHeight,
                alignment = alignment
            )
        ).isEqualTo(alignmentCalculator.startScrollLimit)
    }

    @Test
    fun `child is aligned to start edge if client does not prefer keyline`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 0,
            fraction = 0.5f,
            preferKeylineOverEdge = false
        )
        alignmentCalculator.updateScrollLimits(
            startEdge = verticalCenterKeyline - verticalViewHeight / 2,
            startViewAnchor = verticalCenterKeyline,
            endEdge = verticalCenterKeyline + verticalViewHeight / 2,
            endViewAnchor = verticalCenterKeyline,
            startAlignment = alignment,
            endAlignment = alignment
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
            fraction = 0.5f,
            preferKeylineOverEdge = false
        )

        alignmentCalculator.updateScrollLimits(
            startEdge = -verticalViewHeight / 2,
            endEdge = verticalCenterKeyline + verticalViewHeight,
            startViewAnchor = verticalViewHeight / 2,
            endViewAnchor = verticalCenterKeyline + verticalViewHeight / 2,
            startAlignment = alignment,
            endAlignment = alignment
        )

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = verticalCenterKeyline + verticalViewHeight / 2,
                alignment = alignment
            )
        ).isEqualTo(-(height - (verticalCenterKeyline + verticalViewHeight)))
    }

    @Test
    fun `child is not aligned to start edge if client prefers keyline`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 0,
            fraction = 0.5f,
            preferKeylineOverEdge = true
        )
        alignmentCalculator.updateScrollLimits(
            startEdge = verticalCenterKeyline - verticalViewHeight / 2,
            endEdge = verticalCenterKeyline + verticalViewHeight / 2,
            startViewAnchor = verticalCenterKeyline,
            endViewAnchor = verticalCenterKeyline,
            startAlignment = alignment,
            endAlignment = alignment
        )
        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = height / 2,
                alignment = alignment
            )
        ).isEqualTo(0)
    }

    @Test
    fun `child is not aligned to end edge if client prefers keyline`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MAX,
            offset = 0,
            fraction = 0.5f,
            preferKeylineOverEdge = true
        )
        alignmentCalculator.updateScrollLimits(
            startEdge = 0,
            startViewAnchor = verticalCenterKeyline,
            endEdge = verticalCenterKeyline + verticalViewHeight / 2,
            endViewAnchor = verticalCenterKeyline,
            startAlignment = alignment,
            endAlignment = alignment
        )

        val distanceToKeyline = 0

        assertThat(
            alignmentCalculator.calculateScrollOffset(
                viewAnchor = verticalCenterKeyline,
                alignment = alignment
            )
        ).isEqualTo(distanceToKeyline)
    }

    @Test
    fun `end scroll limit should be limited to distance to end edge when layout is not completely filled`() {
        setLayoutProperties(orientation = RecyclerView.HORIZONTAL, reverseLayout = false)

        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MAX,
            offset = 0,
            fraction = 0f,
            preferKeylineOverEdge = false
        )
        alignmentCalculator.updateScrollLimits(
            startEdge = 0,
            endEdge = horizontalViewWidth * 3,
            startViewAnchor = 0,
            endViewAnchor = horizontalViewWidth * 2,
            startAlignment = alignment,
            endAlignment = alignment
        )

        assertThat(alignmentCalculator.endScrollLimit).isEqualTo(horizontalViewWidth * 3 - width)
    }

    @Test
    fun `start scroll limit should be distance to keyline when layout is incomplete and edge none is set`() {
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.NONE,
            offset = 0,
            fraction = 0.5f,
        )

        val keyline = verticalCenterKeyline

        alignmentCalculator.updateScrollLimits(
            startEdge = keyline - verticalViewHeight,
            startViewAnchor = keyline - verticalViewHeight / 2,
            endEdge = keyline,
            endViewAnchor = keyline - verticalViewHeight / 2,
            startAlignment = alignment,
            endAlignment = alignment
        )

        assertThat(alignmentCalculator.startScrollLimit).isEqualTo(-verticalViewHeight / 2)
    }

    @Test
    fun `layout is considered complete if end is not unknown`() {
        // given
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)

        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 0,
            fraction = 0.5f,
        )

        val keyline = verticalCenterKeyline

        // when
        alignmentCalculator.updateScrollLimits(
            startEdge = 0,
            startViewAnchor = verticalViewHeight / 2,
            endEdge = keyline,
            endViewAnchor = keyline + verticalViewHeight / 2,
            startAlignment = alignment,
            endAlignment = alignment
        )

        // then
        assertThat(alignmentCalculator.isLayoutComplete()).isEqualTo(true)
    }

    @Test
    fun `should align view to keyline if layout is complete for min edge`() {
        // given
        setLayoutProperties(orientation = RecyclerView.VERTICAL, reverseLayout = false)
        val alignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN,
            offset = 0,
            fraction = 0.5f,
        )
        val keyline = verticalCenterKeyline
        alignmentCalculator.updateScrollLimits(
            startEdge = 0,
            startViewAnchor = verticalViewHeight / 2,
            endEdge = keyline,
            endViewAnchor = keyline + verticalViewHeight / 2,
            startAlignment = alignment,
            endAlignment = alignment
        )

        // when
        val scrollOffset = alignmentCalculator.calculateScrollOffset(
            viewAnchor = keyline,
            alignment = alignment
        )

        // then
        assertThat(scrollOffset).isEqualTo(0)
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
