package com.rubensousa.dpadrecyclerview

import android.view.KeyEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * A helper class that allows scrolling a [DpadRecyclerView] based on specific scroll distances,
 * ignoring the default alignment behavior.
 *
 * A typical use case for this class is a terms & conditions page,
 * where a large amount of text is displayed, which the user isn't expected to interact with
 */
class DpadScroller(
    private val calculator: ScrollDistanceCalculator = DefaultScrollDistanceCalculator(),
) {

    private var recyclerView: DpadRecyclerView? = null
    private val keyListener = KeyListener()
    private var smoothScrollEnabled = true

    /**
     * Attaches this [DpadScroller] to a new [DpadRecyclerView] to start observing key events.
     * If you no longer need this behavior, call [detach]
     *
     * @param recyclerView The RecyclerView that will be scrolled discretely
     */
    fun attach(recyclerView: DpadRecyclerView) {
        detach()
        this.recyclerView = recyclerView
        recyclerView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        recyclerView.setOnKeyInterceptListener(keyListener)
    }

    /**
     * Stops observing key events to scroll the current attached [DpadRecyclerView], if any exists
     */
    fun detach() {
        recyclerView?.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        recyclerView?.setOnKeyInterceptListener(null)
        recyclerView = null
    }

    /**
     * Enables or disables smooth scrolling on key events
     */
    fun setSmoothScrollEnabled(enabled: Boolean) {
        smoothScrollEnabled = enabled
    }

    private inner class KeyListener : DpadRecyclerView.OnKeyInterceptListener {

        override fun onInterceptKeyEvent(event: KeyEvent): Boolean {
            val currentRecyclerView = recyclerView ?: return false
            when (event.action) {
                KeyEvent.ACTION_DOWN -> {
                    return if (currentRecyclerView.getOrientation() == RecyclerView.VERTICAL) {
                        scrollVertically(currentRecyclerView, event)
                    } else {
                        scrollHorizontally(currentRecyclerView, event)
                    }
                }
            }
            return false
        }

        private fun scrollVertically(recyclerView: DpadRecyclerView, event: KeyEvent): Boolean {
            val scrollDistance = calculator.calculateScrollDistance(recyclerView, event)
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_DOWN -> {
                    if (smoothScrollEnabled) {
                        recyclerView.smoothScrollBy(0, scrollDistance)
                    } else {
                        recyclerView.scrollBy(0, scrollDistance)
                    }
                    return true
                }

                KeyEvent.KEYCODE_DPAD_UP -> {
                    if (smoothScrollEnabled) {
                        recyclerView.smoothScrollBy(0, -scrollDistance)
                    } else {
                        recyclerView.scrollBy(0, -scrollDistance)
                    }
                    return true
                }
            }
            return false
        }

        private fun scrollHorizontally(recyclerView: DpadRecyclerView, event: KeyEvent): Boolean {
            val scrollDistance = calculator.calculateScrollDistance(recyclerView, event)
            when (event.keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    if (smoothScrollEnabled) {
                        recyclerView.smoothScrollBy(0, scrollDistance)
                    } else {
                        recyclerView.scrollBy(0, scrollDistance)
                    }
                    return true
                }

                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    if (smoothScrollEnabled) {
                        recyclerView.smoothScrollBy(0, -scrollDistance)
                    } else {
                        recyclerView.scrollBy(0, -scrollDistance)
                    }
                    return true
                }
            }
            return false
        }

    }


    interface ScrollDistanceCalculator {
        /**
         * @return the number of pixels we should scroll for this [event]
         */
        fun calculateScrollDistance(recyclerView: DpadRecyclerView, event: KeyEvent): Int
    }

    private class DefaultScrollDistanceCalculator : ScrollDistanceCalculator {
        override fun calculateScrollDistance(
            recyclerView: DpadRecyclerView,
            event: KeyEvent
        ): Int {
            return if (recyclerView.getOrientation() == RecyclerView.VERTICAL) {
                recyclerView.height / 4
            } else {
                recyclerView.width / 4
            }
        }
    }

}
