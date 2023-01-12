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

    abstract fun updateLayoutStateForPredictiveStart(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    )

    abstract fun updateLayoutStateForPredictiveEnd(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    )

    abstract fun updateForExtraLayoutEnd(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State
    )

    abstract fun updateForExtraLayoutStart(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State
    )

    abstract fun updateLayoutStateForScroll(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State,
        offset: Int
    )

    fun updateForStartPreLayout(
        layoutRequest: LayoutRequest,
        extraLayoutSpace: Int,
        minOldPosition: Int,
        anchorView: View
    ) {
        layoutRequest.apply {
            setStartDirection()
            setExtraLayoutSpaceStart(extraLayoutSpace)
            setRecyclingEnabled(false)
            setCurrentPosition(minOldPosition + layoutRequest.itemDirection.value)
            setCheckpoint(layoutInfo.getDecoratedStart(anchorView))
            setAvailableScrollSpace(calculateAvailableScrollSpace(extraLayoutSpace, false))
            setFillSpace(calculateExtraFillSpace(this))
        }
    }

    fun updateForEndPreLayout(
        layoutRequest: LayoutRequest,
        extraLayoutSpace: Int,
        maxOldPosition: Int,
        anchorView: View
    ) {
        layoutRequest.apply {
            setEndDirection()
            setExtraLayoutSpaceEnd(extraLayoutSpace)
            setRecyclingEnabled(false)
            setCurrentPosition(maxOldPosition + layoutRequest.itemDirection.value)
            setCheckpoint(layoutInfo.getDecoratedEnd(anchorView))
            setAvailableScrollSpace(calculateAvailableScrollSpace(extraLayoutSpace, true))
            setFillSpace(calculateExtraFillSpace(this))
        }
    }

    open fun updateLayoutStateBeforePivot(
        layoutRequest: LayoutRequest,
        pivotView: View,
        pivotPosition: Int
    ) {
    }

    open fun updateLayoutStateAfterPivot(
        layoutRequest: LayoutRequest,
        pivotView: View,
        pivotPosition: Int
    ) {

    }

    protected fun updateLayoutStateForExtraLayout(layoutRequest: LayoutRequest, anchorView: View) {
        layoutRequest.apply {
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

    private fun calculateExtraFillSpace(layoutRequest: LayoutRequest): Int {
        return if (layoutRequest.isLayingOutEnd()) {
            max(0, layoutRequest.extraLayoutSpaceEnd - layoutRequest.availableScrollSpace)
        } else {
            max(0, layoutRequest.extraLayoutSpaceStart - layoutRequest.availableScrollSpace)
        }
    }

    protected fun updateExtraLayoutSpace(layoutRequest: LayoutRequest, state: RecyclerView.State) {
        if (setCustomExtraLayoutSpace(layoutRequest, state)) {
            // Skip our logic if user specified a custom strategy for extra layout space
            return
        }
        if (layoutRequest.isLayingOutEnd()) {
            layoutRequest.setExtraLayoutSpaceEnd(getDefaultExtraLayoutSpace())
            layoutRequest.setExtraLayoutSpaceStart(0)
        } else {
            layoutRequest.setExtraLayoutSpaceStart(getDefaultExtraLayoutSpace())
            layoutRequest.setExtraLayoutSpaceEnd(0)
        }
    }

    private fun setCustomExtraLayoutSpace(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State
    ): Boolean {
        return layoutInfo.getConfiguration().extraLayoutSpaceStrategy?.let { strategy ->
            strategy.calculateExtraLayoutSpace(state, extraLayoutSpace)
            layoutRequest.setExtraLayoutSpaceStart(extraLayoutSpace[0])
            layoutRequest.setExtraLayoutSpaceEnd(extraLayoutSpace[1])
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
