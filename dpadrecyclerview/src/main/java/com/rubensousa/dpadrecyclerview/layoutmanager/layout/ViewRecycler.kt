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

internal open class ViewRecycler(
    protected val layoutManager: RecyclerView.LayoutManager,
    protected val layoutInfo: LayoutInfo,
) {

    open fun updateLayoutState(
        recycled: View,
        position: Int,
        size: Int,
        layoutRequest: LayoutRequest
    ) {}

    fun recycleByLayoutRequest(recycler: RecyclerView.Recycler, layoutRequest: LayoutRequest) {
        if (!layoutRequest.isRecyclingEnabled || layoutRequest.isInfinite) {
            return
        }
        if (layoutRequest.isLayingOutStart()) {
            recycleFromEnd(recycler, layoutRequest)
        } else {
            recycleFromStart(recycler, layoutRequest)
        }
    }

    fun recycleFromStart(recycler: RecyclerView.Recycler, layoutRequest: LayoutRequest) {
        val limit = -layoutRequest.extraLayoutSpaceStart
        val childCount = layoutInfo.getChildCount()
        if (layoutRequest.reverseLayout) {
            for (i in childCount - 1 downTo 0) {
                val child = layoutInfo.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedEnd(child) > limit
                    || layoutInfo.orientationHelper.getTransformedEndWithDecoration(child) > limit
                ) {
                    recycle(recycler, childCount - 1, i, layoutRequest)
                    return
                }
            }
        } else {
            for (i in 0 until childCount) {
                val child = layoutInfo.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedEnd(child) > limit
                    || layoutInfo.orientationHelper.getTransformedEndWithDecoration(child) > limit
                ) {
                    recycle(recycler, 0, i, layoutRequest)
                    return
                }
            }
        }
    }

    fun recycleFromEnd(recycler: RecyclerView.Recycler, layoutRequest: LayoutRequest) {
        val limit = layoutInfo.orientationHelper.end + layoutRequest.extraLayoutSpaceEnd
        val childCount = layoutInfo.getChildCount()
        if (layoutRequest.reverseLayout) {
            for (i in 0 until childCount) {
                val child = layoutInfo.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedStart(child) < limit
                    || layoutInfo.orientationHelper.getTransformedStartWithDecoration(child) < limit
                ) {
                    recycle(recycler, 0, i, layoutRequest)
                    return
                }
            }
        } else {
            for (i in childCount - 1 downTo 0) {
                val child = layoutInfo.getChildAt(i) ?: continue
                if (layoutInfo.getDecoratedStart(child) < limit
                    || layoutInfo.orientationHelper.getTransformedStartWithDecoration(child) < limit
                ) {
                    recycle(recycler, childCount - 1, i, layoutRequest)
                    return
                }
            }
        }
    }

    private fun recycle(
        recycler: RecyclerView.Recycler,
        startIndex: Int,
        endIndex: Int,
        layoutRequest: LayoutRequest
    ) {
        if (startIndex == endIndex) {
            return
        }
        if (endIndex > startIndex) {
            for (i in endIndex - 1 downTo startIndex) {
                recycleViewAt(i, recycler, layoutRequest)
            }
        } else {
            for (i in startIndex downTo endIndex + 1) {
                recycleViewAt(i, recycler, layoutRequest)
            }
        }
    }

    private fun recycleViewAt(
        index: Int,
        recycler: RecyclerView.Recycler,
        layoutRequest: LayoutRequest
    ) {
        val view = layoutInfo.getChildAt(index)
        if (view != null) {
            val position = layoutInfo.getLayoutPositionOf(view)
            layoutManager.removeAndRecycleView(view, recycler)
            val size = layoutInfo.getDecoratedSize(view)
            updateLayoutState(view, position, size, layoutRequest)
        }
    }
}