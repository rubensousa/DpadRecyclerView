package com.rubensousa.dpadrecyclerview.test.actions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.*
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class WaitForIdleScrollAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(RecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Waiting for idle scroll state"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView
        if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            return
        }
        val idlingResource = ScrollingIdlingResource(recyclerView)
        IdlingRegistry.getInstance().register(idlingResource)
        InstrumentationRegistry.getInstrumentation().waitForIdle {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
        uiController.loopMainThreadUntilIdle()
    }

    class ScrollingIdlingResource(
        recyclerView: RecyclerView,
        private var callback: IdlingResource.ResourceCallback? = null
    ) : IdlingResource {

        private var isScrolling = recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE

        init {
            recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
                    if (!isScrolling) {
                        callback?.onTransitionToIdle()
                    }
                }
            })
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