package com.rubensousa.dpadrecyclerview.test.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class SelectSubPositionAction(
    private val subPosition: Int,
    private val smooth: Boolean
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(DpadRecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Selecting subposition $subPosition"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as DpadRecyclerView
        if (smooth) {
            recyclerView.setSelectedSubPositionSmooth(subPosition)
        } else {
            recyclerView.setSelectedSubPosition(subPosition)
        }
    }
}