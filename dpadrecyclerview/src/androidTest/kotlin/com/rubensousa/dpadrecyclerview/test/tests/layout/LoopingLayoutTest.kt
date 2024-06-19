package com.rubensousa.dpadrecyclerview.test.tests.layout

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadLoopDirection
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForAnimation
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test
import kotlin.math.ceil
import kotlin.math.min

class LoopingLayoutTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration()
            .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal)
    }

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                fraction = 0.5f
            ),
            childAlignment = ChildAlignment(
                fraction = 0.5f
            ),
            loopDirection = DpadLoopDirection.MIN_MAX
        )
    }

    @Test
    fun testLayoutDoesNotLoopUntilItHasEnoughItems() {
        launchFragment(
            getDefaultAdapterConfiguration().copy(numberOfItems = 1)
        )
        val itemBounds = getItemViewBounds(position = 0)
        val itemWidth = itemBounds.width()
        val recyclerViewBounds = getRecyclerViewBounds()
        val requiredItemsForLoop =
            ceil((recyclerViewBounds.width() + itemWidth) / itemWidth.toDouble()).toInt()
        val maxItemsInLayoutWithoutLoop =
            ceil((recyclerViewBounds.width() / 2.0) / itemWidth.toDouble()).toInt()
        val maxItemsInLayoutWithLoop =
            ceil(recyclerViewBounds.width() / itemWidth.toDouble()).toInt()
        var currentItems = 1

        while (currentItems < requiredItemsForLoop) {
            onRecyclerView("Asserting not looping children") { recyclerView ->
                assertThat(recyclerView.childCount)
                    .isEqualTo(min(currentItems, maxItemsInLayoutWithoutLoop))
            }
            currentItems++
            mutateAdapter { adapter ->
                adapter.add()
            }
            waitForAnimation()
        }

        onRecyclerView("Asserting looping children") { recyclerView ->
            assertThat(recyclerView.childCount).isEqualTo(maxItemsInLayoutWithLoop)
        }
    }

    @Test
    fun testLayoutLoopsAroundForEnoughItems() {
        launchFragment()
        val currentBounds = getItemViewBounds(position = 0)
        var currentPosition = getDefaultAdapterConfiguration().numberOfItems - 1
        currentBounds.offset(-currentBounds.width(), 0)
        while (currentBounds.right > 0) {
            assertThat(getItemViewBounds(currentPosition)).isEqualTo(currentBounds)
            currentBounds.offset(-currentBounds.width(), 0)
            currentPosition--
        }
    }

    @Test
    fun testLayoutScrollsBackwardsToStartPosition() {
        launchFragment(getDefaultAdapterConfiguration().copy(numberOfItems = 10))

        repeat(10) {
            KeyEvents.pressLeft()
            waitForIdleScrollState()
        }

        val currentBounds = getItemViewBounds(position = 0)
        assertFocusAndSelection(position = 0)
        assertThat(currentBounds.centerX()).isEqualTo(getRecyclerViewBounds().centerX())
    }

    @Test
    fun testLayoutScrollsForwardsToStartPosition() {
        launchFragment(getDefaultAdapterConfiguration().copy(numberOfItems = 10))

        repeat(10) {
            KeyEvents.pressRight()
            waitForIdleScrollState()
        }

        val currentBounds = getItemViewBounds(position = 0)
        assertFocusAndSelection(position = 0)
        assertThat(currentBounds.centerX()).isEqualTo(getRecyclerViewBounds().centerX())
    }

    @Test
    fun testSmoothScrollingToPositionAtLoopSide() {
        launchFragment(getDefaultAdapterConfiguration().copy(numberOfItems = 10))

        selectPosition(position = 9, smooth = true)

        val currentBounds = getItemViewBounds(position = 9)
        assertThat(currentBounds.centerX()).isEqualTo(getRecyclerViewBounds().centerX())
    }

    @Test
    fun testLayoutDoesNotLoopWhenReturningToStart() {
        launchFragment(
            adapterConfiguration = getDefaultAdapterConfiguration().copy(numberOfItems = 10),
            layoutConfiguration = getDefaultLayoutConfiguration().copy(
                loopDirection = DpadLoopDirection.MAX
            )
        )
        val startChildrenBounds = getChildrenBounds()

        // By default no start loop should exist
        onRecyclerView("Asserting not looping start") { recyclerView ->
            assertThat(recyclerView.findViewHolderForAdapterPosition(9)).isNull()
        }

        // Scroll forwards to begin the loop
        repeat(10) {
            KeyEvents.pressRight()
            waitForIdleScrollState()
        }

        assertFocusAndSelection(position = 0)
        assertThat(getItemViewBounds(position = 0).centerX())
            .isEqualTo(getRecyclerViewBounds().centerX())

        // Scroll backwards and validate start loop doesn't exist
        repeat(15) {
            KeyEvents.pressLeft()
            waitForIdleScrollState()
        }
        assertFocusAndSelection(position = 0)
        assertThat(getChildrenBounds()).isEqualTo(startChildrenBounds)
    }


}
