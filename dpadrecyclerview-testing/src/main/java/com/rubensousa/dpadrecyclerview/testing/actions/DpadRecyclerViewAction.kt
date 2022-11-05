package com.rubensousa.dpadrecyclerview.testing.actions

import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import org.hamcrest.Matcher
import org.hamcrest.Matchers

internal abstract class DpadRecyclerViewAction(
    private val label: String,
    private val loopMainThreadUntilIdle: Boolean = true
) : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(DpadRecyclerView::class.java)
    }

    override fun getDescription(): String = label

    override fun perform(uiController: UiController, view: View) {
        perform(uiController, view as DpadRecyclerView)
        if (loopMainThreadUntilIdle) {
            uiController.loopMainThreadUntilIdle()
        }
    }

    abstract fun perform(uiController: UiController, recyclerView: DpadRecyclerView)

}
