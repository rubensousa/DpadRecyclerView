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
    timeout: Long = 5,
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
        val idlingResource = AdapterUpdateIdlingResource(recyclerView)
        val isIdleNow = waiter.waitFor(idlingResource, uiController)
        if (!isIdleNow) {
            throw PerformException.Builder()
                .withActionDescription(description)
                .withCause(TimeoutException("Waited ${waiter.getTimeoutMillis()} milliseconds"))
                .withViewDescription(HumanReadables.describe(view))
                .build()
        }
    }

    class AdapterUpdateIdlingResource(
        recyclerView: RecyclerView,
        private var callback: IdlingResource.ResourceCallback? = null
    ) : IdlingResource {

        private var isUpdated = false

        init {
            recyclerView.adapter?.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                        setIdle()
                    }

                    override fun onItemRangeChanged(
                        positionStart: Int,
                        itemCount: Int,
                        payload: Any?
                    ) {
                        setIdle()
                    }

                    override fun onChanged() {
                        setIdle()
                    }

                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        setIdle()
                    }

                    override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                        setIdle()
                    }

                    override fun onItemRangeMoved(
                        fromPosition: Int,
                        toPosition: Int,
                        itemCount: Int
                    ) {
                        setIdle()
                    }
                })
        }

        override fun getName(): String = this::class.simpleName ?: ""

        override fun isIdleNow(): Boolean {
            return isUpdated
        }

        override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
            this.callback = callback
        }

        private fun setIdle() {
            isUpdated = true
            callback?.onTransitionToIdle()
        }

    }
}