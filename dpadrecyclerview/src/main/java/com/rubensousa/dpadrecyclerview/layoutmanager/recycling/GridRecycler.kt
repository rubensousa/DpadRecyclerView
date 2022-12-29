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

package com.rubensousa.dpadrecyclerview.layoutmanager.recycling

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutState

internal class GridRecycler(
    layoutManager: RecyclerView.LayoutManager,
    layoutInfo: LayoutInfo,
    configuration: LayoutConfiguration
) : ViewRecycler(layoutManager, layoutInfo, configuration) {

    override fun updateLayoutState(
        recycled: View,
        position: Int,
        size: Int,
        layoutState: LayoutState
    ) {
        // Whenever a view is recycled,
        // just update the offsets if the view is using the spans at the edges
        if (layoutState.isLayingOutEnd()) {
            if (layoutInfo.getStartColumnIndex(position) == 0) {
                layoutState.increaseStartOffset(size)
            }
        } else {
            if (layoutInfo.isPositionAtLastColumn(position)) {
                layoutState.decreaseEndOffset(size)
            }
        }
    }

}
