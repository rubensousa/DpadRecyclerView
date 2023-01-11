package com.rubensousa.dpadrecyclerview.testfixtures

import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import org.junit.Test

class VerticalGridLayoutTest {

    private val screenWidth = 1920
    private val screenHeight = 1080
    private val parentKeyline = screenHeight / 2
    private val childWidth = screenWidth / 3
    private val childHeight = 300
    private val childKeyline = 0.5f
    private val childTopKeyline = (parentKeyline - childHeight * childKeyline).toInt()
    private val grid = VerticalGridLayout(
        LayoutConfig(
            parentWidth = screenWidth,
            parentHeight = screenHeight,
            viewWidth = childWidth,
            viewHeight = childHeight,
            defaultItemCount = 1000,
            parentKeyline = screenHeight / 2,
            childKeyline = childKeyline
        ),
        spanCount = 3
    )
    private val expectedDefaultBounds = listOf(
        ViewBounds(left = 0, top = 390, right = 640, bottom = 690),
        ViewBounds(left = 640, top = 390, right = 1280, bottom = 690),
        ViewBounds(left = 1280, top = 390, right = 1920, bottom = 690),
        ViewBounds(left = 0, top = 690, right = 640, bottom = 990),
        ViewBounds(left = 640, top = 690, right = 1280, bottom = 990),
        ViewBounds(left = 1280, top = 690, right = 1920, bottom = 990),
        ViewBounds(left = 0, top = 990, right = 640, bottom = 1290),
        ViewBounds(left = 640, top = 990, right = 1280, bottom = 1290),
        ViewBounds(left = 1280, top = 990, right = 1920, bottom = 1290)
    )
    private val expectedSecondBounds = listOf(
        ViewBounds(left = 0, top = 90, right = 640, bottom = 390),
        ViewBounds(left = 640, top = 90, right = 1280, bottom = 390),
        ViewBounds(left = 1280, top = 90, right = 1920, bottom = 390),
        ViewBounds(left = 0, top = 390, right = 640, bottom = 690),
        ViewBounds(left = 640, top = 390, right = 1280, bottom = 690),
        ViewBounds(left = 1280, top = 390, right = 1920, bottom = 690),
        ViewBounds(left = 0, top = 690, right = 640, bottom = 990),
        ViewBounds(left = 640, top = 690, right = 1280, bottom = 990),
        ViewBounds(left = 1280, top = 690, right = 1920, bottom = 990),
        ViewBounds(left = 0, top = 990, right = 640, bottom = 1290),
        ViewBounds(left = 640, top = 990, right = 1280, bottom = 1290),
        ViewBounds(left = 1280, top = 990, right = 1920, bottom = 1290)
    )
    private val expectedOtherBounds = listOf(
        ViewBounds(left = 0, top = -210, right = 640, bottom = 90),
        ViewBounds(left = 640, top = -210, right = 1280, bottom = 90),
        ViewBounds(left = 1280, top = -210, right = 1920, bottom = 90),
        ViewBounds(left = 0, top = 90, right = 640, bottom = 390),
        ViewBounds(left = 640, top = 90, right = 1280, bottom = 390),
        ViewBounds(left = 1280, top = 90, right = 1920, bottom = 390),
        ViewBounds(left = 0, top = 390, right = 640, bottom = 690),
        ViewBounds(left = 640, top = 390, right = 1280, bottom = 690),
        ViewBounds(left = 1280, top = 390, right = 1920, bottom = 690),
        ViewBounds(left = 0, top = 690, right = 640, bottom = 990),
        ViewBounds(left = 640, top = 690, right = 1280, bottom = 990),
        ViewBounds(left = 1280, top = 690, right = 1920, bottom = 990),
        ViewBounds(left = 0, top = 990, right = 640, bottom = 1290),
        ViewBounds(left = 640, top = 990, right = 1280, bottom = 1290),
        ViewBounds(left = 1280, top = 990, right = 1920, bottom = 1290)
    )


    @Test
    fun `initial layout only renders views that should be visible`() {
        grid.init(position = 0)
        grid.assertViewBounds(expectedDefaultBounds)

        assertThat(grid.getLayoutStartOffset()).isEqualTo(expectedDefaultBounds.first().top)
        assertThat(grid.getLayoutEndOffset()).isEqualTo(expectedDefaultBounds.last().bottom)
    }

    @Test
    fun `scroll down does not add any extra space by default`() {
        grid.init(position = 0)

        grid.scrollDown()
        grid.assertViewBounds(expectedSecondBounds)

        assertThat(grid.getLayoutStartOffset()).isEqualTo(expectedSecondBounds.first().top)
        assertThat(grid.getLayoutEndOffset()).isEqualTo(expectedSecondBounds.last().bottom)

        grid.scrollDown()
        grid.assertViewBounds(expectedOtherBounds)

        assertThat(grid.getLayoutStartOffset()).isEqualTo(expectedOtherBounds.first().top)
        assertThat(grid.getLayoutEndOffset()).isEqualTo(expectedOtherBounds.last().bottom)
    }

    @Test
    fun `scroll up does not add any extra space by default`() {
        grid.init(position = grid.spanCount * 20)

        while (grid.selectedPosition != grid.spanCount) {
            grid.assertViewBounds(expectedOtherBounds)
            grid.scrollUp()
        }

        grid.assertViewBounds(expectedSecondBounds)

        grid.scrollUp()

        grid.assertViewBounds(expectedDefaultBounds)
    }

    @Test
    fun `layout at any position renders correct alignment`() {
        grid.init(position = grid.spanCount)
        grid.assertViewBounds(expectedSecondBounds)

        repeat(10) { index ->
            grid.init(position = grid.spanCount * (index + 2))
            grid.assertViewBounds(expectedOtherBounds)
        }
    }

    private fun generateEndBounds(referenceTop: Int, numberOfRows: Int): List<ViewBounds> {
        val bounds = ArrayList<ViewBounds>()
        repeat(numberOfRows) { index ->
            val rowTop = referenceTop + childHeight * index
            bounds.addAll(generateRowBounds(rowTop))
        }
        return bounds
    }

    private fun generateRowBounds(top: Int): List<ViewBounds> {
        return List(grid.spanCount) { index ->
            val left = index * childWidth
            ViewBounds(
                left = left,
                top = top,
                right = left + childWidth,
                bottom = top + childHeight
            )
        }
    }

}