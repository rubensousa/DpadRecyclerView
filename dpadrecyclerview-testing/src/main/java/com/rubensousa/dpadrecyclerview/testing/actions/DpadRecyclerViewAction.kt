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
