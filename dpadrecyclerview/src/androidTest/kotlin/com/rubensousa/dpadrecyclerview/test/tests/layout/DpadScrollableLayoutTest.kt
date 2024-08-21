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

package com.rubensousa.dpadrecyclerview.test.tests.layout

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadScrollableLayout
import com.rubensousa.dpadrecyclerview.spacing.DpadGridSpacingDecoration
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.test.TestAdapter
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit
import com.rubensousa.dpadrecyclerview.testing.R as testingR

class DpadScrollableLayoutTest {

    private lateinit var fragmentScenario: FragmentScenario<DpadScrollableFragment>

    @Before
    fun setup() {
        fragmentScenario = launchFragment()
    }

    @Test
    fun testLayoutIsOnlyTriggeredOnce() {
        var layoutCompleted = 0

        // when
        fragmentScenario.onFragment { fragment ->
            layoutCompleted = fragment.layoutsCompleted
        }

        // then
        assertThat(layoutCompleted).isEqualTo(1)
    }

    @Test
    fun testInitialLayoutPositions() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()

        // when
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)

        // then
        assertThat(header1Bounds).isEqualTo(
            Rect(0, 0, screenWidth, headerHeight)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, headerHeight, screenWidth, headerHeight * 2)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, headerHeight * 2, screenWidth, screenHeight + headerHeight * 2)
        )
    }

    @Test
    fun testHidingHeaderWithoutAnimation() {
        // given
        val headerHeight = getHeaderHeight()
        val screenHeight = getHeight()
        val screenWidth = getWidth()

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.hideHeader(smooth = false)
        }

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)

        assertThat(header1Bounds).isEqualTo(
            Rect(0, -headerHeight * 2, screenWidth, -headerHeight)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, -headerHeight, screenWidth, 0)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, 0, screenWidth, screenHeight)
        )
    }

    @Test
    fun testHidingHeaderWithAnimation() {
        // given
        val headerHeight = getHeaderHeight()
        val screenHeight = getHeight()
        val screenWidth = getWidth()

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.hideHeader(smooth = true)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight * 2, bottom = -headerHeight)

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, -headerHeight * 2, screenWidth, -headerHeight)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, -headerHeight, screenWidth, 0)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, 0, screenWidth, screenHeight)
        )
    }

    @Test
    fun testShowingHeaderWithAnimation() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.hideHeader(smooth = true)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight * 2, bottom = -headerHeight)

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.showHeader(smooth = true)
        }
        waitViewAtCoordinates(R.id.header1, top = 0, bottom = headerHeight)

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, 0, screenWidth, headerHeight)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, headerHeight, screenWidth, headerHeight * 2)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, headerHeight * 2, screenWidth, screenHeight + headerHeight * 2)
        )
    }

    @Test
    fun testShowingHeaderWithoutAnimation() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.hideHeader(smooth = true)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight * 2, bottom = -headerHeight)

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.showHeader(smooth = false)
        }
        waitViewAtCoordinates(R.id.header1, top = 0, bottom = headerHeight)

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, 0, screenWidth, headerHeight)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, headerHeight, screenWidth, headerHeight * 2)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, headerHeight * 2, screenWidth, screenHeight + headerHeight * 2)
        )
    }

    @Test
    fun testOffsetAnimation() {
        // given
        val headerOffset = getHeaderHeight() / 2
        val headerHeight = getHeaderHeight()
        val screenHeight = getHeight()
        val screenWidth = getWidth()

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.scrollHeaderTo(-headerOffset)
        }

        waitViewAtCoordinates(
            R.id.header1,
            top = -headerOffset,
            bottom = -headerOffset + headerHeight
        )

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, -headerOffset, screenWidth, -headerOffset + headerHeight)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, -headerOffset + headerHeight, screenWidth, -headerOffset + headerHeight * 2)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(
                0,
                -headerOffset + headerHeight * 2,
                screenWidth,
                -headerOffset + headerHeight * 2 + screenHeight
            )
        )
    }

    @Test
    fun testLayoutStillHidesHeaderWhenItGetsSmallerWhileNotVisible() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.hideHeader(smooth = true)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight * 2, bottom = -headerHeight)

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.removeViewAt(0)
        }

        // then
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header2Bounds).isEqualTo(
            Rect(0, -headerHeight, screenWidth, 0)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, 0, screenWidth, screenHeight)
        )
    }

    @Test
    fun testLayoutAlignsHeaderToTopWhenItGetsSmallerWhileVisible() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.scrollHeaderTo(topOffset = -headerHeight / 2)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight / 2, bottom = headerHeight / 2)

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.removeViewAt(0)
        }

        // then
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header2Bounds).isEqualTo(
            Rect(0, 0, screenWidth, headerHeight)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, headerHeight, screenWidth, screenHeight + headerHeight)
        )
    }

    @Test
    fun testLayoutStillHidesHeaderWhenItGetsBiggerWhileNotVisible() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.hideHeader(smooth = true)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight * 2, bottom = -headerHeight)

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.header1?.updateLayoutParams<DpadScrollableLayout.LayoutParams> {
                height *= 2
            }
        }

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, -headerHeight * 3, screenWidth, -headerHeight)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, -headerHeight, screenWidth, 0)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, 0, screenWidth, screenHeight)
        )
    }

    @Test
    fun testOffsetIsAdjustedWhenLayoutGetsBiggerWhileHeaderIsVisible() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.scrollHeaderTo(topOffset = -headerHeight / 2)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight / 2, bottom = headerHeight / 2)

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.header1?.updateLayoutParams<DpadScrollableLayout.LayoutParams> {
                height *= 2
            }
        }

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, 0, screenWidth, headerHeight * 2)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, headerHeight * 2, screenWidth, headerHeight * 3)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(0, headerHeight * 3, screenWidth, screenHeight + headerHeight * 3)
        )
    }

    @Test
    fun testHeaderStaysPartiallyVisibleAfterLayoutRequest() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.scrollHeaderTo(topOffset = -headerHeight / 2)
        }
        waitViewAtCoordinates(R.id.header1, top = -headerHeight / 2, bottom = headerHeight / 2)

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.requestLayout()
        }

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        assertThat(header1Bounds).isEqualTo(
            Rect(0,  -headerHeight / 2, screenWidth, headerHeight / 2)
        )
    }

    @Test
    fun testRequestingLayoutDuringOffsetChangesDoesNotBreakLayout() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        val screenHeight = getHeight()
        val scrollDuration = 5000
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.setScrollDurationConfig(
                object : DpadScrollableLayout.ScrollDurationConfig {
                    override fun calculateScrollDuration(layoutHeight: Int, dy: Int): Int {
                        return scrollDuration
                    }
                })
        }

        // when
        fragmentScenario.onFragment { fragment ->
            fragment.scrollableLayout?.scrollHeaderTo(topOffset = -headerHeight / 2)
            fragment.scrollableLayout?.requestLayout()
        }
        waitViewAtCoordinates(
            R.id.header1,
            top = -headerHeight / 2,
            bottom = headerHeight / 2,
            timeout = scrollDuration.toLong()
        )

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        val header2Bounds = getViewBounds(R.id.header2)
        val recyclerViewBounds = getViewBounds(R.id.recyclerView)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, -headerHeight / 2, screenWidth, headerHeight / 2)
        )
        assertThat(header2Bounds).isEqualTo(
            Rect(0, headerHeight / 2, screenWidth, headerHeight / 2 + headerHeight)
        )
        assertThat(recyclerViewBounds).isEqualTo(
            Rect(
                0,
                headerHeight / 2 + headerHeight,
                screenWidth,
                screenHeight + headerHeight / 2 + headerHeight
            )
        )
    }

    @Test
    fun testHeaderIsVisibleByDefault() {
        // given
        val headerHeight = getHeaderHeight()
        val screenWidth = getWidth()
        var actualVisibility = false

        // when
        fragmentScenario.onFragment { fragment ->
            actualVisibility = fragment.scrollableLayout?.isHeaderVisible ?: false
        }

        // then
        val header1Bounds = getViewBounds(R.id.header1)
        assertThat(header1Bounds).isEqualTo(
            Rect(0, 0, screenWidth, headerHeight)
        )
        assertThat(actualVisibility).isTrue()
    }

    private fun waitViewAtCoordinates(viewId: Int, top: Int, bottom: Int, timeout: Long = 4L) {
        Espresso.onView(withId(viewId))
            .perform(
                DpadViewActions.waitForCondition<View>("Wait for view at: $top and $bottom",
                    timeout = timeout,
                    timeoutUnit = TimeUnit.SECONDS,
                    condition = { view ->
                        val intArray = IntArray(2)
                        view.getLocationInWindow(intArray)
                        val y = intArray[1]
                        y == top && y + view.height == bottom
                    }
                )
            )
    }

    private fun getHeight(): Int {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext.resources.displayMetrics.heightPixels
    }

    private fun getWidth(): Int {
        return InstrumentationRegistry.getInstrumentation()
            .targetContext.resources.displayMetrics.widthPixels
    }

    private fun getHeaderHeight(): Int {
        return getSizeInPixels(R.dimen.dpadrecyclerview_header_size)
    }

    private fun getSizeInPixels(resourceId: Int): Int {
        return InstrumentationRegistry.getInstrumentation().targetContext.resources.getDimensionPixelSize(
            resourceId
        )
    }

    private fun getViewBounds(viewId: Int): Rect {
        val rect = Rect()
        Espresso.onView(withId(viewId)).perform(DpadViewActions.getViewBounds(rect))
        return rect
    }

    private fun launchFragment(): FragmentScenario<DpadScrollableFragment> {
        return launchFragmentInContainer<DpadScrollableFragment>(
            themeResId = com.rubensousa.dpadrecyclerview.testing.R.style.DpadRecyclerViewTestTheme
        ).also {
            fragmentScenario = it
            waitForCondition("Waiting for layout pass") { recyclerView ->
                !recyclerView.isLayoutRequested
            }
        }
    }

    class DpadScrollableFragment : Fragment(R.layout.dpadrecyclerview_scrollable_container) {

        var recyclerView: DpadRecyclerView? = null
        var scrollableLayout: DpadScrollableLayout? = null
        var header1: View? = null
        var header2: View? = null
        var layoutsCompleted = 0

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            recyclerView = view.findViewById(R.id.recyclerView)
            scrollableLayout = view.findViewById(R.id.scrollableLayout)
            header1 = view.findViewById(R.id.header1)
            header2 = view.findViewById(R.id.header2)
            recyclerView?.apply {
                adapter = TestAdapter(
                    adapterConfiguration = TestAdapterConfiguration(
                        itemLayoutId = testingR.layout.dpadrecyclerview_test_item_grid,
                        numberOfItems = 50
                    ),
                    onViewHolderSelected = {

                    },
                    onViewHolderDeselected = {

                    }
                )
                setSpanCount(5)
                addItemDecoration(
                    DpadGridSpacingDecoration.create(
                        itemSpacing = resources.getDimensionPixelSize(
                            R.dimen.dpadrecyclerview_grid_spacing
                        )
                    )
                )
            }
            recyclerView?.addOnLayoutCompletedListener(object :
                DpadRecyclerView.OnLayoutCompletedListener {
                override fun onLayoutCompleted(state: RecyclerView.State) {
                    layoutsCompleted++
                }
            })
        }

        override fun onDestroyView() {
            super.onDestroyView()
            scrollableLayout = null
            recyclerView = null
            header1 = null
            header2 = null
        }

    }

}
