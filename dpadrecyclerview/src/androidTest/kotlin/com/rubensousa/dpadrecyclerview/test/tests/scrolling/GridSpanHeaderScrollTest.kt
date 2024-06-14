/*
 * Copyright 2024 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.test.tests.scrolling

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.test.RecyclerViewFragment
import com.rubensousa.dpadrecyclerview.test.helpers.TestAdapter
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertSelectedPosition
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.helpers.waitForLayout
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.math.abs
import com.rubensousa.dpadrecyclerview.testing.R as RTesting

private val headerViewType = 0
private val normalViewType = 1

class GridSpanHeaderScrollTest {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    private lateinit var fragmentScenario: FragmentScenario<RecyclerViewFragment>

    private val defaultGrid = listOf(
        -1,
        1, 2, 3, 4,
        -2,
        3, 4, 5, 6,
        -3,
        7, 8, 9, 10
    )

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testFocusIsAtFirstFocusablePosition() {
        // given
        setContent(items = defaultGrid, spanCount = 4)

        // when
        waitForLayout()

        // then
        assertFocusPosition(position = 1)
        assertSelectedPosition(position = 1)
    }

    @Test
    fun testFocusUpWhenAboveItemDoesNotExistGoesToStartOfSpan() {
        // given
        setContent(
            items = listOf(
                -1,
                1, 2, 3,
                -2,
                4, 5, 6, 7
            ), spanCount = 4
        )
        KeyEvents.pressDown(times = 1)
        KeyEvents.pressRight(times = 3)
        waitForIdleScrollState()

        assertFocusAndSelection(8)

        // when
        KeyEvents.pressUp()

        // then
        assertFocusAndSelection(position = 1)
    }

    @Test
    fun testLastRowReceivesFocus() {
        // given
        val headers = 3
        val spans = 5
        val items = buildList {
            repeat(headers) { header ->
                add(-(header + 1))
                repeat(spans) {
                    add(size)
                }
            }
        }
        setContent(items, spans)

        // when
        KeyEvents.pressDown(times = headers, delay = 500)
        waitForIdleScrollState()

        // then
        assertFocusPosition(position = items.size - spans)
    }

    @Test
    fun testFastScrollWithIncompleteRows() {
        // given
        val headers = 10
        val spans = 5
        val items = buildList {
            repeat(headers) { header ->
                add(-(header + 1))
                repeat(spans - 1) {
                    add(size)
                }
            }
        }
        setContent(items, spans)

        // when
        KeyEvents.pressDown(times = headers)
        waitForIdleScrollState()
        assertFocusAndSelection(items.size - spans + 1)
        KeyEvents.pressUp(times = headers)
        waitForIdleScrollState(

        )

        // then
        assertFocusAndSelection(1)
    }

    @Test
    fun testConsecutiveHeadersLeadToFocusSearch() {
        // given
        val headers = 10
        val spanCount = 4
        val items = buildList {
            add(-1)
            addAll(listOf(1, 2, 3, 4))
            repeat(headers - 1) {
                add(-size)
            }
            addAll(listOf(5, 6, 7, 8))
        }
        setContent(
            items = items,
            spanCount = spanCount
        )

        // when
        KeyEvents.pressDown(times = headers)
        waitForIdleScrollState()
        assertFocusAndSelection(items.size - spanCount)

        // then
        KeyEvents.pressUp(times = headers)
        waitForIdleScrollState()
        assertFocusAndSelection(1)
    }

    @Test
    fun testFocusStaysInTheSameSpan() {
        // given
        val spanCount = 4
        setContent(defaultGrid, spanCount)
        val startPosition = 1

        repeat(spanCount) { currentSpan ->
            assertFocusPosition(position = startPosition + currentSpan)
            KeyEvents.pressDown()
            waitForIdleScrollState()
            assertFocusAndSelection(position = startPosition + currentSpan + spanCount + 1)
            KeyEvents.pressRight()
            KeyEvents.pressUp()
            waitForIdleScrollState()
        }

    }

    @Test
    fun testSearchFocusOutOfBoundsDoesNotCrash() {
        // given
        val spanCount = 4
        setContent(defaultGrid, spanCount)

        // when
        KeyEvents.pressDown(times = defaultGrid.size)
        waitForIdleScrollState()

        // then
        assertFocusAndSelection(defaultGrid.size - spanCount)
    }

    private fun setContent(items: List<Int>, spanCount: Int) {
        onRecyclerView("Set content") { recyclerView ->
            recyclerView.setSpanCount(spanCount)
            recyclerView.addItemDecoration(
                DpadGridSpacingDecoration.create(
                    itemSpacing = recyclerView.resources.getDimensionPixelSize(
                        R.dimen.dpadrecyclerview_grid_spacing
                    )
                )
            )
            val gridAdapter = Adapter()
            gridAdapter.submitList(items.toMutableList())
            recyclerView.setSpanSizeLookup(object : DpadSpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemViewType = gridAdapter.getItemViewType(position)
                    return when (itemViewType) {
                        normalViewType -> 1
                        else -> spanCount
                    }
                }
            })
            recyclerView.adapter = gridAdapter
        }
    }

    private class Adapter : TestAdapter<SpanViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SpanViewHolder {
            return when (viewType) {
                normalViewType -> GridViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.dpadrecyclerview_item_grid, parent, false)
                )

                else -> HeaderViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.dpadrecyclerview_grid_header, parent, false)
                )
            }
        }

        override fun onBindViewHolder(holder: SpanViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun getItemViewType(position: Int): Int {
            val item = getItem(position)
            return if (item > 0) {
                normalViewType
            } else {
                headerViewType
            }
        }

    }

    private abstract class SpanViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: Int)
    }

    private class GridViewHolder(view: View) : SpanViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.textView)
        override fun bind(item: Int) {
            textView.text = item.toString()
        }
    }

    private class HeaderViewHolder(view: View) : SpanViewHolder(view) {
        private val textView = view.findViewById<TextView>(R.id.textView)
        override fun bind(item: Int) {
            textView.text = "Header ${abs(item)}"
        }
    }

    private fun launchFragment(): FragmentScenario<RecyclerViewFragment> {
        return launchFragmentInContainer<RecyclerViewFragment>(
            themeResId = RTesting.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
            waitForCondition("Waiting for layout pass") { recyclerView ->
                !recyclerView.isLayoutRequested
            }
        }
    }

}
