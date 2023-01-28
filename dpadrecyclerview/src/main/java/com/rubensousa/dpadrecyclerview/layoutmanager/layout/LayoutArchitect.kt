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

import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

internal abstract class LayoutArchitect(protected val layoutInfo: LayoutInfo) {

    abstract fun updateLayoutStateForPredictiveStart(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    )

    abstract fun updateLayoutStateForPredictiveEnd(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    )

    fun updateExtraLayoutSpace(layoutRequest: LayoutRequest, state: RecyclerView.State) {
        if (setCustomExtraLayoutSpace(layoutRequest, state)) {
            // Skip our logic if user specified a custom strategy for extra layout space
            return
        }
        if (layoutRequest.isLayingOutEnd()) {
            layoutRequest.setExtraLayoutSpace(
                end = getDefaultExtraLayoutSpace(),
                start = 0
            )
        } else {
            layoutRequest.setExtraLayoutSpace(
                start = getDefaultExtraLayoutSpace(),
                end = 0
            )
        }
    }

    private fun setCustomExtraLayoutSpace(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State
    ): Boolean {
        return layoutInfo.getConfiguration().extraLayoutSpaceStrategy?.let { strategy ->
            var startSpace = strategy.calculateStartExtraLayoutSpace(state)
            var endSpace = strategy.calculateEndExtraLayoutSpace(state)

            // Ensure minimum extra layout space for target scrolling
            if (layoutRequest.isLayingOutEnd()) {
                endSpace = max(getDefaultExtraLayoutSpace(), endSpace)
            } else {
                startSpace = max(getDefaultExtraLayoutSpace(), startSpace)
            }
            layoutRequest.setExtraLayoutSpace(startSpace, endSpace)
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
