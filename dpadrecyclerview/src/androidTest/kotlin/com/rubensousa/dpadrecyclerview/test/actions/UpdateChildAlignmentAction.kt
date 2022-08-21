package com.rubensousa.dpadrecyclerview.test.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class UpdateChildAlignmentAction(private val alignment: ChildAlignment) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(DpadRecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Updating item alignment config to: $alignment"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as DpadRecyclerView
        recyclerView.setChildAlignment(alignment)
        uiController.loopMainThreadUntilIdle()
    }

}
