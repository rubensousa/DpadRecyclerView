package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy

class LimitedScrollBehavior {

    private val linearInterpolator = LinearInterpolator()

    fun setup(
        dpadRecyclerView: DpadRecyclerView,
        extraLayoutSpaceStart: () -> Int = { 0 },
        extraLayoutSpaceEnd: () -> Int = { 0 },
    ) {
        dpadRecyclerView.setFocusSearchDebounceMs(200)
        dpadRecyclerView.setSmoothScrollMaxPendingMoves(0)
        dpadRecyclerView.setExtraLayoutSpaceStrategy(object : ExtraLayoutSpaceStrategy {
            override fun calculateStartExtraLayoutSpace(state: RecyclerView.State): Int {
                return extraLayoutSpaceStart()
            }

            override fun calculateEndExtraLayoutSpace(state: RecyclerView.State): Int {
                return extraLayoutSpaceEnd()
            }
        })
        dpadRecyclerView.setSmoothScrollBehavior(
            object : DpadRecyclerView.SmoothScrollByBehavior {
                override fun configSmoothScrollByDuration(dx: Int, dy: Int): Int {
                    return 200
                }

                override fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator? {
                    return linearInterpolator
                }
            })
    }

}
