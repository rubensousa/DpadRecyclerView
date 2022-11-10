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

package com.rubensousa.dpadrecyclerview.internal.layout

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

@SuppressLint("LogNotTimber")
class TvLayoutInfo(
    private val layout: LayoutManager,
    private val configuration: TvLayoutConfiguration
) {

    private var spanSizeLookup: TvSpanSizeLookup = TvSpanSizeLookup.default()
    private var orientationHelper = OrientationHelper.createOrientationHelper(
        layout,
        configuration.orientation
    )
    private var dpadRecyclerView: RecyclerView? = null

    fun isRTL() = layout.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL

    fun update(){
        orientationHelper.onLayoutComplete()
    }

    fun setRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
    }

    fun getSpanSize(position: Int): Int {
        return spanSizeLookup.getSpanSize(position)
    }

    fun getColumnIndex(position: Int): Int {
        return spanSizeLookup.getSpanIndex(position, configuration.spanCount)
    }

    fun getEndColumnIndex(position: Int): Int {
        return getColumnIndex(position) + spanSizeLookup.getSpanSize(position) - 1
    }

    fun getRowIndex(position: Int): Int {
        return spanSizeLookup.getSpanGroupIndex(position, configuration.spanCount)
    }

    fun setSpanSizeLookup(lookup: TvSpanSizeLookup) {
        spanSizeLookup = lookup
    }

    fun getAdapterPositionOfChildAt(index: Int): Int {
        val child = layout.getChildAt(index) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOfView(child)
    }

    fun getAdapterPositionOfView(view: View): Int {
        val params = view.layoutParams as DpadLayoutParams?
        return if (params == null || params.isItemRemoved) {
            // when item is removed, the position value can be any value.
            RecyclerView.NO_POSITION
        } else {
            params.absoluteAdapterPosition
        }
    }

    fun getSpanGroupIndex(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        viewPosition: Int
    ): Int {
        if (!state.isPreLayout) {
            return spanSizeLookup.getCachedSpanGroupIndex(viewPosition, configuration.spanCount)
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(viewPosition)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            Log.w(
                DpadRecyclerView.TAG, "Cannot find span size for pre layout position. $viewPosition"
            )
            return 0
        }
        return spanSizeLookup.getCachedSpanGroupIndex(adapterPosition, configuration.spanCount)
    }

    fun getMeasuredSize(view: View): Int {
        return if (configuration.isVertical()) {
            view.measuredHeight
        } else {
            view.measuredWidth
        }
    }

    fun getStartDecorationSize(view: View): Int {
        return if (configuration.isVertical()) {
            layout.getTopDecorationHeight(view)
        } else {
            layout.getLeftDecorationWidth(view)
        }
    }

    fun getEndDecorationSize(view: View): Int {
        return if (configuration.isVertical()) {
            layout.getBottomDecorationHeight(view)
        } else {
            layout.getRightDecorationWidth(view)
        }
    }

    fun getDecoratedSize(view: View): Int {
        return orientationHelper.getDecoratedMeasurement(view)
    }

    // If the main size is the width, this would be the height and vice-versa
    fun getPerpendicularDecoratedSize(view: View): Int {
        return orientationHelper.getDecoratedMeasurementInOther(view)
    }

    fun isItemFullyVisible(position: Int): Boolean {
        val recyclerView = dpadRecyclerView ?: return false
        val itemView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
            ?: return false
        return itemView.left >= 0
                && itemView.right <= recyclerView.width
                && itemView.top >= 0
                && itemView.bottom <= recyclerView.height
    }

    fun findImmediateChildIndex(view: View): Int {
        var currentView: View? = view
        if (currentView != null && currentView !== dpadRecyclerView) {
            currentView = layout.findContainingItemView(currentView)
            if (currentView != null) {
                var i = 0
                val count = layout.childCount
                while (i < count) {
                    if (layout.getChildAt(i) === currentView) {
                        return i
                    }
                    i++
                }
            }
        }
        return RecyclerView.NO_POSITION
    }

}
