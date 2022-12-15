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

import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import kotlin.math.abs
import kotlin.math.max

/**
 * Calculates the required amount for layout in both directions
 */
internal class LayoutCalculator(private val layoutInfo: LayoutInfo) {

    fun init(layoutState: LayoutState, state: State, configuration: LayoutConfiguration) {
        layoutState.setPreLayout(state.isPreLayout)
        layoutState.setReverseLayout(configuration.reverseLayout)
        layoutState.setRecyclingEnabled(false)
    }

    private fun updateExtraLayoutSpace(layoutState: LayoutState, state: State) {
        val extraScrollSpace = getDefaultExtraLayoutSpace(state)
        if (layoutState.isLayingOutEnd()) {
            layoutState.setExtraLayoutSpaceEnd(extraScrollSpace / 2)
            layoutState.setExtraLayoutSpaceStart(0)
        } else {
            layoutState.setExtraLayoutSpaceStart(extraScrollSpace / 2)
            layoutState.setExtraLayoutSpaceEnd(0)
        }
    }

    fun updateLayoutStateForPredictiveStart(layoutState: LayoutState, anchorPosition: Int) {
        layoutState.apply {
            setStartDirection()
            setRecyclingEnabled(false)
            setCurrentPosition(anchorPosition)
            setCheckpoint(layoutState.getStartOffset())
            setFillSpace(layoutState.extraLayoutSpaceStart)
            updateCurrentPositionFromScrap()
        }
    }

    fun updateLayoutStateForPredictiveEnd(layoutState: LayoutState, anchorPosition: Int) {
        layoutState.apply {
            setEndDirection()
            setRecyclingEnabled(false)
            setCurrentPosition(anchorPosition)
            setCheckpoint(layoutState.getEndOffset())
            setFillSpace(layoutState.extraLayoutSpaceEnd)
            updateCurrentPositionFromScrap()
        }
    }

    fun updateLayoutStateForScroll(layoutState: LayoutState, state: State, offset: Int) {
        if (offset > 0) {
            layoutState.setEndDirection()
        } else {
            layoutState.setStartDirection()
        }

        updateExtraLayoutSpace(layoutState, state)

        // Enable recycling since we might add new views now
        layoutState.setRecyclingEnabled(true)

        val view = if (layoutState.isLayingOutEnd()) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
        if (view == null) {
            return
        }
        // Start layout from the next position of the child closest to the edge
        layoutState.setCurrentPosition(
            layoutInfo.getLayoutPositionOf(view) + layoutState.direction.value
        )

        // We need to at least fill the next scroll target
        val requiredSpace = abs(offset)

        if (layoutState.isLayingOutEnd()) {
            layoutState.apply {
                setCheckpoint(layoutInfo.getDecoratedEnd(view))
                // The available scroll space starts from the checkpoint until the actual edge
                setAvailableScrollSpace(
                    max(0, checkpoint - layoutInfo.getEndAfterPadding())
                )
                // Remove availableScrollSpace since that space is already filled
                val fill = requiredSpace + layoutState.extraLayoutSpaceEnd - availableScrollSpace
                setFillSpace(max(0, fill))
            }
        } else {
            layoutState.apply {
                setCheckpoint(layoutInfo.getDecoratedStart(view))
                setAvailableScrollSpace(
                    max(0, layoutInfo.getStartAfterPadding() - checkpoint)
                )
                val fill = requiredSpace + layoutState.extraLayoutSpaceStart - availableScrollSpace
                setFillSpace(max(0, fill))
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
            0
        }
    }

}
