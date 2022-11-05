package com.rubensousa.dpadrecyclerview.testing.actions

import android.graphics.Rect
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

/**
 * Useful [ViewAction] for plain views
 */
object DpadViewActions {

    @JvmStatic
    fun getViewBounds(rect: Rect): ViewAction {
        return GetViewBoundsAction(rect)
    }

    /**
     * Note: this will only work if there's something else in your View hierarchy that can take focus
     */
    @JvmStatic
    fun clearFocus(): ViewAction {
        return ClearFocusAction()
    }

    @JvmStatic
    fun requestFocus(): ViewAction {
        return RequestFocusAction()
    }

    private class RequestFocusAction : DpadViewAction("Requesting view focus") {
        override fun perform(uiController: UiController, view: View) {
            view.requestFocus()
        }
    }

    private class ClearFocusAction : DpadViewAction("Clearing view focus") {
        override fun perform(uiController: UiController, view: View) {
            view.clearFocus()
        }
    }

    private class GetViewBoundsAction(
        private val rect: Rect
    ) : DpadViewAction("Retrieving View Bounds") {

        override fun perform(uiController: UiController, view: View) {
            view.getGlobalVisibleRect(rect)
        }

    }

    private abstract class DpadViewAction(private val description: String) : ViewAction {
        override fun getConstraints(): Matcher<View> = Matchers.isA(View::class.java)
        override fun getDescription(): String = description
    }

}