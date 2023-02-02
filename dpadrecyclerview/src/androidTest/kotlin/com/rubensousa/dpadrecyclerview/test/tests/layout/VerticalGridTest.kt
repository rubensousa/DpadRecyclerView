package com.rubensousa.dpadrecyclerview.test.tests.layout

import androidx.recyclerview.widget.RecyclerView
import androidx.test.platform.app.InstrumentationRegistry
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.*
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testfixtures.LayoutConfig
import com.rubensousa.dpadrecyclerview.testfixtures.VerticalGridLayout
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Before
import org.junit.Test

class VerticalGridTest : DpadRecyclerViewTest() {

    private val spanCount = 4
    private val numberOfItems = 200

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration().copy(
            numberOfItems = numberOfItems,
            itemLayoutId = R.layout.dpadrecyclerview_test_item_grid
        )
    }

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = spanCount,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            )
        )
    }

    private lateinit var grid: VerticalGridLayout

    @Before
    fun setup() {
        launchFragment()
        val recyclerViewBounds = getRecyclerViewBounds()
        val itemWidth = recyclerViewBounds.width() / spanCount
        val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
        val itemHeight = resources.getDimensionPixelOffset(
            R.dimen.dpadrecyclerview_test_grid_item_size
        )

        grid = VerticalGridLayout(
            config = LayoutConfig(
                parentWidth = recyclerViewBounds.width(),
                parentHeight = recyclerViewBounds.height(),
                viewWidth = itemWidth,
                viewHeight = itemHeight,
                defaultItemCount = numberOfItems,
                parentKeyline = recyclerViewBounds.height() / 2,
                childKeyline = 0.5f
            ),
            spanCount = spanCount
        )
    }

    @Test
    fun testInitialLayoutPositions() {
        grid.init(position = 0)
        assertChildrenPositions(grid)
    }

    @Test
    fun testMiddleLayoutPositions() {
        grid.init(position = 50)
        selectPosition(position = 50)
        assertChildrenPositions(grid)
    }

    @Test
    fun testEndLayoutPositions() {
        grid.init(position = numberOfItems - 1)
        selectPosition(position = numberOfItems - 1)
        assertChildrenPositions(grid)
    }

}
