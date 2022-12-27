/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.testing.actions

import android.graphics.Rect
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.concurrent.TimeUnit

/**
 * Useful [ViewAction] for plain views
 */
object DpadViewActions {

    @JvmStatic
    fun getViewBounds(rect: Rect): ViewAction {
        return GetViewBoundsAction(rect, relativeToParent = false)
    }

    @JvmStatic
    fun getRelativeViewBounds(rect: Rect): ViewAction {
        return GetViewBoundsAction(rect, relativeToParent = true)
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

    @JvmStatic
    fun <T : View> waitForCondition(
        description: String,
        condition: (view: T) -> Boolean,
        timeout: Long = 5,
        timeoutUnit: TimeUnit = TimeUnit.SECONDS
    ): ViewAction {
        return WaitForCondition(description, condition, timeout, timeoutUnit)
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
        private val rect: Rect,
        private val relativeToParent: Boolean
    ) : DpadViewAction("Retrieving View Bounds") {

        override fun perform(uiController: UiController, view: View) {
            if (relativeToParent) {
                rect.left = view.left
                rect.top = view.top
                rect.right = view.right
                rect.bottom = view.bottom
            } else {
                val location = IntArray(2)
                view.getLocationInWindow(location)
                rect.left = location[0]
                rect.top = location[1]
                rect.right = rect.left + view.width
                rect.bottom = rect.top + view.height
            }
        }
    }

    abstract class DpadViewAction(private val description: String) : ViewAction {
        override fun getConstraints(): Matcher<View> = Matchers.isA(View::class.java)
        override fun getDescription(): String = description
    }

}