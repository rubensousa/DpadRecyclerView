package com.rubensousa.dpadrecyclerview.test.actions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.*
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class WaitForAdapterUpdateAction : ViewAction {

    override fun getConstraints(): Matcher<View> {
        return Matchers.isA(RecyclerView::class.java)
    }

    override fun getDescription(): String {
        return "Waiting for adapter change"
    }

    override fun perform(uiController: UiController, view: View) {
        val recyclerView = view as RecyclerView
        val idlingResource = AdapterUpdateIdlingResource(recyclerView)
        IdlingRegistry.getInstance().register(idlingResource)
        InstrumentationRegistry.getInstrumentation().waitForIdle {
            IdlingRegistry.getInstance().unregister(idlingResource)
        }
        uiController.loopMainThreadUntilIdle()
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