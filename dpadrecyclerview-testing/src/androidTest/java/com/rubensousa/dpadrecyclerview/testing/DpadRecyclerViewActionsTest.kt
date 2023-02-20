/*
 * Copyright 2022 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.testing

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class DpadRecyclerViewActionsTest : RecyclerViewTest() {

    @get:Rule
    val disableIdleTimeoutRule = DisableIdleTimeoutRule()

    private val defaultSize = 1000

    @Test
    fun testChangingSelectionSmooth() {
        launchGridFragment()
        val targetPosition = 20
        performActions(
            DpadRecyclerViewActions.selectPosition(
                position = targetPosition,
                smooth = true
            )
        )

        performActions(DpadRecyclerViewActions.waitForIdleScroll())

        var currentPosition = 0
        performActions(DpadRecyclerViewActions.execute("Retrieving current position") { recyclerView ->
            currentPosition = recyclerView.getSelectedPosition()
        })
        assertThat(currentPosition).isEqualTo(targetPosition)
    }

    @Test
    fun testChangingSelectionImmediate() {
        launchGridFragment()
        val targetPosition = 20
        performActions(
            DpadRecyclerViewActions.selectPosition(
                position = targetPosition, smooth = false
            )
        )

        var currentPosition = 0
        performActions(DpadRecyclerViewActions.execute("Retrieving current position") { recyclerView ->
            currentPosition = recyclerView.getSelectedPosition()
        })
        assertThat(currentPosition).isEqualTo(targetPosition)
    }

    @Test
    fun testChangingToLastPosition() {
        launchGridFragment()
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

        assertThat(retrievedPosition).isEqualTo(targetPosition)
    }

    @Test
    fun testChangingSubSelectionSmooth() {
        launchSubPositionFragment()
        val targetSubPosition = 1
        performActions(
            DpadRecyclerViewActions.selectSubPosition(
                subPosition = targetSubPosition
            )
        )
        performActions(DpadRecyclerViewActions.waitForIdleScroll())

        var currentSubPosition = 0
        performActions(DpadRecyclerViewActions.execute("Retrieving current position") { recyclerView ->
            currentSubPosition = recyclerView.getSelectedSubPosition()
        })
        assertThat(currentSubPosition).isEqualTo(targetSubPosition)
    }

    @Test
    fun testChangingSubSelectionImmediate() {
        launchSubPositionFragment()
        val targetSubPosition = 1
        performActions(
            DpadRecyclerViewActions.selectSubPosition(
                subPosition = targetSubPosition,
                smooth = false
            )
        )
        performActions(DpadRecyclerViewActions.waitForIdleScroll())

        var currentSubPosition = 0
        performActions(DpadRecyclerViewActions.execute("Retrieving current position") { recyclerView ->
            currentSubPosition = recyclerView.getSelectedSubPosition()
        })
        assertThat(currentSubPosition).isEqualTo(targetSubPosition)
    }

    @Test
    fun testChangingPositionWithSubSelectionSmooth() {
        launchSubPositionFragment()
        val targetPosition = 20
        val targetSubPosition = 2
        performActions(
            DpadRecyclerViewActions.selectPosition(
                position = targetPosition,
                subPosition = targetSubPosition,
                smooth = true
            )
        )
        performActions(DpadRecyclerViewActions.waitForIdleScroll())

        var currentSubPosition = 0
        var currentPosition = 0

        performActions(DpadRecyclerViewActions.execute("Retrieving current position") { recyclerView ->
            currentPosition = recyclerView.getSelectedPosition()
            currentSubPosition = recyclerView.getSelectedSubPosition()
        })
        assertThat(currentPosition).isEqualTo(targetPosition)
        assertThat(currentSubPosition).isEqualTo(targetSubPosition)
    }

    @Test
    fun testChangingPositionWithSubSelectionImmediate() {
        launchSubPositionFragment()
        val targetPosition = 20
        val targetSubPosition = 2
        performActions(
            DpadRecyclerViewActions.selectPosition(
                position = targetPosition,
                subPosition = targetSubPosition,
                smooth = false
            )
        )

        var currentSubPosition = 0
        var currentPosition = 0

        performActions(DpadRecyclerViewActions.execute("Retrieving current position") { recyclerView ->
            currentPosition = recyclerView.getSelectedPosition()
            currentSubPosition = recyclerView.getSelectedSubPosition()
        })
        assertThat(currentPosition).isEqualTo(targetPosition)
        assertThat(currentSubPosition).isEqualTo(targetSubPosition)
    }

    @Test
    fun testRetrievingItemViewBounds() {
        launchGridFragment()
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
        launchGridFragment()
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
        launchGridFragment()
        var currentSize = defaultSize
        onGridFragment { fragment ->
            fragment.insertItem()
        }
        currentSize++
        performActions(DpadRecyclerViewActions.selectLastPosition(smooth = false))
        performActions(DpadRecyclerViewActions.waitForAdapterUpdate())

        assertGridAdapterCount(currentSize)
    }

    @Test
    fun testWaitingForAdapterRemoval() {
        launchGridFragment()
        var currentSize = defaultSize
        onGridFragment { fragment ->
            fragment.removeItem()
        }
        currentSize--
        performActions(
            DpadRecyclerViewActions.selectLastPosition(smooth = false),
            DpadRecyclerViewActions.waitForAdapterUpdate()
        )

        assertGridAdapterCount(currentSize)
    }

    @Test
    fun testWaitingForCompleteAdapterRemoval() {
        launchGridFragment()
        onGridFragment { fragment ->
            fragment.clearItems()
        }
        performActions(DpadRecyclerViewActions.waitForAdapterUpdate())

        assertGridAdapterCount(0)
    }

    @Test
    fun testWaitingForAdapterMove() {
        launchGridFragment()
        onGridFragment { fragment ->
            fragment.moveLastItem()
        }
        performActions(
            DpadRecyclerViewActions.selectLastPosition(smooth = false),
            DpadRecyclerViewActions.waitForAdapterUpdate()
        )

        performActions(DpadRecyclerViewActions.execute("Checking last item tag") { recyclerView ->
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(0)
            assertThat(viewHolder?.itemView?.tag).isEqualTo(999)
        })
    }

    @Test
    fun testWaitingForAdapterChange() {
        launchGridFragment()
        onGridFragment { fragment ->
            fragment.changeLastItem()
        }
        performActions(
            DpadRecyclerViewActions.selectLastPosition(smooth = false),
            DpadRecyclerViewActions.waitForAdapterUpdate()
        )

        performActions(DpadRecyclerViewActions.execute("Checking last item tag") { recyclerView ->
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(defaultSize - 1)
            assertThat(viewHolder?.itemView?.tag).isEqualTo(-999)
        })
    }

    @Test
    fun testWaitingForMultipleAdapterUpdates() {
        launchGridFragment()
        onGridFragment { fragment ->
            fragment.changeLastItem()
        }
        onGridFragment { fragment ->
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

    @Test
    fun testChangingChildAlignment() {
        launchGridFragment()

        val newChildAlignment = ChildAlignment(
            offset = 40,
            fraction = 0.0f
        )

        performActions(DpadRecyclerViewActions.updateChildAlignment(newChildAlignment))

        var childAlignment: ChildAlignment? = null
        performActions(DpadRecyclerViewActions.execute("Get current child alignment")
        { recyclerView ->
            childAlignment = recyclerView.getChildAlignment()
        })

        assertThat(childAlignment).isEqualTo(newChildAlignment)
    }

    @Test
    fun testChangingParentAlignment() {
        launchGridFragment()

        val newParentAlignment = ParentAlignment(
            offset = 0,
            fraction = 0.0f
        )

        performActions(DpadRecyclerViewActions.updateParentAlignment(newParentAlignment))

        var parentAlignment: ParentAlignment? = null
        performActions(DpadRecyclerViewActions.execute("Get current child alignment")
        { recyclerView ->
            parentAlignment = recyclerView.getParentAlignment()
        })

        assertThat(parentAlignment).isEqualTo(newParentAlignment)
    }

}
