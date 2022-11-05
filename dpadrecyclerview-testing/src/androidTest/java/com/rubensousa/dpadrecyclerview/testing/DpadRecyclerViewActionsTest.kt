package com.rubensousa.dpadrecyclerview.testing

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class DpadRecyclerViewActionsTest : DpadFragmentTest() {

    @get:Rule
    val disableIdleTimeoutRule = DisableIdleTimeoutRule()

    private val defaultSize = 1000

    @Test
    fun testChangingSelectionSmooth() {
        val targetPosition = 20
        performActions(
            DpadRecyclerViewActions.selectPosition(
                position = targetPosition,
                smooth = true
            )
        )

        val selectionEvents = getSelectionEvents()
        assertThat(selectionEvents.size).isEqualTo(2)
        assertThat(selectionEvents.last()).isEqualTo(DpadSelectionEvent(targetPosition))
    }

    @Test
    fun testChangingSelectionImmediate() {
        val targetPosition = 20
        performActions(
            DpadRecyclerViewActions.selectPosition(
                position = targetPosition, smooth = false
            )
        )

        val selectionEvents = getSelectionEvents()
        assertThat(selectionEvents.size).isEqualTo(2)
        assertThat(selectionEvents.last()).isEqualTo(DpadSelectionEvent(targetPosition))
    }

    @Test
    fun testChangingToLastPosition() {
        var targetPosition = 0
        performActions(DpadRecyclerViewActions.execute("Getting adapter item count") { recyclerView ->
            targetPosition = recyclerView.adapter!!.itemCount - 1
        })

        var retrievedPosition = 0
        performActions(
            DpadRecyclerViewActions.selectLastPosition(
                smooth = false, onPositionSelected = { position ->
                    retrievedPosition = position
                }
            )
        )

        val selectionEvents = getSelectionEvents()
        assertThat(selectionEvents.size).isEqualTo(2)
        assertThat(selectionEvents.last()).isEqualTo(DpadSelectionEvent(targetPosition))

        assertThat(retrievedPosition).isEqualTo(targetPosition)
    }

    @Test
    fun testRetrievingViewBounds() {
        val expectedRect = Rect()
        performActions(DpadRecyclerViewActions.execute("Getting view bounds") { recyclerView ->
            recyclerView.getGlobalVisibleRect(expectedRect)
        })

        val actualRect = Rect()
        performActions(DpadRecyclerViewActions.getViewBounds(actualRect))

        assertThat(actualRect).isEqualTo(expectedRect)
    }

    @Test
    fun testRetrievingItemViewBounds() {
        val expectedRect = Rect()
        val targetPosition = 15
        performActions(
            DpadRecyclerViewActions.selectPosition(position = targetPosition, smooth = false)
        )
        performActions(DpadRecyclerViewActions.execute("Getting item view bounds") { recyclerView ->
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(targetPosition)!!
            viewHolder.itemView.getGlobalVisibleRect(expectedRect)
        })

        val actualRect = Rect()
        performActions(DpadRecyclerViewActions.getItemViewBounds(targetPosition, actualRect))

        assertThat(actualRect).isEqualTo(expectedRect)
    }

    @Test
    fun testWaitingForIdleScroll() {
        val expectedScrollState = RecyclerView.SCROLL_STATE_IDLE
        repeat(15) {
            KeyEvents.pressDown()
        }

        var actualScrollState = RecyclerView.SCROLL_STATE_SETTLING

        performActions(DpadRecyclerViewActions.waitForIdleScroll())
        performActions(DpadRecyclerViewActions.execute("Getting scroll state") { recyclerView ->
            actualScrollState = recyclerView.scrollState
        })

        assertThat(actualScrollState).isEqualTo(expectedScrollState)
    }

    @Test
    fun testWaitingForAdapterInsertion() {
        var currentSize = defaultSize
        onFragment { fragment ->
            fragment.insertItem()
        }
        currentSize++
        performActions(DpadRecyclerViewActions.selectLastPosition(smooth = false))
        performActions(DpadRecyclerViewActions.waitForAdapterUpdate())

        assertAdapterCount(currentSize)
    }

    @Test
    fun testWaitingForAdapterRemoval() {
        var currentSize = defaultSize
        onFragment { fragment ->
            fragment.removeItem()
        }
        currentSize--
        performActions(DpadRecyclerViewActions.selectLastPosition(smooth = false))
        performActions(DpadRecyclerViewActions.waitForAdapterUpdate())

        assertAdapterCount(currentSize)
    }

    @Test
    fun testWaitingForAdapterMove() {
        onFragment { fragment ->
            fragment.moveLastItem()
        }
        performActions(DpadRecyclerViewActions.selectLastPosition(smooth = false))
        performActions(DpadRecyclerViewActions.waitForAdapterUpdate())

        performActions(DpadRecyclerViewActions.execute("Checking last item tag") { recyclerView ->
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(0)
            assertThat(viewHolder?.itemView?.tag).isEqualTo(999)
        })
    }

    @Test
    fun testWaitingForAdapterChange() {
        onFragment { fragment ->
            fragment.changeLastItem()
        }
        performActions(DpadRecyclerViewActions.selectLastPosition(smooth = false))
        performActions(DpadRecyclerViewActions.waitForAdapterUpdate())

        performActions(DpadRecyclerViewActions.execute("Checking last item tag") { recyclerView ->
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(defaultSize - 1)
            assertThat(viewHolder?.itemView?.tag).isEqualTo(-999)
        })
    }

    @Test
    fun testWaitingForMultipleAdapterUpdates() {
        onFragment { fragment ->
            fragment.changeLastItem()
        }
        onFragment { fragment ->
            fragment.insertItem()
        }
        performActions(
            DpadRecyclerViewActions.selectPosition(
                position = defaultSize - 2,
                smooth = false
            )
        )
        performActions(DpadRecyclerViewActions.waitForAdapterUpdate(updates = 2))
        performActions(DpadRecyclerViewActions.execute("Checking item tags") { recyclerView ->
            val previousLastViewHolder = recyclerView.findViewHolderForAdapterPosition(
                defaultSize - 1
            )
            assertThat(previousLastViewHolder?.itemView?.tag).isEqualTo(-999)

            val lastViewHolder = recyclerView.findViewHolderForAdapterPosition(defaultSize)
            assertThat(lastViewHolder?.itemView?.tag).isEqualTo(1000)
        })
    }


}
