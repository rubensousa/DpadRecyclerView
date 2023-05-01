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

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import java.util.concurrent.TimeUnit

internal class ActionWaiter(private val timeout: Long, private val timeoutUnit: TimeUnit) {

    fun getTimeoutMillis(): Long {
        return TimeUnit.MILLISECONDS.convert(timeout, timeoutUnit)
    }

    fun waitFor(idlingResource: IdlingResource, uiController: UiController): Boolean {
        val startTime = System.currentTimeMillis()
        val timeoutMillis = getTimeoutMillis()
        val endTime = startTime + timeoutMillis
        while (!idlingResource.isIdleNow && System.currentTimeMillis() < endTime) {
            uiController.loopMainThreadForAtLeast(50L)
        }
        return idlingResource.isIdleNow
    }

}
