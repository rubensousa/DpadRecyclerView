/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.layoutmanager.layout

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

internal abstract class LayoutArchitect(protected val layoutInfo: LayoutInfo) {

    private val extraLayoutSpace = IntArray(2)

    abstract fun updateLayoutStateForPredictiveStart(layoutState: LayoutState, anchorPosition: Int)
    abstract fun updateLayoutStateForPredictiveEnd(layoutState: LayoutState, anchorPosition: Int)
    abstract fun updateForExtraLayoutEnd(
        layoutState: LayoutState,
        recyclerViewState: RecyclerView.State
    )
    abstract fun updateForExtraLayoutStart(
        layoutState: LayoutState,
        recyclerViewState: RecyclerView.State
    )
    abstract fun updateLayoutStateForScroll(
        layoutState: LayoutState,
        recyclerViewState: RecyclerView.State,
        offset: Int
    )

    fun updateForStartPreLayout(
        layoutState: LayoutState,
        extraLayoutSpace: Int,
        minOldPosition: Int,
        anchorView: View
    ) {
        layoutState.apply {
            setStartDirection()
            setExtraLayoutSpaceStart(extraLayoutSpace)
            setRecyclingEnabled(false)
            setCurrentPosition(minOldPosition + layoutState.itemDirection.value)
            setCheckpoint(layoutInfo.getDecoratedStart(anchorView))
            setAvailableScrollSpace(calculateAvailableScrollSpace(extraLayoutSpace, false))
            setFillSpace(calculateExtraFillSpace(this))
        }
    }

    fun updateForEndPreLayout(
        layoutState: LayoutState,
        extraLayoutSpace: Int,
        maxOldPosition: Int,
        anchorView: View
    ) {
        layoutState.apply {
            setEndDirection()
            setExtraLayoutSpaceEnd(extraLayoutSpace)
            setRecyclingEnabled(false)
            setCurrentPosition(maxOldPosition + layoutState.itemDirection.value)
            setCheckpoint(layoutInfo.getDecoratedEnd(anchorView))
            setAvailableScrollSpace(calculateAvailableScrollSpace(extraLayoutSpace, true))
            setFillSpace(calculateExtraFillSpace(this))
        }
    }

    fun updateLayoutStateAfterPivot(layoutState: LayoutState, pivotPosition: Int) {
        layoutState.apply {
            setEndDirection()
            setCurrentPosition(pivotPosition + 1)
            setCheckpoint(layoutState.getEndOffset())
            setAvailableScrollSpace(0)
            val endFillSpace = max(
                0, layoutInfo.getEndAfterPadding() - layoutState.getEndOffset()
            )
            setFillSpace(layoutState.extraLayoutSpaceEnd + endFillSpace)
        }
    }

    fun updateLayoutStateBeforePivot(layoutState: LayoutState, pivotPosition: Int) {
        layoutState.apply {
            setStartDirection()
            setCurrentPosition(pivotPosition - 1)
            setCheckpoint(layoutState.getStartOffset())
            setAvailableScrollSpace(0)
            val startFillSpace = max(
                0, layoutState.getStartOffset() - layoutInfo.getStartAfterPadding()
            )
            setFillSpace(layoutState.extraLayoutSpaceStart + startFillSpace)
        }
    }

    protected fun updateLayoutStateForExtraLayout(layoutState: LayoutState, anchorView: View) {
        layoutState.apply {
            setRecyclingEnabled(false)
            setCurrentPosition(layoutInfo.getLayoutPositionOf(anchorView) + direction.value)
            setAvailableScrollSpace(calculateAvailableScrollSpace(checkpoint, isLayingOutEnd()))
            setFillSpace(calculateExtraFillSpace(this))
        }
    }

    protected fun calculateAvailableScrollSpace(
        offset: Int,
        towardsEnd: Boolean
    ): Int {
        return if (towardsEnd) {
            max(0, offset - layoutInfo.getEndAfterPadding())
        } else {
            max(0, layoutInfo.getStartAfterPadding() - offset)
        }
    }

    private fun calculateExtraFillSpace(layoutState: LayoutState): Int {
        return if (layoutState.isLayingOutEnd()) {
            max(0, layoutState.extraLayoutSpaceEnd - layoutState.availableScrollSpace)
        } else {
            max(0, layoutState.extraLayoutSpaceStart - layoutState.availableScrollSpace)
        }
    }

    protected fun updateExtraLayoutSpace(layoutState: LayoutState, state: RecyclerView.State) {
        if (setCustomExtraLayoutSpace(layoutState, state)) {
            // Skip our logic if user specified a custom strategy for extra layout space
            return
        }
        if (layoutState.isLayingOutEnd()) {
            layoutState.setExtraLayoutSpaceEnd(getDefaultExtraLayoutSpace())
            layoutState.setExtraLayoutSpaceStart(0)
        } else {
            layoutState.setExtraLayoutSpaceStart(getDefaultExtraLayoutSpace())
            layoutState.setExtraLayoutSpaceEnd(0)
        }
    }

    private fun setCustomExtraLayoutSpace(
        layoutState: LayoutState,
        state: RecyclerView.State
    ): Boolean {
        return layoutInfo.getConfiguration().extraLayoutSpaceStrategy?.let { strategy ->
            strategy.calculateExtraLayoutSpace(state, extraLayoutSpace)
            layoutState.setExtraLayoutSpaceStart(extraLayoutSpace[0])
            layoutState.setExtraLayoutSpaceEnd(extraLayoutSpace[1])
            true
        } ?: false
    }

    /**
     * If we're scrolling to a specific target position,
     * we should layout extra items before we reach the target to make sure
     * the scroll alignment works correctly.
     */
    private fun getDefaultExtraLayoutSpace(): Int {
        return if (layoutInfo.isScrollingToTarget) {
            layoutInfo.getTotalSpace()
        } else {
            0
        }
    }

}
