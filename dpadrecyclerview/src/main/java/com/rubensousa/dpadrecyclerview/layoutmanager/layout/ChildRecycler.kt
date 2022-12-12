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

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration

internal class ChildRecycler(
    private val layoutManager: RecyclerView.LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration
) {

    companion object {
        const val TAG = "ChildRecycler"
    }

    fun recycleByLayoutState(recycler: RecyclerView.Recycler, layoutState: LayoutState) {
        if (!layoutState.isRecyclingEnabled || layoutState.isInfinite()) {
            return
        }
        if (layoutState.isLayingOutStart()) {
            recycleFromEnd(recycler, layoutState)
        } else {
            recycleFromStart(recycler, layoutState)
        }
    }

    private fun recycleFromStart(recycler: RecyclerView.Recycler, layoutState: LayoutState) {
        val limit = -layoutState.extraLayoutSpaceStart
        val childCount = layoutManager.childCount
        if (configuration.reverseLayout) {
            for (i in childCount - 1 downTo 0) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedEnd(child) > limit
                    || layoutInfo.orientationHelper.getTransformedEndWithDecoration(child) > limit
                ) {
                    recycle(recycler, childCount - 1, i, layoutState)
                    return
                }
            }
        } else {
            for (i in 0 until childCount) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedEnd(child) > limit
                    || layoutInfo.orientationHelper.getTransformedEndWithDecoration(child) > limit
                ) {
                    recycle(recycler, 0, i, layoutState)
                    return
                }
            }
        }
    }

    private fun recycleFromEnd(recycler: RecyclerView.Recycler, layoutState: LayoutState) {
        val limit = layoutInfo.orientationHelper.end + layoutState.extraLayoutSpaceEnd
        val childCount = layoutManager.childCount
        if (configuration.reverseLayout) {
            for (i in 0 until childCount) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedStart(child) < limit
                    || layoutInfo.orientationHelper.getTransformedStartWithDecoration(child) < limit
                ) {
                    recycle(recycler, 0, i, layoutState)
                    return
                }
            }
        } else {
            for (i in childCount - 1 downTo 0) {
                val child = layoutManager.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedStart(child) < limit
                    || layoutInfo.orientationHelper.getTransformedStartWithDecoration(child) < limit
                ) {
                    recycle(recycler, childCount - 1, i, layoutState)
                    return
                }
            }
        }
    }

    private fun recycle(
        recycler: RecyclerView.Recycler,
        startIndex: Int,
        endIndex: Int,
        layoutState: LayoutState
    ) {
        if (startIndex == endIndex) {
            return
        }
        if (endIndex > startIndex) {
            for (i in endIndex - 1 downTo startIndex) {
                recycleViewAt(i, recycler, layoutState)
            }
        } else {
            for (i in startIndex downTo endIndex + 1) {
                recycleViewAt(i, recycler, layoutState)
            }
        }
    }

    private fun recycleViewAt(index: Int, recycler: Recycler, layoutState: LayoutState) {
        val view = layoutManager.getChildAt(index)
        if (view != null) {
            layoutManager.removeAndRecycleViewAt(index, recycler)
            Log.i(TAG, "Recycled: ${layoutInfo.getAdapterPositionOf(view)}")
            val size = layoutInfo.getDecoratedSize(view)
            if (layoutState.isLayingOutEnd()) {
                layoutState.increaseStartOffset(size)
            } else {
                layoutState.decreaseEndOffset(size)
            }
        }
    }

}