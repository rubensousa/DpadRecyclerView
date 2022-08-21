package com.rubensousa.dpadrecyclerview.test.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class SelectPositionsAction(
    private val position: Int,
    private val subPosition: Int,
    private val smooth: Boolean
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(DpadRecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Selecting position $position"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as DpadRecyclerView
        recyclerView.setSelectedPosition(position, subPosition, smooth)
    }
}