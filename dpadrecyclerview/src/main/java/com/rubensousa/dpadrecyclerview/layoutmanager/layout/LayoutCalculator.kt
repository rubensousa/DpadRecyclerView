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
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import kotlin.math.max

/**
 * Calculates the required amount for layout in both directions
 */
internal class LayoutCalculator(private val layoutInfo: LayoutInfo) {

    fun init(layoutState: LayoutState, state: State, configuration: LayoutConfiguration) {
        layoutState.setPreLayout(state.isPreLayout)
        layoutState.setReverseLayout(configuration.reverseLayout)
        updateExtraLayoutSpace(layoutState, state)
    }

    private fun updateExtraLayoutSpace(layoutState: LayoutState, state: State) {
        val extraScrollSpace = getDefaultExtraLayoutSpace(state)
        // TODO Use default of not creating extra space in opposite direction
        layoutState.setExtraLayoutSpace(extraScrollSpace)
    }

    fun updateLayoutStateForPredictiveStart(layoutState: LayoutState, anchor: View) {
        layoutState.apply {
            setStartDirection()
            setRecyclingEnabled(false)
            setCheckpoint(layoutState.getStartOffset())
            setCurrentPosition(layoutInfo.getLayoutPositionOf(anchor) + direction.value)
            setFillSpace(layoutState.extraLayoutSpaceStart)
            setAvailableScrollSpace(0)
        }
    }

    fun updateLayoutStateForPredictiveEnd(layoutState: LayoutState, anchor: View) {
        layoutState.apply {
            setEndDirection()
            setRecyclingEnabled(false)
            setCheckpoint(layoutState.getEndOffset())
            setCurrentPosition(layoutInfo.getLayoutPositionOf(anchor) + direction.value)
            setFillSpace(layoutState.extraLayoutSpaceEnd)
            setAvailableScrollSpace(0)
        }
    }

    fun updatePreLayoutStateBeforeStart(layoutState: LayoutState, view: View) {
        layoutState.apply {
            updateCurrentPositionFromScrap()
            setStartDirection()
            setRecyclingEnabled(false)
            setCheckpoint(layoutInfo.getDecoratedStart(view))
            setCurrentPosition(layoutInfo.getLayoutPositionOf(view) + direction.value)
            setFillSpace(layoutState.extraLayoutSpaceStart)
            setAvailableScrollSpace(0)
        }
    }

    fun updatePreLayoutStateAfterEnd(layoutState: LayoutState, view: View) {
        layoutState.apply {
            updateCurrentPositionFromScrap()
            setEndDirection()
            setRecyclingEnabled(false)
            setCheckpoint(layoutInfo.getDecoratedEnd(view))
            setCurrentPosition(layoutInfo.getLayoutPositionOf(view) + direction.value)
            setFillSpace(layoutState.extraLayoutSpaceEnd)
            setAvailableScrollSpace(0)
        }
    }

    fun updateLayoutStateForScroll(layoutState: LayoutState, state: State, offset: Int) {
        updateExtraLayoutSpace(layoutState, state)
        // Enable recycling since we might add new views now
        layoutState.setRecyclingEnabled(true)
        if (offset > 0) {
            layoutState.setEndDirection()
        } else {
            layoutState.setStartDirection()
        }

        val view = if (layoutState.isLayingOutEnd()) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
        if (view == null) {
            return
        }
        layoutState.setCurrentPosition(
            layoutInfo.getLayoutPositionOf(view) + layoutState.direction.value
        )
        if (layoutState.isLayingOutEnd()) {
            layoutState.apply {
                setCheckpoint(layoutInfo.getDecoratedEnd(view))
                setAvailableScrollSpace(
                    max(0, checkpoint - layoutInfo.getEndAfterPadding())
                )
                val endFillSpace = max(
                    0, layoutInfo.orientationHelper.endAfterPadding - layoutState.getEndOffset()
                )
                setFillSpace(layoutState.extraLayoutSpaceEnd + endFillSpace)
            }
        } else {
            layoutState.apply {
                setCheckpoint(layoutInfo.getDecoratedStart(view))
                setAvailableScrollSpace(
                    max(0, layoutInfo.getStartAfterPadding() - checkpoint)
                )
                val startFillSpace = max(
                    0, layoutState.getStartOffset() - layoutInfo.getStartAfterPadding()
                )
                setFillSpace(layoutState.extraLayoutSpaceStart + startFillSpace)
            }
        }
    }

    /**
     * *: to be filled
     *
     * Pivot *****
     */
    fun updateLayoutStateAfterPivot(layoutState: LayoutState, pivotInfo: PivotInfo) {
        layoutState.apply {
            setEndDirection()
            setCurrentPosition(pivotInfo.position + 1)
            setCheckpoint(layoutState.getEndOffset())
            setAvailableScrollSpace(0)
            val endFillSpace = max(
                0, layoutInfo.getEndAfterPadding() - layoutState.getEndOffset()
            )
            setFillSpace(layoutState.extraLayoutSpaceEnd + endFillSpace)
        }
    }

    /**
     * *: to be filled
     *
     * ****** Pivot
     */
    fun updateLayoutStateBeforePivot(layoutState: LayoutState, pivotInfo: PivotInfo) {
        layoutState.apply {
            setStartDirection()
            setCurrentPosition(pivotInfo.position - 1)
            setCheckpoint(layoutState.getStartOffset())
            setAvailableScrollSpace(0)
            val startFillSpace = max(
                0, layoutState.getStartOffset() - layoutInfo.getStartAfterPadding()
            )
            setFillSpace(layoutState.extraLayoutSpaceStart + startFillSpace)
        }
    }

    /**
     * Layout an extra page by default if we're scrolling
     */
    private fun getDefaultExtraLayoutSpace(state: State): Int {
        return if (layoutInfo.isScrolling || state.hasTargetScrollPosition()) {
            layoutInfo.getTotalSpace()
        } else {
            layoutInfo.getTotalSpace()
        }
    }

}
