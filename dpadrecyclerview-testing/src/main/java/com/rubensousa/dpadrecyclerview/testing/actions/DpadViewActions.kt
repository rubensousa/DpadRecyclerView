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