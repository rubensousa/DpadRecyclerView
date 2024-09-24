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

package com.rubensousa.dpadrecyclerview.test.tests.drag

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.platform.app.InstrumentationRegistry
import com.rubensousa.dpadrecyclerview.DpadDragHelper
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import com.rubensousa.dpadrecyclerview.test.RecyclerViewFragment
import com.rubensousa.dpadrecyclerview.test.TestAdapter
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.testfixtures.DefaultInstrumentedReportRule
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DragHelperGridTest {

    @get:Rule
    val report = DefaultInstrumentedReportRule()

    private lateinit var fragmentScenario: FragmentScenario<RecyclerViewFragment>
    private lateinit var dragHelper: DpadDragHelper<Int>
    private lateinit var testAdapter: TestAdapter
    private val numberOfItems = 50
    private val spanCount = 5
    private val dragStarted = mutableListOf<RecyclerView.ViewHolder>()
    private val dragStopRequests = mutableListOf<DragStopRequest>()

    @Before
    fun setup() = report.before {
        step("Launch fragment") {
            fragmentScenario = launchFragment()
        }
        step("Setup RecyclerView") {
            onRecyclerView("Setup RecyclerView ") { recyclerView ->
                testAdapter = TestAdapter(
                    adapterConfiguration = TestAdapterConfiguration(
                        itemLayoutId = R.layout.dpadrecyclerview_test_item_grid,
                        numberOfItems = numberOfItems
                    ),
                    onViewHolderSelected = {

                    },
                    onViewHolderDeselected = {

                    }
                )
                recyclerView.apply {
                    adapter = testAdapter
                    setSpanCount(spanCount)
                    addItemDecoration(
                        DpadGridSpacingDecoration.create(
                            itemSpacing = resources.getDimensionPixelSize(
                                com.rubensousa.dpadrecyclerview.test.R.dimen.dpadrecyclerview_grid_spacing
                            )
                        )
                    )
                }
                dragHelper = DpadDragHelper(
                    adapter = testAdapter,
                    callback = object : DpadDragHelper.DragCallback {
                        override fun onDragStarted(viewHolder: RecyclerView.ViewHolder) {
                            dragStarted.add(viewHolder)
                        }

                        override fun onDragStopped(fromUser: Boolean) {
                            dragStopRequests.add(DragStopRequest(fromUser))
                        }
                    }
                )
                dragHelper.attachToRecyclerView(recyclerView)
            }
        }
    }

    @Test
    fun testDragStartRowToEndRow() = report {
        val endRowPosition = spanCount - 1
        Given("Start dragging at position 0") {
            startDragging(position = 0)
        }

        When("Scroll $spanCount times to the right") {
            repeat(spanCount) {
                KeyEvents.pressRight()
                waitForIdleScrollState()
            }
        }

        // then
        Then("Focus is in the last column at $endRowPosition") {
            assertFocusAndSelection(position = endRowPosition)
            testAdapter.assertContents { index ->
                when {
                    index < spanCount - 1 -> index + 1
                    index == spanCount - 1 -> 0
                    else -> index
                }
            }
        }
    }

    @Test
    fun testDragEndRowToStartRow() {
        // given
        val endRowPosition = spanCount - 1
        startDragging(position = endRowPosition)

        // when
        repeat(spanCount) {
            KeyEvents.pressLeft()
            waitForIdleScrollState()
        }

        // then
        assertFocusAndSelection(position = 0)
        testAdapter.assertContents { index ->
            when {
                index == 0 -> endRowPosition
                index < spanCount -> index - 1
                else -> index
            }
        }
    }

    @Test
    fun testDragColumnTopToColumnBottom() {
        // given
        val topColumnPosition = spanCount - 1
        val bottomColumnPosition = topColumnPosition + spanCount
        startDragging(position = topColumnPosition)

        // when
        KeyEvents.pressDown()

        // then
        assertFocusAndSelection(position = bottomColumnPosition)
        testAdapter.assertContents { index ->
            when {
                index < topColumnPosition -> index
                index < bottomColumnPosition -> index + 1
                index == bottomColumnPosition -> topColumnPosition
                else -> index
            }
        }
    }

    @Test
    fun testDragColumnBottomToColumnTop() {
        // given
        val topColumnPosition = spanCount - 1
        val bottomColumnPosition = topColumnPosition + spanCount
        startDragging(position = bottomColumnPosition)

        // when
        KeyEvents.pressUp()

        // then
        assertFocusAndSelection(position = topColumnPosition)
        testAdapter.assertContents { index ->
            when {
                index < topColumnPosition -> index
                index == topColumnPosition -> bottomColumnPosition
                index <= bottomColumnPosition -> index - 1
                else -> index
            }
        }
    }

    @Test
    fun testDragStartToEnd() {
        // given
        startDragging(position = 0)

        // when
        repeat(numberOfItems / spanCount) {
            KeyEvents.pressDown()
            waitForIdleScrollState()
        }
        repeat(spanCount) {
            KeyEvents.pressRight()
            waitForIdleScrollState()
        }

        // then
        assertFocusAndSelection(position = numberOfItems - 1)
        testAdapter.assertContents { index ->
            when {
                index < numberOfItems - 1 -> index + 1
                else -> 0
            }
        }
    }

    @Test
    fun testDragEndToStart() {
        // given
        startDragging(position = numberOfItems - 1)

        // when
        repeat(numberOfItems / spanCount) {
            KeyEvents.pressUp()
            waitForIdleScrollState()
        }
        repeat(spanCount) {
            KeyEvents.pressLeft()
            waitForIdleScrollState()
        }

        // then
        assertFocusAndSelection(position = 0)
        testAdapter.assertContents { index ->
            when {
                index == 0 -> numberOfItems - 1
                else -> index - 1
            }
        }
    }

    private fun startDragging(position: Int) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            dragHelper.startDrag(position)
        }
    }

    private fun launchFragment(): FragmentScenario<RecyclerViewFragment> {
        return launchFragmentInContainer<RecyclerViewFragment>(
            themeResId = R.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
            waitForCondition("Waiting for layout pass") { recyclerView ->
                !recyclerView.isLayoutRequested
            }
        }
    }

}
