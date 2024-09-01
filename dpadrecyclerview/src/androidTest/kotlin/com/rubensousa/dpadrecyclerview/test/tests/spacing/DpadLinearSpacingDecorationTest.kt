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
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectLastPosition
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Test

class DpadLinearSpacingDecorationTest : DpadRecyclerViewTest() {

    private val verticalLayoutConfiguration = TestLayoutConfiguration(
        spans = 1,
        orientation = RecyclerView.VERTICAL,
        parentAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN_MAX,
        ),
        childAlignment = ChildAlignment(offset = 0)
    )

    private val horizontalAdapterConfiguration = super.getDefaultAdapterConfiguration()
        .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal)

    private val horizontalLayoutConfiguration = verticalLayoutConfiguration.copy(
        orientation = RecyclerView.HORIZONTAL
    )

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return verticalLayoutConfiguration
    }

    @Test
    fun testVerticalEvenDecoration() {
        val verticalSpacing = 48
        val horizontalEdgeSpacing = 0
        val decoration = DpadLinearSpacingDecoration.create(itemSpacing = verticalSpacing)
        launchFragmentWithVerticalDecoration(decoration)

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = verticalSpacing,
                right = horizontalEdgeSpacing,
                bottom = verticalSpacing
            )
        )

        repeat(4) { index ->
            assertChildDecorations(
                childIndex = index + 1,
                insets = ViewBounds(
                    left = horizontalEdgeSpacing,
                    top = 0,
                    right = horizontalEdgeSpacing,
                    bottom = verticalSpacing
                )
            )
        }

        selectLastPosition()

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = 0,
                right = horizontalEdgeSpacing,
                bottom = verticalSpacing
            ),
            fromStart = false
        )
    }

    @Test
    fun testVerticalDecorationWithHorizontalEdgeSpacing() {
        val verticalSpacing = 48
        val horizontalEdgeSpacing = 64
        val decoration = DpadLinearSpacingDecoration.create(
            itemSpacing = verticalSpacing,
            perpendicularEdgeSpacing = horizontalEdgeSpacing
        )
        launchFragmentWithVerticalDecoration(decoration)

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = verticalSpacing,
                right = horizontalEdgeSpacing,
                bottom = verticalSpacing
            )
        )

        repeat(4) { index ->
            assertChildDecorations(
                childIndex = index + 1,
                insets = ViewBounds(
                    left = horizontalEdgeSpacing,
                    top = 0,
                    right = horizontalEdgeSpacing,
                    bottom = verticalSpacing
                )
            )
        }

        selectLastPosition()

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = 0,
                right = horizontalEdgeSpacing,
                bottom = verticalSpacing
            ),
            fromStart = false
        )
    }

    @Test
    fun testVerticalEdgeDecoration() {
        val verticalSpacing = 48
        val verticalEdgeSpacing = verticalSpacing * 2
        val horizontalEdgeSpacing = 24
        val decoration = DpadLinearSpacingDecoration.create(
            itemSpacing = verticalSpacing,
            edgeSpacing = verticalEdgeSpacing,
            perpendicularEdgeSpacing = horizontalEdgeSpacing
        )
        launchFragmentWithVerticalDecoration(decoration)

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = verticalEdgeSpacing,
                right = horizontalEdgeSpacing,
                bottom = verticalSpacing
            )
        )

        repeat(3) { index ->
            assertChildDecorations(
                childIndex = index + 1,
                insets = ViewBounds(
                    left = horizontalEdgeSpacing,
                    top = 0,
                    right = horizontalEdgeSpacing,
                    bottom = verticalSpacing
                )
            )
        }

        selectLastPosition()

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = 0,
                right = horizontalEdgeSpacing,
                bottom = verticalEdgeSpacing
            ),
            fromStart = false
        )
    }

    @Test
    fun testReverseVerticalDecoration() {
        val verticalSpacing = 48
        val verticalEdgeSpacing = verticalSpacing * 2
        val horizontalEdgeSpacing = 24
        val decoration = DpadLinearSpacingDecoration.create(
            itemSpacing = verticalSpacing,
            edgeSpacing = verticalEdgeSpacing,
            perpendicularEdgeSpacing = horizontalEdgeSpacing
        )
        launchFragmentWithVerticalDecoration(decoration, reverseLayout = true)

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = verticalSpacing,
                right = horizontalEdgeSpacing,
                bottom = verticalEdgeSpacing
            ),
            fromStart = false
        )

        repeat(3) { index ->
            assertChildDecorations(
                childIndex = index + 1,
                insets = ViewBounds(
                    left = horizontalEdgeSpacing,
                    top = verticalSpacing,
                    right = horizontalEdgeSpacing,
                    bottom = 0
                ),
                fromStart = false
            )
        }

        selectLastPosition()

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = verticalEdgeSpacing,
                right = horizontalEdgeSpacing,
                bottom = 0
            ),
            fromStart = true
        )
    }

    @Test
    fun testHorizontalEvenDecoration() {
        val horizontalSpacing = 48
        val verticalEdgeSpacing = 64
        val decoration = DpadLinearSpacingDecoration.create(
            itemSpacing = horizontalSpacing,
            perpendicularEdgeSpacing = verticalEdgeSpacing
        )
        launchFragmentWithHorizontalDecoration(decoration)

        assertChildDecorations(
            childIndex = 0, ViewBounds(
                left = horizontalSpacing,
                top = verticalEdgeSpacing,
                right = horizontalSpacing,
                bottom = verticalEdgeSpacing
            )
        )

        repeat(4) { index ->
            assertChildDecorations(
                childIndex = index + 1, ViewBounds(
                    left = 0,
                    top = verticalEdgeSpacing,
                    right = horizontalSpacing,
                    bottom = verticalEdgeSpacing
                )
            )
        }

        selectLastPosition()

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = 0,
                top = verticalEdgeSpacing,
                right = horizontalSpacing,
                bottom = verticalEdgeSpacing
            ),
            fromStart = false
        )

    }

    @Test
    fun testHorizontalEdgeDecoration() {
        val horizontalSpacing = 48
        val horizontalEdgeSpacing = horizontalSpacing * 2
        val verticalEdgeSpacing = 64
        val decoration = DpadLinearSpacingDecoration.create(
            itemSpacing = horizontalSpacing,
            edgeSpacing = horizontalEdgeSpacing,
            perpendicularEdgeSpacing = verticalEdgeSpacing
        )
        launchFragmentWithHorizontalDecoration(decoration)

        assertChildDecorations(
            childIndex = 0, ViewBounds(
                left = horizontalEdgeSpacing,
                top = verticalEdgeSpacing,
                right = horizontalSpacing,
                bottom = verticalEdgeSpacing
            )
        )

        repeat(4) { index ->
            assertChildDecorations(
                childIndex = index + 1, ViewBounds(
                    left = 0,
                    top = verticalEdgeSpacing,
                    right = horizontalSpacing,
                    bottom = verticalEdgeSpacing
                )
            )
        }


        selectLastPosition()

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = 0,
                top = verticalEdgeSpacing,
                right = horizontalEdgeSpacing,
                bottom = verticalEdgeSpacing
            ),
            fromStart = false
        )

    }

    @Test
    fun testReverseHorizontalDecoration() {
        val horizontalSpacing = 48
        val horizontalEdgeSpacing = horizontalSpacing * 2
        val verticalEdgeSpacing = 64
        val decoration = DpadLinearSpacingDecoration.create(
            itemSpacing = horizontalSpacing,
            edgeSpacing = horizontalEdgeSpacing,
            perpendicularEdgeSpacing = verticalEdgeSpacing
        )
        launchFragmentWithHorizontalDecoration(decoration, reverseLayout = true)

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalSpacing,
                top = verticalEdgeSpacing,
                right = horizontalEdgeSpacing,
                bottom = verticalEdgeSpacing
            ),
            fromStart = false
        )

        repeat(4) { index ->
            assertChildDecorations(
                childIndex = index + 1,
                insets = ViewBounds(
                    left = horizontalSpacing,
                    top = verticalEdgeSpacing,
                    right = 0,
                    bottom = verticalEdgeSpacing
                ),
                fromStart = false
            )
        }


        selectLastPosition()

        assertChildDecorations(
            childIndex = 0,
            insets = ViewBounds(
                left = horizontalEdgeSpacing,
                top = verticalEdgeSpacing,
                right = 0,
                bottom = verticalEdgeSpacing
            ),
            fromStart = true
        )

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
                DpadLinearSpacingDecoration(
                    itemSpacing = spacing,
                    minEdgeSpacing = 0,
                    maxEdgeSpacing = 0,
                    perpendicularEdgeSpacing = 0
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
                DpadLinearSpacingDecoration(
                    itemSpacing = 0,
                    minEdgeSpacing = edgeSpacing,
                    maxEdgeSpacing = edgeSpacing,
                    perpendicularEdgeSpacing = 0
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
                DpadLinearSpacingDecoration(
                    itemSpacing = 0,
                    minEdgeSpacing = edgeSpacing,
                    maxEdgeSpacing = 0,
                    perpendicularEdgeSpacing = 0
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
                DpadLinearSpacingDecoration(
                    itemSpacing = 0,
                    minEdgeSpacing = 0,
                    maxEdgeSpacing = edgeSpacing,
                    perpendicularEdgeSpacing = 0
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
            recyclerView.setItemSpacing(itemSpacing)
            recyclerView.setItemMinEdgeSpacing(minEdgeSpacing)
            recyclerView.setItemMaxEdgeSpacing(maxEdgeSpacing)

            // then
            assertThat(recyclerView.getSpacingDecoration()).isEqualTo(
                DpadLinearSpacingDecoration(
                    itemSpacing = itemSpacing,
                    minEdgeSpacing = minEdgeSpacing,
                    maxEdgeSpacing = maxEdgeSpacing,
                    perpendicularEdgeSpacing = 0
                )
            )
        }
    }

    private fun launchFragmentWithVerticalDecoration(
        decoration: DpadLinearSpacingDecoration,
        reverseLayout: Boolean = false,
    ) {
        launchFragment(verticalLayoutConfiguration.copy(reverseLayout = reverseLayout))
        onRecyclerView("Set linear space decoration") { recyclerView ->
            recyclerView.addItemDecoration(decoration)
        }
        Espresso.onIdle()
    }

    private fun launchFragmentWithHorizontalDecoration(
        decoration: DpadLinearSpacingDecoration,
        reverseLayout: Boolean = false,
    ) {
        launchFragment(
            horizontalLayoutConfiguration.copy(reverseLayout = reverseLayout),
            horizontalAdapterConfiguration
        )
        onRecyclerView("Set linear space decoration") { recyclerView ->
            recyclerView.addItemDecoration(decoration)
        }
        Espresso.onIdle()
    }


}
