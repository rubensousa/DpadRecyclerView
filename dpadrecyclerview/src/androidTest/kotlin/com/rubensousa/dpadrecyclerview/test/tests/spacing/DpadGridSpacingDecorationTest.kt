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

package com.rubensousa.dpadrecyclerview.test.tests.spacing

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Test

class DpadGridSpacingDecorationTest : DpadRecyclerViewTest() {

    private val spanCount = 4

    private val verticalLayoutConfiguration = TestLayoutConfiguration(
        spans = spanCount,
        orientation = RecyclerView.VERTICAL,
        parentAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN_MAX,
        ),
        childAlignment = ChildAlignment(offset = 0)
    )

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return verticalLayoutConfiguration
    }

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(
            itemLayoutId = R.layout.dpadrecyclerview_test_item_grid
        )
    }

    @Test
    fun testVerticalEvenDecoration() {
        val itemSpacing = 32
        val decoration = DpadGridSpacingDecoration.create(
            itemSpacing = itemSpacing
        )
        launchFragmentWithVerticalDecoration(decoration)

        // First row
        repeat(spanCount) { spanIndex ->
            assertChildDecorations(
                childIndex = spanIndex,
                insets = ViewBounds(
                    left = getStartDecoration(itemSpacing, spanIndex),
                    top = itemSpacing,
                    right = getEndDecoration(itemSpacing, spanIndex),
                    bottom = itemSpacing
                )
            )
        }

        // Middle row
        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount + spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = getStartDecoration(itemSpacing, spanIndex),
                    top = 0,
                    right = getEndDecoration(itemSpacing, spanIndex),
                    bottom = itemSpacing
                )
            )
        }

        // Last row
        selectLastPosition()

        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount - 1 - spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = getStartDecoration(itemSpacing, spanIndex),
                    top = 0,
                    right = getEndDecoration(itemSpacing, spanIndex),
                    bottom = itemSpacing
                ),
                fromStart = false
            )
        }

    }

    @Test
    fun testVerticalDecoration() {
        val verticalItemSpacing = 48
        val verticalEdgeSpacing = 64
        val horizontalItemSpacing = 32
        val decoration = DpadGridSpacingDecoration.create(
            itemSpacing = horizontalItemSpacing,
            perpendicularItemSpacing = verticalItemSpacing,
            edgeSpacing = verticalEdgeSpacing
        )
        launchFragmentWithVerticalDecoration(decoration)

        // First row
        repeat(spanCount) { spanIndex ->
            assertChildDecorations(
                childIndex = spanIndex,
                insets = ViewBounds(
                    left = getStartDecoration(horizontalItemSpacing, spanIndex),
                    top = verticalEdgeSpacing,
                    right = getEndDecoration(horizontalItemSpacing, spanIndex),
                    bottom = verticalItemSpacing
                )
            )
        }

        // Middle row
        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount + spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = getStartDecoration(horizontalItemSpacing, spanIndex),
                    top = 0,
                    right = getEndDecoration(horizontalItemSpacing, spanIndex),
                    bottom = verticalItemSpacing
                )
            )
        }

        // Last row
        selectLastPosition()

        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount - 1 - spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = getStartDecoration(horizontalItemSpacing, spanIndex),
                    top = 0,
                    right = getEndDecoration(horizontalItemSpacing, spanIndex),
                    bottom = verticalEdgeSpacing
                ),
                fromStart = false
            )
        }

    }

    @Test
    fun testReverseVerticalDecoration() {
        val verticalItemSpacing = 48
        val verticalEdgeSpacing = 64
        val horizontalItemSpacing = 32
        val decoration = DpadGridSpacingDecoration.create(
            itemSpacing = horizontalItemSpacing,
            perpendicularItemSpacing = verticalItemSpacing,
            edgeSpacing = verticalEdgeSpacing
        )
        launchFragmentWithVerticalDecoration(decoration, reverseLayout = true)

        // First row
        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount - 1 - spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = getStartDecoration(horizontalItemSpacing, spanIndex),
                    top = verticalItemSpacing,
                    right = getEndDecoration(horizontalItemSpacing, spanIndex),
                    bottom = verticalEdgeSpacing
                ),
                fromStart = false
            )
        }

        // Middle row
        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount * 2 - 1 - spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = getStartDecoration(horizontalItemSpacing, spanIndex),
                    top = verticalItemSpacing,
                    right = getEndDecoration(horizontalItemSpacing, spanIndex),
                    bottom = 0
                ),
                fromStart = false
            )
        }

        // Last row
        selectLastPosition()

        repeat(spanCount) { spanIndex ->
            assertChildDecorations(
                childIndex = spanIndex,
                insets = ViewBounds(
                    left = getStartDecoration(horizontalItemSpacing, spanIndex),
                    top = verticalEdgeSpacing,
                    right = getEndDecoration(horizontalItemSpacing, spanIndex),
                    bottom = 0
                )
            )
        }

    }

    @Test
    fun testVerticalMultipleSpanSizeDecoration() {
        val verticalItemSpacing = 48
        val verticalEdgeSpacing = 64
        val horizontalItemSpacing = 32
        val decoration = DpadGridSpacingDecoration.create(
            itemSpacing = horizontalItemSpacing,
            perpendicularItemSpacing = verticalItemSpacing,
            edgeSpacing = verticalEdgeSpacing
        )
        launchFragmentWithVerticalDecoration(decoration,
            spanSizeLookup = object : DpadSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (position.rem(spanCount + 1) == 0 || position == 0) {
                        spanCount
                    } else {
                        1
                    }
                }
            })

        // First row
        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalItemSpacing,
                top = verticalEdgeSpacing,
                right = horizontalItemSpacing,
                bottom = verticalItemSpacing
            )
        )

        // Middle row
        repeat(spanCount) { spanIndex ->
            val childIndex = spanIndex + 1
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = getStartDecoration(horizontalItemSpacing, spanIndex),
                    top = 0,
                    right = getEndDecoration(horizontalItemSpacing, spanIndex),
                    bottom = verticalItemSpacing
                )
            )
        }

    }

    @Test
    fun testHorizontalDecoration() {
        val horizontalItemSpacing = 32
        val horizontalEdgeSpacing = 64
        val verticalItemSpacing = 48
        val decoration = DpadGridSpacingDecoration.create(
            itemSpacing = verticalItemSpacing,
            perpendicularItemSpacing = horizontalItemSpacing,
            edgeSpacing = horizontalEdgeSpacing,
        )
        launchFragmentWithHorizontalDecoration(decoration)

        // First row
        repeat(spanCount) { spanIndex ->
            assertChildDecorations(
                childIndex = spanIndex,
                insets = ViewBounds(
                    left = horizontalEdgeSpacing,
                    top = getStartDecoration(verticalItemSpacing, spanIndex),
                    right = horizontalItemSpacing,
                    bottom = getEndDecoration(verticalItemSpacing, spanIndex)
                )
            )
        }

        // Middle row
        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount + spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = 0,
                    top = getStartDecoration(verticalItemSpacing, spanIndex),
                    right = horizontalItemSpacing,
                    bottom = getEndDecoration(verticalItemSpacing, spanIndex)
                )
            )
        }

        // Last row
        selectLastPosition()

        repeat(spanCount) { spanIndex ->
            val childIndex = spanCount - 1 - spanIndex
            assertChildDecorations(
                childIndex = childIndex,
                insets = ViewBounds(
                    left = 0,
                    top = getStartDecoration(verticalItemSpacing, spanIndex),
                    right = horizontalEdgeSpacing,
                    bottom = getEndDecoration(verticalItemSpacing, spanIndex)
                ),
                fromStart = false
            )
        }

    }

    @Test
    fun testItemSpacingSetter() {
        // given
        val spacing = 50
        launchFragment()

        // when
        onRecyclerView("Set spacing") { recyclerView ->
            recyclerView.setItemSpacing(spacing)

            // then
            assertThat(recyclerView.getSpacingDecoration()).isEqualTo(
                DpadGridSpacingDecoration(
                    itemSpacing = spacing,
                    minEdgeSpacing = 0,
                    maxEdgeSpacing = 0,
                    perpendicularItemSpacing = spacing
                )
            )
        }
    }

    @Test
    fun testItemEdgeSpacingSetter() {
        // given
        val edgeSpacing = 50
        launchFragment()

        // when
        onRecyclerView("Set spacing") { recyclerView ->
            recyclerView.setItemEdgeSpacing(edgeSpacing)

            // then
            assertThat(recyclerView.getSpacingDecoration()).isEqualTo(
                DpadGridSpacingDecoration(
                    itemSpacing = 0,
                    minEdgeSpacing = edgeSpacing,
                    maxEdgeSpacing = edgeSpacing,
                    perpendicularItemSpacing = 0
                )
            )
        }
    }

    @Test
    fun testItemMinEdgeSpacingSetter() {
        // given
        val edgeSpacing = 50
        launchFragment()

        // when
        onRecyclerView("Set spacing") { recyclerView ->
            recyclerView.setItemMinEdgeSpacing(edgeSpacing)

            // then
            assertThat(recyclerView.getSpacingDecoration()).isEqualTo(
                DpadGridSpacingDecoration(
                    itemSpacing = 0,
                    minEdgeSpacing = edgeSpacing,
                    maxEdgeSpacing = 0,
                    perpendicularItemSpacing = 0
                )
            )
        }
    }

    @Test
    fun testItemMaxEdgeSpacingSetter() {
        // given
        val edgeSpacing = 50
        launchFragment()

        // when
        onRecyclerView("Set spacing") { recyclerView ->
            recyclerView.setItemMaxEdgeSpacing(edgeSpacing)

            // then
            assertThat(recyclerView.getSpacingDecoration()).isEqualTo(
                DpadGridSpacingDecoration(
                    itemSpacing = 0,
                    minEdgeSpacing = 0,
                    maxEdgeSpacing = edgeSpacing,
                    perpendicularItemSpacing = 0
                )
            )
        }
    }

    @Test
    fun testItemSpacingSettersCombineResults() {
        // given
        val itemSpacing = 24
        val minEdgeSpacing = 48
        val maxEdgeSpacing = 64
        launchFragment()

        // when
        onRecyclerView("Set spacing") { recyclerView ->
            recyclerView.setItemMinEdgeSpacing(minEdgeSpacing)
            recyclerView.setItemMaxEdgeSpacing(maxEdgeSpacing)
            recyclerView.setItemSpacing(itemSpacing)

            // then
            assertThat(recyclerView.getSpacingDecoration()).isEqualTo(
                DpadGridSpacingDecoration(
                    itemSpacing = itemSpacing,
                    minEdgeSpacing = minEdgeSpacing,
                    maxEdgeSpacing = maxEdgeSpacing,
                    perpendicularItemSpacing = itemSpacing
                )
            )
        }
    }

    private fun getStartDecoration(spacing: Int, spanIndex: Int): Int {
        return (spacing * (spanCount - spanIndex) / spanCount.toFloat()).toInt()
    }

    private fun getEndDecoration(spacing: Int, spanIndex: Int, spanSize: Int = 1): Int {
        return (spacing * (spanIndex + spanSize) / spanCount.toFloat()).toInt()
    }

    private fun launchFragmentWithVerticalDecoration(
        decoration: DpadGridSpacingDecoration,
        reverseLayout: Boolean = false,
        spanSizeLookup: DpadSpanSizeLookup? = null
    ) {
        launchFragment(verticalLayoutConfiguration.copy(reverseLayout = reverseLayout))
        onRecyclerView("Set linear space decoration") { recyclerView ->
            recyclerView.addItemDecoration(decoration)
            if (spanSizeLookup != null) {
                recyclerView.setSpanSizeLookup(spanSizeLookup)
            }
        }
        Espresso.onIdle()
    }

    private fun launchFragmentWithHorizontalDecoration(
        decoration: DpadGridSpacingDecoration,
        reverseLayout: Boolean = false
    ) {
        launchFragment(
            verticalLayoutConfiguration.copy(
                orientation = RecyclerView.HORIZONTAL,
                reverseLayout = reverseLayout
            ),
            getDefaultAdapterConfiguration()
                .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_grid_horizontal)
        )
        onRecyclerView("Set linear space decoration") { recyclerView ->
            recyclerView.addItemDecoration(decoration)
        }
        Espresso.onIdle()
    }


}
