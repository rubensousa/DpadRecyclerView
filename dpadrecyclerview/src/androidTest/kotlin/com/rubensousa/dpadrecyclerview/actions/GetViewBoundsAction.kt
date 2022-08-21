package com.rubensousa.dpadrecyclerview.actions

import android.graphics.Rect
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class GetViewBoundsAction(private val rect: Rect): ViewAction {

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
