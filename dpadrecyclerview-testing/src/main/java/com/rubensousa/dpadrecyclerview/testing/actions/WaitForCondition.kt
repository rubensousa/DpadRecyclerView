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
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.util.HumanReadables
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class WaitForCondition<T : View>(
    description: String,
    private val condition: (view: T) -> Boolean,
    timeout: Long = 5,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
) : DpadViewActions.DpadViewAction(description) {

    private val waiter = ActionWaiter(timeout, timeoutUnit)

    override fun perform(uiController: UiController, view: View) {
        val idlingResource = ConditionIdleResource(condition, view as T)
        val isIdleNow = waiter.waitFor(idlingResource, uiController)
        if (!isIdleNow) {
            throw PerformException.Builder()
                .withActionDescription(description)
                .withCause(TimeoutException("Waited ${waiter.getTimeoutMillis()} milliseconds"))
                .withViewDescription(HumanReadables.describe(view))
                .build()
        }
        uiController.loopMainThreadForAtLeast(300L)
    }

    class ConditionIdleResource<T : View>(
        private val condition: (view: T) -> Boolean,
        private val view: T
    ) : IdlingResource {

        override fun getName(): String = this::class.simpleName ?: ""

        override fun isIdleNow(): Boolean = condition(view)

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {}

    }

}
