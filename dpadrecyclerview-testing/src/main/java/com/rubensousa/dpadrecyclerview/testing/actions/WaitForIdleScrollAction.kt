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

internal class WaitForIdleScrollAction(
    timeout: Long = 5,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
) : ViewAction {

    private val waiter = ActionWaiter(timeout, timeoutUnit)

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(RecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Waiting for idle scroll state"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView
        val idlingResource = ScrollingIdlingResource(recyclerView)
        uiController.loopMainThreadForAtLeast(300L)
        val isIdleNow = waiter.waitFor(idlingResource, uiController)
        if (!isIdleNow) {
            throw PerformException.Builder()
                .withActionDescription(description)
                .withCause(TimeoutException("Waited ${waiter.getTimeoutMillis()} milliseconds"))
                .withViewDescription(HumanReadables.describe(view))
                .build()
        }
    }

    class ScrollingIdlingResource(
        recyclerView: RecyclerView,
        private var callback: IdlingResource.ResourceCallback? = null
    ) : IdlingResource {

        private var isScrolling = recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE
        private val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
                if (!isScrolling) {
                    callback?.onTransitionToIdle()
                }
            }
        }

        init {
            recyclerView.addOnScrollListener(scrollListener)
        }

        override fun getName(): String = this::class.simpleName ?: ""

        override fun isIdleNow(): Boolean {
            return !isScrolling
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = callback
        }

    }

}
