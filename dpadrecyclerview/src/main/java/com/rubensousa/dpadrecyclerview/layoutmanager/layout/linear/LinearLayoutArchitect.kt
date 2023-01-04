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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear

import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutState
import kotlin.math.abs
import kotlin.math.max

/**
 * Calculates the required amount for layout in both directions
 */
internal class LinearLayoutArchitect(layoutInfo: LayoutInfo) : LayoutArchitect(layoutInfo) {

    override fun updateLayoutStateForPredictiveStart(
        layoutState: LayoutState,
        anchorPosition: Int
    ) {
        layoutState.apply {
            setStartDirection()
            setRecyclingEnabled(false)
            setCurrentPosition(anchorPosition)
            setCheckpoint(layoutState.getStartOffset())
            setFillSpace(layoutState.extraLayoutSpaceStart)
            updateCurrentPositionFromScrap()
        }
    }

    override fun updateLayoutStateForPredictiveEnd(layoutState: LayoutState, anchorPosition: Int) {
        layoutState.apply {
            setEndDirection()
            setRecyclingEnabled(false)
            setCurrentPosition(anchorPosition)
            setCheckpoint(layoutState.getEndOffset())
            setFillSpace(layoutState.extraLayoutSpaceEnd)
            updateCurrentPositionFromScrap()
        }
    }

    override fun updateForExtraLayoutEnd(layoutState: LayoutState, recyclerViewState: State) {
        layoutState.apply {
            setEndDirection()
            updateExtraLayoutSpace(layoutState, recyclerViewState)
            val view = layoutInfo.getChildClosestToEnd() ?: return
            setCheckpoint(layoutInfo.getDecoratedEnd(view))
            updateLayoutStateForExtraLayout(this, view)
        }
    }

    override fun updateForExtraLayoutStart(layoutState: LayoutState, recyclerViewState: State) {
        layoutState.apply {
            setStartDirection()
            updateExtraLayoutSpace(layoutState, recyclerViewState)
            val view = layoutInfo.getChildClosestToStart() ?: return
            setCheckpoint(layoutInfo.getDecoratedStart(view))
            updateLayoutStateForExtraLayout(this, view)
        }
    }

    override fun updateLayoutStateForScroll(
        layoutState: LayoutState,
        recyclerViewState: State,
        offset: Int
    ) {
        if (offset > 0) {
            layoutState.setEndDirection()
        } else {
            layoutState.setStartDirection()
        }

        updateExtraLayoutSpace(layoutState, recyclerViewState)

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

}
