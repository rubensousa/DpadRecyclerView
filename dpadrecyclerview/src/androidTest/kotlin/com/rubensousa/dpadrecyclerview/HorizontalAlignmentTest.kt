package com.rubensousa.dpadrecyclerview

import android.view.Gravity
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.helpers.*
import com.rubensousa.dpadrecyclerview.test.R
import org.junit.Rule
import org.junit.Test

class HorizontalAlignmentTest : GridTest() {

    @get:Rule
    val fastUiAutomatorRule = FastUiAutomatorRule()

    override fun getDefaultLayoutConfiguration(): TestGridFragment.LayoutConfiguration {
        return TestGridFragment.LayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
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

    override fun getDefaultAdapterConfiguration(): TestGridFragment.AdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(
            itemLayoutId = R.layout.test_item_horizontal
        )
    }

    @Test
    fun testMiddleItemsAreAlignedToContainerOffsets() {
        launchFragment()
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)
            position++
        }

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 100
            )
        )
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() + 100)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = 0
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN_MAX, offset = -100
            )
        )
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() - 100)
    }

    @Test
    fun testMiddleItemsAreAlignedToItemOffsets() {
        launchFragment()
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)

        val recyclerViewBounds = getRecyclerViewBounds()
        var position = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = position)
            assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)
            position++
        }

        updateChildAlignment(ChildAlignment(offset = 100))
        var viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() + 100)

        updateChildAlignment(ChildAlignment(offset = 0))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateChildAlignment(ChildAlignment(offset = -100))
        viewBounds = getItemViewBounds(position = position)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX() - 100)
    }

    @Test
    fun testFirstItemAlignmentForEdgeAlignments() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(viewBounds.width() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(viewBounds.width() / 2)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
    }

    @Test
    fun testLastItemAlignmentForEdgeAlignments() {
        launchFragment()
        val lastPosition = selectLastPosition()
        val recyclerViewBounds = getRecyclerViewBounds()

        var viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.right).isEqualTo(recyclerViewBounds.right)

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.NONE,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MIN,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())

        updateParentAlignment(
            ParentAlignment(
                edge = Edge.MAX,
                offset = 0,
                offsetPercent = 50f
            )
        )

        viewBounds = getItemViewBounds(position = lastPosition)
        assertThat(viewBounds.right).isEqualTo(recyclerViewBounds.right)
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
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.left).isEqualTo(recyclerViewBounds.centerX() + offset)
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)
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
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.left)
                .isEqualTo(recyclerViewBounds.left + containerOffset + itemOffset)
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)
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
        UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times = 5)
        val recyclerViewBounds = getRecyclerViewBounds()
        val startPosition = 5
        repeat(5) {
            val viewBounds = getItemViewBounds(position = startPosition + it)
            assertThat(viewBounds.centerX())
                .isEqualTo(recyclerViewBounds.centerX() + containerOffset + itemOffset)
            UiAutomatorHelper.pressKey(KeyEvent.KEYCODE_DPAD_RIGHT)
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
        val layoutConfig = TestGridFragment.LayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = parentAlignment,
            childAlignment = ChildAlignment(
                offset = 0,
                offsetPercent = 50f
            )
        )
        launchFragment(layoutConfig)

        val recyclerViewBounds = getRecyclerViewBounds()
        var viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
        assertThat(viewBounds.centerY()).isEqualTo(recyclerViewBounds.centerY())

        updateParentAlignment(parentAlignment.copy(gravity = Gravity.BOTTOM))

        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
        assertThat(viewBounds.bottom).isEqualTo(recyclerViewBounds.bottom)

        updateParentAlignment(parentAlignment.copy(gravity = Gravity.TOP))
        viewBounds = getItemViewBounds(position = 0)
        assertThat(viewBounds.centerX()).isEqualTo(recyclerViewBounds.centerX())
        assertThat(viewBounds.top).isEqualTo(recyclerViewBounds.top)
    }

}
