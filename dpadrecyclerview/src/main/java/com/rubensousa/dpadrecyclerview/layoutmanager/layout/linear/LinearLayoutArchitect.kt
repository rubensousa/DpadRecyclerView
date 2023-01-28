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

import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest

/**
 * Calculates the required amount for layout in both directions
 */
internal open class LinearLayoutArchitect(layoutInfo: LayoutInfo) : LayoutArchitect(layoutInfo) {

    override fun updateLayoutStateForPredictiveStart(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    ) {
        layoutRequest.prepend(anchorPosition) {
            setRecyclingEnabled(false)
            setCheckpoint(getLayoutStart())
            updateCurrentPositionFromScrap()
            setFillSpace(layoutRequest.extraLayoutSpaceStart)
        }
    }

    override fun updateLayoutStateForPredictiveEnd(
        layoutRequest: LayoutRequest,
        anchorPosition: Int
    ) {
        layoutRequest.append(anchorPosition) {
            setRecyclingEnabled(false)
            setCheckpoint(getLayoutEnd())
            updateCurrentPositionFromScrap()
            setFillSpace(layoutRequest.extraLayoutSpaceEnd)
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
