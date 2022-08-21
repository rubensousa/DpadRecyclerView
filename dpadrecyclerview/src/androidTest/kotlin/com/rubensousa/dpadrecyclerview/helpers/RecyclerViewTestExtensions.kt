package com.rubensousa.dpadrecyclerview.helpers

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.rubensousa.dpadrecyclerview.actions.*
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.assertions.FocusAssertion
import com.rubensousa.dpadrecyclerview.assertions.SelectionAssertion
import com.rubensousa.dpadrecyclerview.assertions.ViewHolderSelectedAssertion
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
