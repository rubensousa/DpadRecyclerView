package com.rubensousa.dpadrecyclerview.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class UpdateParentAlignmentAction(private val newConfig: ParentAlignment) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(DpadRecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Updating item alignment config to: $newConfig"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as DpadRecyclerView
        recyclerView.setParentAlignment(newConfig)
        uiController.loopMainThreadUntilIdle()
    }

}
