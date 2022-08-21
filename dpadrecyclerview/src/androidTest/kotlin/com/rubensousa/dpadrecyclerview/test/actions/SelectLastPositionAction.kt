package com.rubensousa.dpadrecyclerview.test.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class SelectLastPositionAction(
    private val smooth: Boolean,
    private val onPositionSelected : (position: Int) -> Unit
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(DpadRecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Waiting for idle scroll state"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as DpadRecyclerView
        val itemCount = recyclerView.adapter?.itemCount ?: return
        val lastPosition = itemCount - 1
        recyclerView.setSelectedPosition(lastPosition, smooth)
        onPositionSelected(lastPosition)
    }
}