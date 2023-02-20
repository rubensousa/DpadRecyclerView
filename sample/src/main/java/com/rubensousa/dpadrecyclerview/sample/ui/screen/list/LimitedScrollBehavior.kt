package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.view.animation.Interpolator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy

class LimitedScrollBehavior {

    fun setup(
        recyclerView: DpadRecyclerView,
        extraLayoutSpaceStart: () -> Int = { 0 },
        extraLayoutSpaceEnd: () -> Int = { 0 },
        maxPendingAlignments: Int = 2
    ) {
        recyclerView.setSmoothScrollMaxPendingAlignments(maxPendingAlignments)
        recyclerView.setSmoothScrollMaxPendingMoves(0)
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
                    return if (recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                        RecyclerView.UNDEFINED_DURATION
                    } else {
                        1000
                    }
                }

                override fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator? {
                    return null
                }
            })
    }

}
