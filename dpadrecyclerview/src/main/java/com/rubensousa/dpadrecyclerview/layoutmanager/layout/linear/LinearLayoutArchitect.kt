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
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import kotlin.math.abs
import kotlin.math.max

/**
 * Calculates the required amount for layout in both directions
 */
internal open class LinearLayoutArchitect(layoutInfo: LayoutInfo) : LayoutArchitect(layoutInfo) {

    override fun updateLayoutStateForPredictiveStart(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    ) {
        layoutRequest.apply {
            setStartDirection()
            setRecyclingEnabled(false)
            setCurrentPosition(anchorPosition)
            setCheckpoint(getLayoutStart())
            setFillSpace(layoutRequest.extraLayoutSpaceStart)
            updateCurrentPositionFromScrap()
        }
    }

    override fun updateLayoutStateForPredictiveEnd(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    ) {
        layoutRequest.apply {
            setEndDirection()
            setRecyclingEnabled(false)
            setCurrentPosition(anchorPosition)
            setCheckpoint(getLayoutEnd())
            setFillSpace(layoutRequest.extraLayoutSpaceEnd)
            updateCurrentPositionFromScrap()
        }
    }

    override fun updateForExtraLayoutEnd(layoutRequest: LayoutRequest, state: State) {
        layoutRequest.apply {
            setEndDirection()
            updateExtraLayoutSpace(layoutRequest, state)
            val view = layoutInfo.getChildClosestToEnd() ?: return
            setCheckpoint(layoutInfo.getDecoratedEnd(view))
            updateLayoutStateForExtraLayout(this, view)
        }
    }

    override fun updateForExtraLayoutStart(layoutRequest: LayoutRequest, state: State) {
        layoutRequest.apply {
            setStartDirection()
            updateExtraLayoutSpace(layoutRequest, state)
            val view = layoutInfo.getChildClosestToStart() ?: return
            setCheckpoint(layoutInfo.getDecoratedStart(view))
            updateLayoutStateForExtraLayout(this, view)
        }
    }

    override fun updateLayoutStateForScroll(
        layoutRequest: LayoutRequest,
        state: State,
        offset: Int
    ) {
        if (offset > 0) {
            layoutRequest.setEndDirection()
        } else {
            layoutRequest.setStartDirection()
        }

        updateExtraLayoutSpace(layoutRequest, state)

        // Enable recycling since we might add new views now
        layoutRequest.setRecyclingEnabled(true)

        val view = if (layoutRequest.isLayingOutEnd()) {
            layoutInfo.getChildClosestToEnd()
        } else {
            layoutInfo.getChildClosestToStart()
        }
        if (view == null) {
            return
        }
        // Start layout from the next position of the child closest to the edge
        layoutRequest.setCurrentPosition(
            layoutInfo.getLayoutPositionOf(view) + layoutRequest.direction.value
        )

        // We need to at least fill the next scroll target
        val requiredSpace = abs(offset)

        if (layoutRequest.isLayingOutEnd()) {
            layoutRequest.apply {
                setCheckpoint(getLayoutEnd())
                // The available scroll space starts from the checkpoint until the actual edge
                setAvailableScrollSpace(
                    max(0, checkpoint - layoutInfo.getEndAfterPadding())
                )
                // Remove availableScrollSpace since that space is already filled
                val fill = requiredSpace + layoutRequest.extraLayoutSpaceEnd - availableScrollSpace
                setFillSpace(max(0, fill))
            }
        } else {
            layoutRequest.apply {
                setCheckpoint(getLayoutStart())
                setAvailableScrollSpace(
                    max(0, layoutInfo.getStartAfterPadding() - checkpoint)
                )
                val fill =
                    requiredSpace + layoutRequest.extraLayoutSpaceStart - availableScrollSpace
                setFillSpace(max(0, fill))
            }
        }
    }

    open fun getLayoutStart(): Int {
        val firstView = layoutInfo.getChildClosestToStart() ?: return 0
        return layoutInfo.getDecoratedStart(firstView)
    }

    open fun getLayoutEnd(): Int {
        val lastView = layoutInfo.getChildClosestToEnd() ?: return 0
        return layoutInfo.getDecoratedEnd(lastView)
    }


}
