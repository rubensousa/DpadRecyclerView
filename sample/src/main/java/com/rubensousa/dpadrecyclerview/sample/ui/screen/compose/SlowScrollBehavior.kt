package com.rubensousa.dpadrecyclerview.sample.ui.screen.compose

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy

/**
 * Throttles scroll events specifically for Compose based screens that use [DpadRecyclerView].
 * This will avoid unnecessary layout requests during scroll actions
 */
fun DpadRecyclerView.setSlowScrollBehavior(
    nonIdleScrollDuration: Int = 300
) {
    val linearInterpolator = LinearInterpolator()
    setSmoothScrollMaxPendingAlignments(3)
    setSmoothScrollMaxPendingMoves(0)
    setLayoutWhileScrollingEnabled(false)
    setSmoothScrollBehavior(
        object : DpadRecyclerView.SmoothScrollByBehavior {
            override fun configSmoothScrollByDuration(dx: Int, dy: Int): Int {
                return if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    RecyclerView.UNDEFINED_DURATION
                } else {
                    nonIdleScrollDuration
                }
            }

            override fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator? {
                return if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    null
                } else {
                    linearInterpolator
                }
            }
        })
}
