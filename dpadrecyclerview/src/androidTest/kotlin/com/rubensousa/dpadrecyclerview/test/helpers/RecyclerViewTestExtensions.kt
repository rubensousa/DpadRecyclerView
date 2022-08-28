package com.rubensousa.dpadrecyclerview.test.helpers

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.matcher.ViewMatchers
import com.rubensousa.dpadrecyclerview.test.actions.*
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.test.assertions.FocusAssertion
import com.rubensousa.dpadrecyclerview.test.assertions.SelectionAssertion
import com.rubensousa.dpadrecyclerview.test.assertions.ViewHolderSelectedAssertion
import com.rubensousa.dpadrecyclerview.test.R

fun selectLastPosition(smooth: Boolean = false, id: Int = R.id.recyclerView): Int {
    var selectedPosition: Int = RecyclerView.NO_POSITION
    Espresso.onView(ViewMatchers.withId(id))
        .perform(SelectLastPositionAction(smooth) { position -> selectedPosition = position })
    if (smooth) {
        Espresso.onView(ViewMatchers.withId(id)).perform(WaitForIdleScrollAction())
    }
    return selectedPosition
}

fun selectPosition(position: Int, smooth: Boolean = false, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id))
        .perform(SelectPositionAction(position, smooth))
    if (smooth) {
        Espresso.onView(ViewMatchers.withId(id)).perform(WaitForIdleScrollAction())
    }
}

fun selectPosition(
    position: Int,
    subPosition: Int,
    smooth: Boolean = false,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(ViewMatchers.withId(id))
        .perform(SelectPositionsAction(position, subPosition, smooth))
    if (smooth) {
        Espresso.onView(ViewMatchers.withId(id)).perform(WaitForIdleScrollAction())
    }
}

fun selectSubPosition(
    subPosition: Int,
    smooth: Boolean = false,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(ViewMatchers.withId(id))
        .perform(SelectSubPositionAction(subPosition, smooth))
    if (smooth) {
        Espresso.onView(ViewMatchers.withId(id)).perform(WaitForIdleScrollAction())
    }
}

fun assertSelectedPosition(position: Int, subPosition: Int = 0, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id))
        .check(SelectionAssertion(position, subPosition))
}

fun assertViewHolderSelected(
    position: Int,
    isSelected: Boolean = true,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(ViewMatchers.withId(id))
        .check(ViewHolderSelectedAssertion(position, isSelected))
}

fun assertFocusPosition(position: Int, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id))
        .check(FocusAssertion(focusedPosition = position))
}

fun getItemViewBounds(position: Int, id: Int = R.id.recyclerView): Rect {
    val rect = Rect()
    Espresso.onView(ViewMatchers.withId(id))
        .perform(
            WaitForIdleScrollAction(),
            ViewHolderItemViewAction(position, GetViewBoundsAction(rect))
        )
    return rect
}

fun getRecyclerViewBounds(id: Int = R.id.recyclerView): Rect {
    val rect = Rect()
    Espresso.onView(ViewMatchers.withId(id)).perform(
        WaitForIdleScrollAction(),
        GetViewBoundsAction(rect)
    )
    return rect
}

fun updateParentAlignment(alignment: ParentAlignment, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(
        WaitForIdleScrollAction(),
        UpdateParentAlignmentAction(alignment)
    )
}

fun updateChildAlignment(alignment: ChildAlignment, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(
        WaitForIdleScrollAction(),
        UpdateChildAlignmentAction(alignment)
    )
}

fun waitForIdleScrollState(id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(WaitForIdleScrollAction())
}

fun waitForAdapterUpdate(id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(WaitForAdapterUpdateAction())
}

fun onRecyclerView(
    label: String,
    id: Int = R.id.recyclerView,
    waitForIdle: Boolean = true,
    action: (recyclerView: DpadRecyclerView) -> Unit
) {
    Espresso.onView(ViewMatchers.withId(id)).perform(object : DpadRecyclerViewAction(
        label, waitForIdle
    ) {
        override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
            action(recyclerView)
        }
    })
}

fun performDpadRecyclerViewAction(action: DpadRecyclerViewAction, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(action)
}