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

package com.rubensousa.dpadrecyclerview.testing.actions

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.util.HumanReadables
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.concurrent.TimeUnit

/**
 * Useful [ViewAction]s for [DpadRecyclerView]. For other [ViewAction], check [DpadViewActions]
 */
object DpadRecyclerViewActions {

    @JvmStatic
    fun selectLastPosition(
        smooth: Boolean,
        onPositionSelected: (position: Int) -> Unit = {}
    ): ViewAction {
        return SelectLastPositionAction(smooth, onPositionSelected)
    }

    @JvmStatic
    fun selectPosition(
        position: Int,
        subPosition: Int = 0,
        smooth: Boolean = true,
    ): ViewAction {
        return SelectPositionAction(position, subPosition, smooth)
    }

    @JvmStatic
    fun selectSubPosition(
        subPosition: Int,
        smooth: Boolean = true,
    ): ViewAction {
        return SelectSubPositionAction(subPosition, smooth)
    }

    @JvmStatic
    fun getItemViewBounds(position: Int, rect: Rect): ViewAction {
        return executeForViewHolderItemViewAt(position, DpadViewActions.getViewBounds(rect))
    }

    @JvmStatic
    fun getRelativeItemViewBounds(position: Int, rect: Rect): ViewAction {
        return executeForViewHolderItemViewAt(
            position,
            DpadViewActions.getRelativeViewBounds(rect)
        )
    }

    @JvmStatic
    fun updateChildAlignment(alignment: ChildAlignment): ViewAction {
        return UpdateChildAlignmentAction(alignment)
    }

    @JvmStatic
    fun updateParentAlignment(alignment: ParentAlignment): ViewAction {
        return UpdateParentAlignmentAction(alignment)
    }

    @JvmStatic
    fun executeForViewHolderItemViewAt(position: Int, action: ViewAction): ViewAction {
        return ViewHolderItemViewAction(position, action)
    }

    @JvmStatic
    fun waitForAdapterUpdate(
        updates: Int = 1,
        failOnTimeout: Boolean = false,
        timeout: Long = 2,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): ViewAction {
        return WaitForAdapterUpdateAction(updates, failOnTimeout, timeout, timeoutUnit)
    }

    @JvmStatic
    fun waitForIdleScroll(
        timeout: Long = 5,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): ViewAction {
        return WaitForCondition<RecyclerView>(
            "Waiting for idle Scroll",
            { recyclerView -> recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE },
            timeout,
            timeoutUnit
        )
    }

    @JvmStatic
    fun execute(label: String, action: (recyclerView: DpadRecyclerView) -> Unit): ViewAction {
        return object : DpadRecyclerViewAction(label) {
            override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
                try {
                    action(recyclerView)
                } catch (exception: Exception) {
                    throw PerformException.Builder()
                        .withActionDescription(label)
                        .withCause(exception)
                        .withViewDescription(HumanReadables.describe(recyclerView))
                        .build()
                }
            }
        }
    }

    private class SelectPositionAction(
        private val position: Int,
        private val subPosition: Int,
        private val smooth: Boolean
    ) : DpadRecyclerViewAction("Selecting position $position with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            if (smooth) {
                if (subPosition > 0) {
                    recyclerView.setSelectedSubPositionSmooth(position, subPosition)
                } else {
                    recyclerView.setSelectedPositionSmooth(position)
                }
            } else {
                if (subPosition > 0) {
                    recyclerView.setSelectedSubPosition(position, subPosition)
                } else {
                    recyclerView.setSelectedPosition(position)
                }
            }
        }
    }

    private class SelectLastPositionAction(
        private val smooth: Boolean,
        private val onPositionSelected: (position: Int) -> Unit = {}
    ) : DpadRecyclerViewAction("Selecting last position") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            val itemCount = recyclerView.adapter?.itemCount ?: return
            val lastPosition = itemCount - 1
            if (smooth) {
                recyclerView.setSelectedPositionSmooth(lastPosition)
            } else {
                recyclerView.setSelectedPosition(lastPosition)
            }
            onPositionSelected(lastPosition)
        }
    }

    private class SelectSubPositionAction(
        private val subPosition: Int,
        private val smooth: Boolean
    ) : DpadRecyclerViewAction("Selecting subPosition $subPosition with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            if (smooth) {
                recyclerView.setSelectedSubPositionSmooth(subPosition)
            } else {
                recyclerView.setSelectedSubPosition(subPosition)
            }
        }

    }

    private class UpdateChildAlignmentAction(
        private val alignment: ChildAlignment,
        private val smooth: Boolean = false
    ) : DpadRecyclerViewAction("Updating child alignment to: $alignment with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            recyclerView.setChildAlignment(alignment, smooth)
        }

    }

    private class UpdateParentAlignmentAction(
        private val alignment: ParentAlignment,
        private val smooth: Boolean = false
    ) : DpadRecyclerViewAction("Updating parent alignment to: $alignment with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            recyclerView.setParentAlignment(alignment, smooth)
        }

    }

    private class ViewHolderItemViewAction(
        private val position: Int,
        private val action: ViewAction
    ) : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return Matchers.allOf(
                ViewMatchers.isAssignableFrom(RecyclerView::class.java)
            )
        }

        override fun getDescription(): String {
            return "Performing ViewAction: $action.description on item at position $position"
        }

        override fun perform(uiController: UiController, view: View) {
            val recyclerView = view as RecyclerView
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
                ?: throw PerformException.Builder()
                    .withActionDescription(this.toString())
                    .withViewDescription(HumanReadables.describe(view))
                    .withCause(IllegalStateException("No view holder at position: $position"))
                    .build()
            action.perform(uiController, viewHolder.itemView)
        }

    }

}
