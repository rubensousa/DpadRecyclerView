package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy

class LimitedScrollBehavior {

    private val linearInterpolator = LinearInterpolator()
    private val quinticInterpolator = Interpolator { t ->
        var value = t
        value -= 1.0f
        value * value * value * value * value + 1.0f
    }

    fun setup(
        recyclerView: DpadRecyclerView,
        extraLayoutSpaceStart: () -> Int,
        extraLayoutSpaceEnd: () -> Int,
        maxPendingAlignments: Int = 2,
        duration: Int? = null
    ) {
        recyclerView.setSmoothScrollMaxPendingAlignments(maxPendingAlignments)
        recyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
            override fun calculateStartExtraLayoutSpace(state: RecyclerView.State): Int {
                return extraLayoutSpaceStart()
            }
            override fun calculateEndExtraLayoutSpace(state: RecyclerView.State): Int {
                return extraLayoutSpaceEnd()
            }
        })
        recyclerView.setSmoothScrollBehavior(
            object : DpadRecyclerView.SmoothScrollByBehavior {
                override fun configSmoothScrollByDuration(dx: Int, dy: Int): Int {
                    return duration ?: RecyclerView.UNDEFINED_DURATION
                }

                override fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator {
                    return if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                        quinticInterpolator
                    } else {
                        linearInterpolator
                    }
                }
            })
    }

}
