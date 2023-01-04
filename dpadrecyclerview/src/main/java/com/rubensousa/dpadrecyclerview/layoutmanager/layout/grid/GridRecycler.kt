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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewRecycler

internal class GridRecycler(
    layoutManager: RecyclerView.LayoutManager,
    layoutInfo: LayoutInfo
) : ViewRecycler(layoutManager, layoutInfo) {

    override fun updateLayoutState(
        recycled: View,
        position: Int,
        size: Int,
        layoutRequest: LayoutRequest
    ) {
        // Whenever a view is recycled,
        // just update the offsets if the view is using the spans at the edges
        if (layoutRequest.isLayingOutEnd()) {
            if (layoutInfo.getStartColumnIndex(position) == 0) {
                layoutRequest.increaseWindowStart(size)
            }
        } else {
            if (layoutInfo.isPositionAtLastColumn(position)) {
                layoutRequest.decreaseWindowEnd(size)
            }
        }
    }

}
