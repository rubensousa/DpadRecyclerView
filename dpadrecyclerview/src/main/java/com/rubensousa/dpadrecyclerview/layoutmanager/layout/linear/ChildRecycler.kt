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

import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class ChildRecycler(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration
) {

    fun recycleByLayoutState(recycler: Recycler, layoutState: LinearLayoutState) {
        if (!layoutState.recycle || layoutState.isInfinite) {
            return
        }
        if (layoutState.isLayingOutStart()) {
            recycleFromEnd(recycler, layoutState.availableScrollSpace, layoutState.extraLayoutSpaceStart)
        } else {
            recycleFromStart(recycler, layoutState.availableScrollSpace, layoutState.extraLayoutSpaceStart)
        }
    }

    fun recycleFromStart(recycler: Recycler, availableScrollSpace: Int, extraLayoutSpace: Int) {
        if (availableScrollSpace < 0) {
            return
        }
        val limit = availableScrollSpace - extraLayoutSpace
        val childCount: Int = layoutManager.childCount
        if (configuration.reverseLayout) {
            for (i in childCount - 1 downTo 0) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.orientationHelper.getDecoratedEnd(child) > limit
                    || layoutInfo.orientationHelper.getTransformedEndWithDecoration(child) > limit
                ) {
                    recycle(recycler, childCount - 1, i)
                    return
                }
            }
        } else {
            for (i in 0 until childCount) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.orientationHelper.getDecoratedEnd(child) > limit
                    || layoutInfo.orientationHelper.getTransformedEndWithDecoration(child) > limit
                ) {
                    recycle(recycler, 0, i)
                    return
                }
            }
        }
    }

    fun recycleFromEnd(
        recycler: Recycler,
        availableScrollSpace: Int,
        extraLayoutSpace: Int
    ) {
        val childCount = layoutManager.childCount
        if (availableScrollSpace < 0) {
            return
        }
        val limit = layoutInfo.orientationHelper.end - availableScrollSpace + extraLayoutSpace
        if (configuration.reverseLayout) {
            for (i in 0 until childCount) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.orientationHelper.getDecoratedStart(child) < limit
                    || layoutInfo.orientationHelper.getTransformedStartWithDecoration(child) < limit
                ) {
                    recycle(recycler, 0, i)
                    return
                }
            }
        } else {
            for (i in childCount - 1 downTo 0) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.orientationHelper.getDecoratedStart(child) < limit
                    || layoutInfo.orientationHelper.getTransformedStartWithDecoration(child) < limit
                ) {
                    recycle(recycler, childCount - 1, i)
                    return
                }
            }
        }
    }

    private fun recycle(recycler: Recycler, startIndex: Int, endIndex: Int) {
        if (startIndex == endIndex) {
            return
        }
        if (endIndex > startIndex) {
            for (i in endIndex - 1 downTo startIndex) {
                layoutManager.removeAndRecycleViewAt(i, recycler)
            }
        } else {
            for (i in startIndex downTo endIndex + 1) {
                layoutManager.removeAndRecycleViewAt(i, recycler)
            }
        }
    }

}
