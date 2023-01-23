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
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.PerformException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.util.HumanReadables
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

internal class WaitForAdapterUpdateAction(
    private val updates: Int,
    private val failOnTimeout: Boolean,
    timeout: Long = 2,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
) : ViewAction {

    private val waiter = ActionWaiter(timeout, timeoutUnit)

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(RecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Waiting for adapter change"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView
        val idlingResource = AdapterUpdateIdlingResource(updates, recyclerView)
        val isIdleNow = waiter.waitFor(idlingResource, uiController)
        if (!isIdleNow && failOnTimeout) {
            throw PerformException.Builder()
                .withActionDescription(description)
                .withCause(TimeoutException("Waited ${waiter.getTimeoutMillis()} milliseconds"))
                .withViewDescription(HumanReadables.describe(view))
                .build()
        }
        if (isIdleNow) {
            uiController.loopMainThreadForAtLeast(300)
        }
    }

    class AdapterUpdateIdlingResource(updates: Int, recyclerView: RecyclerView) : IdlingResource {

        private var remainingUpdates = updates
        private var callback: IdlingResource.ResourceCallback? = null

        init {
            recyclerView.adapter?.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        refreshRemainingUpdates()
                    }

                    override fun onItemRangeChanged(
                        positionStart: Int,
                        itemCount: Int,
                        payload: Any?
                    ) {
                        refreshRemainingUpdates()
                    }

                    override fun onChanged() {
                        refreshRemainingUpdates()
                    }

                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        refreshRemainingUpdates()
                    }

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        refreshRemainingUpdates()
                    }

                    override fun onItemRangeMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        itemCount: Int
                    ) {
                        refreshRemainingUpdates()
                    }
                })
        }

        override fun getName(): String = this::class.simpleName ?: ""

        override fun isIdleNow(): Boolean {
            return remainingUpdates == 0
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = callback
        }

        private fun refreshRemainingUpdates() {
            if (remainingUpdates > 0) {
                remainingUpdates--
            }
            if (remainingUpdates == 0) {
                callback?.onTransitionToIdle()
            }
        }

    }
}