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

import android.view.KeyEvent
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadDragHelper
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration
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

class DragHelperLinearTest {

    @get:Rule
    val report = DefaultInstrumentedReportRule()

    private lateinit var fragmentScenario: FragmentScenario<RecyclerViewFragment>
    private lateinit var dragHelper: DpadDragHelper<Int>
    private lateinit var testAdapter: TestAdapter
    private val numberOfItems = 10
    private val dragStarted = mutableListOf<RecyclerView.ViewHolder>()
    private val dragStopRequests = mutableListOf<DragStopRequest>()

    @Before
    fun setup() {
        fragmentScenario = launchFragment()
        onRecyclerView("Setup RecyclerView ") { recyclerView ->
            testAdapter = TestAdapter(
                adapterConfiguration = TestAdapterConfiguration(
                    itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal,
                    numberOfItems = numberOfItems
                ),
                onViewHolderSelected = {

                },
                onViewHolderDeselected = {

                }
            )
            recyclerView.apply {
                adapter = testAdapter
                setOrientation(RecyclerView.HORIZONTAL)
                addItemDecoration(
                    DpadLinearSpacingDecoration.create(
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

    @Test
    fun testDetachRecyclerViewStopsDragging() {
        // given
        startDragging(position = 0)

        // when
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            dragHelper.detachFromRecyclerView()
        }

        // then
        assertThat(dragStopRequests).hasSize(1)
        assertThat(dragStopRequests.first().fromUser).isFalse()
    }

    @Test
    fun testDragStartCallback() {
        // given
        val position = 0

        // when
        startDragging(position)

        // then
        val viewHolder = dragStarted.first()
        assertThat(viewHolder.absoluteAdapterPosition).isEqualTo(position)
    }

    @Test
    fun testDragStartsInAnyPosition() {
        // given
        val position = numberOfItems - 1

        // when
        startDragging(position)

        // then
        assertFocusAndSelection(position)
        val viewHolder = dragStarted.first()
        assertThat(viewHolder.absoluteAdapterPosition).isEqualTo(position)
    }

    @Test
    fun testDragStopsOnCertainKeyEvents() {
        // given
        val cancelKeyCodes = setOf(
            KeyEvent.KEYCODE_ENTER,
            KeyEvent.KEYCODE_DPAD_CENTER,
        )

        // when
        cancelKeyCodes.forEach { keyCode ->
            startDragging(position = 0)
            KeyEvents.pressKey(keyCode)
            Espresso.onIdle()
        }

        // then
        assertThat(dragStopRequests).hasSize(cancelKeyCodes.size)
        dragStopRequests.forEach {
            assertThat(it.fromUser).isTrue()
        }
    }

    @Test
    fun testDragStopRestoresFocusScrolling() {
        // given
        startDragging(position = 0)

        // when
        stopDragging()
        KeyEvents.pressRight()
        waitForIdleScrollState()

        // then
        assertFocusAndSelection(position = 1)
    }

    @Test
    fun testIsDraggingIsTrueAfterStartDragging() {
        // when
        startDragging(position = 0)

        // then
        assertThat(dragHelper.isDragging).isTrue()
    }

    @Test
    fun testIsDraggingIsFalseAfterStop() {
        // given
        startDragging(position = 0)

        // when
        stopDragging()

        // then
        assertThat(dragHelper.isDragging).isFalse()
    }

    @Test
    fun testDragMoveForward() {
        // given
        startDragging(position = 0)

        // when
        KeyEvents.pressRight()

        // then
        assertFocusAndSelection(position = 1)
        testAdapter.assertContents { index ->
            when (index) {
                0 -> 1
                1 -> 0
                else -> index
            }
        }
    }

    @Test
    fun testDragMoveBackward() {
        // given
        startDragging(position = 1)

        // when
        KeyEvents.pressLeft()

        // then
        assertFocusAndSelection(position = 0)
        testAdapter.assertContents { index ->
            when (index) {
                0 -> 1
                1 -> 0
                else -> index
            }
        }
    }

    @Test
    fun testDragStartEdgeDoesNothing() {
        // given
        startDragging(position = 0)

        // when
        KeyEvents.pressLeft()

        // then
        assertFocusAndSelection(position = 0)
        testAdapter.assertContents { index -> index }
    }

    @Test
    fun testDragEndEdgeDoesNothing() {
        // given
        startDragging(position = numberOfItems - 1)

        // when
        KeyEvents.pressRight()

        // then
        assertFocusAndSelection(numberOfItems - 1)
        testAdapter.assertContents { index -> index }
    }

    @Test
    fun testDragFromStartToEnd() {
        // given
        startDragging(position = 0)

        // when
        KeyEvents.pressRight(times = numberOfItems)
        waitForIdleScrollState()

        // then
        assertFocusAndSelection(position = numberOfItems - 1)
        testAdapter.assertContents { index ->
            when (index) {
                numberOfItems - 1 -> 0
                else -> index + 1
            }
        }
    }

    @Test
    fun testDragFromEndToStart() {
        // given
        startDragging(position = numberOfItems - 1)

        // when
        KeyEvents.pressLeft(times = numberOfItems)
        waitForIdleScrollState()

        // then
        assertFocusAndSelection(position = 0)
        testAdapter.assertContents { index ->
            when (index) {
                0 -> numberOfItems - 1
                else -> index - 1
            }
        }
    }

    private fun startDragging(position: Int) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            dragHelper.startDrag(position)
        }
    }

    private fun stopDragging() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            dragHelper.stopDrag()
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
