package com.rubensousa.dpadrecyclerview.test.tests

import android.view.Gravity
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.*
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import org.junit.Rule
import org.junit.Test

class VerticalAlignmentTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = 0,
                offsetPercent = 50f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetPercent = 50f
            )
        )
    }

    @Test
    fun testMiddleItemsAreAlignedToContainerOffsets() {
        launchFragment()
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
            position++
        }

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 100
            )
        )
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() + 100)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 0
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = -100
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() - 100)
    }

    @Test
    fun testMiddleItemsAreAlignedToItemOffsets() {
        launchFragment()
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
            position++
        }

        updateChildAlignment(ChildAlignment(offset = 100))
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() + 100)

        updateChildAlignment(ChildAlignment(offset = 0))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateChildAlignment(ChildAlignment(offset = -100))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY() - 100)
    }

    @Test
    fun testFirstItemAlignmentForEdgeAlignments() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(viewBounds.height() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(viewBounds.height() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
    }

    @Test
    fun testLastItemAlignmentForEdgeAlignments() {
        launchFragment()
        val lastPosition = selectLastPosition()
        val recyclerViewBounds = getRecyclerViewBounds()

        var viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)
    }

    @Test
    fun testItemsAreAlignedToContainerOffset() {
        val offset = 100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = offset,
                offsetPercent = 50f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetPercent = 0f
            )
        )
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.top).isEqualTo(recyclerViewBounds.centerY() + offset)
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Test
    fun testItemsAreAlignedToBothContainerAndItemAlignmentOffsets() {
        val containerOffset = 100
        val itemOffset = 100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = containerOffset,
                offsetPercent = 0f
            ),
            childAlignment = ChildAlignment(
                offset = itemOffset,
                offsetPercent = 0f
            )
        )
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.top)
                .isEqualTo(recyclerViewBounds.top + containerOffset + itemOffset)
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Test
    fun testItemsAreAlignedToBothContainerAndItemAlignmentOffsetPercentages() {
        val containerOffset = 100
        val itemOffset = 100
        launchFragment(
            parentAlignment = ParentAlignment(
                edge = Edge.MIN_MAX,
                offset = containerOffset,
                offsetPercent = 50f
            ),
            childAlignment = ChildAlignment(
                offset = itemOffset,
                offsetPercent = 50f
            )
        )
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.centerY())
                .isEqualTo(recyclerViewBounds.centerY() + containerOffset + itemOffset)
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_DOWN)
        }
    }

    @Test
    fun testGravityAffectsBoundsOfItems() {
        val parentAlignment = ParentAlignment(
            edge = Edge.NONE,
            gravity = Gravity.CENTER,
            offset = 0,
            offsetPercent = 50f
        )
        val layoutConfig = TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = parentAlignment,
            childAlignment = ChildAlignment(
                offset = 0,
                offsetPercent = 50f
            )
        )
        val adapterConfig = TestAdapterConfiguration(
            itemLayoutId = R.layout.test_item_horizontal
        )
        launchFragment(layoutConfig, adapterConfig)

        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(parentAlignment.copy(gravity = Gravity.END))

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.right).isEqualTo(recyclerViewBounds.right)

        updateParentAlignment(parentAlignment.copy(gravity = Gravity.START))

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())
        assertThat(viewBounds.left).isEqualTo(recyclerViewBounds.left)
    }

}
