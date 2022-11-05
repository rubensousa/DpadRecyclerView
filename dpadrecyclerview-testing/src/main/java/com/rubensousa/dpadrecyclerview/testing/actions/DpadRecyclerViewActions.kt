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
    fun getViewBounds(rect: Rect): ViewAction {
        return GetViewBoundsAction(rect)
    }

    @JvmStatic
    fun getItemViewBounds(position: Int, rect: Rect): ViewAction {
        return executeForViewHolderItemViewAt(position, GetViewBoundsAction(rect))
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
        timeout: Long = 5,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): ViewAction {
        return WaitForAdapterUpdateAction(timeout, timeoutUnit)
    }

    @JvmStatic
    fun waitForIdleScroll(
        timeout: Long = 5,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): ViewAction {
        return WaitForIdleScrollAction(timeout, timeoutUnit)
    }

    @JvmStatic
    fun execute(label: String, action: (recyclerView: DpadRecyclerView) -> Unit): ViewAction {
        return object : DpadRvAction(label) {
            override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
                action(recyclerView)
            }
        }
    }

    private class GetViewBoundsAction(private val rect: Rect) : ViewAction {

        override fun getConstraints(): Matcher<View> {
            return Matchers.isA(View::class.java)
        }

        override fun getDescription(): String {
            return "Retrieving view bounds"
        }

        override fun perform(uiController: UiController, view: View) {
            view.getGlobalVisibleRect(rect)
        }

    }

    private class SelectPositionAction(
        private val position: Int,
        private val subPosition: Int,
        private val smooth: Boolean
    ) : DpadRvAction("Selecting position $position with smoothScrolling: $smooth") {

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
    ) : DpadRvAction("Selecting last position") {

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
    ) : DpadRvAction("Selecting subPosition $subPosition with smoothScrolling: $smooth") {

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
    ) : DpadRvAction("Updating child alignment to: $alignment with smoothScrolling: $smooth") {

        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            recyclerView.setChildAlignment(alignment, smooth)
        }

    }

    private class UpdateParentAlignmentAction(
        private val alignment: ParentAlignment,
        private val smooth: Boolean = false
    ) : DpadRvAction("Updating parent alignment to: $alignment with smoothScrolling: $smooth") {

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
                ViewMatchers.isAssignableFrom(RecyclerView::class.java),
                ViewMatchers.isDisplayed()
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
