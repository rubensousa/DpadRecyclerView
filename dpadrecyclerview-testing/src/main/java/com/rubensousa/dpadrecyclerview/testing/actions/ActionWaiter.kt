package com.rubensousa.dpadrecyclerview.testing.actions

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.UiController
import java.util.concurrent.TimeUnit

class ActionWaiter(private val timeout: Long, private val timeoutUnit: TimeUnit) {

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
