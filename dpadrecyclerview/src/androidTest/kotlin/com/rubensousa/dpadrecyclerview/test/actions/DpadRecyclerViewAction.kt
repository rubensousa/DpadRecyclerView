package com.rubensousa.dpadrecyclerview.test.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import org.hamcrest.Matcher
import org.hamcrest.Matchers

abstract class DpadRecyclerViewAction(
    private val label: String,
    private val waitForIdle: Boolean = true
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(DpadRecyclerView::class.java)
    }

    override fun getDescription(): String = label

    override fun perform(uiController: UiController, view: View) {
        perform(uiController, view as DpadRecyclerView)
        if (waitForIdle) {
            uiController.loopMainThreadUntilIdle()
        }
    }

    abstract fun perform(uiController: UiController, recyclerView: DpadRecyclerView)

}
