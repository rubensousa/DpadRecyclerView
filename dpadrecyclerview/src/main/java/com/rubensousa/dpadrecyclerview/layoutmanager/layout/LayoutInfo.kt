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

import android.graphics.Rect
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration

internal class LayoutInfo(
    private val layout: LayoutManager,
    private val configuration: LayoutConfiguration
) {

    val orientation: Int
        get() = configuration.orientation

    var orientationHelper = OrientationHelper.createOrientationHelper(
        layout, configuration.orientation
    )
        private set

    var isScrolling = false
        private set
    var isLayoutInProgress = false
        private set

    var gravity: Int = Gravity.START.or(Gravity.TOP)
        private set

    private var dpadRecyclerView: RecyclerView? = null

    fun isRTL() = layout.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL

    fun isHorizontal() = configuration.isHorizontal()

    fun isVertical() = configuration.isVertical()

    fun setGravity(gravity: Int) {
        this.gravity = gravity
    }

    /**
     * Needs to be called after onLayoutChildren when not in pre-layout
     */
    fun onLayoutCompleted() {
        orientationHelper.onLayoutComplete()
    }

    fun setIsScrolling(isScrolling: Boolean) {
        this.isScrolling = isScrolling
    }

    fun setLayoutInProgress(isInProgress: Boolean) {
        isLayoutInProgress = isInProgress
    }

    fun setRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
    }

    fun getSpanSize(position: Int): Int {
        return configuration.spanSizeLookup.getSpanSize(position)
    }

    fun getColumnIndex(position: Int): Int {
        return configuration.spanSizeLookup.getSpanIndex(position, configuration.spanCount)
    }

    fun getEndColumnIndex(position: Int): Int {
        return getColumnIndex(position) + configuration.spanSizeLookup.getSpanSize(position) - 1
    }

    fun getRowIndex(position: Int): Int {
        return configuration.spanSizeLookup
            .getSpanGroupIndex(position, configuration.spanCount)
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
            return configuration.spanSizeLookup
                .getCachedSpanGroupIndex(viewPosition, configuration.spanCount)
        }
        val adapterPosition = recycler.convertPreLayoutPositionToPostLayout(viewPosition)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            Log.w(
                DpadRecyclerView.TAG, "Cannot find span size for pre layout position. $viewPosition"
            )
            return 0
        }
        return configuration.spanSizeLookup
            .getCachedSpanGroupIndex(adapterPosition, configuration.spanCount)
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

    fun getDecoratedLeft(child: View, decoratedLeft: Int): Int {
        return decoratedLeft + getLayoutParams(child).leftInset
    }

    fun getDecoratedTop(child: View, decoratedTop: Int): Int {
        return decoratedTop + getLayoutParams(child).topInset
    }

    fun getDecoratedRight(child: View, decoratedRight: Int): Int {
        return decoratedRight - getLayoutParams(child).rightInset
    }

    fun getDecoratedBottom(child: View, decoratedBottom: Int): Int {
        return decoratedBottom - getLayoutParams(child).bottomInset
    }

    fun getDecoratedBoundsWithMargins(view: View, outBounds: Rect) {
        val params = view.layoutParams as DpadLayoutParams
        outBounds.left += params.leftInset
        outBounds.top += params.topInset
        outBounds.right -= params.rightInset
        outBounds.bottom -= params.bottomInset
    }

    private fun getLayoutParams(child: View): DpadLayoutParams {
        return child.layoutParams as DpadLayoutParams
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

    fun isWrapContent(): Boolean {
        return orientationHelper.mode == View.MeasureSpec.UNSPECIFIED && orientationHelper.end == 0
    }

    fun getChildClosestToStart(): View? {
        val startIndex = if (configuration.reverseLayout) {
            layout.childCount - 1
        } else {
            0
        }
        return layout.getChildAt(startIndex)
    }

    fun getChildClosestToEnd(): View? {
        val endIndex = if (configuration.reverseLayout) {
            0
        } else {
            layout.childCount - 1
        }
        return layout.getChildAt(endIndex)
    }

    fun findFirstAddedPosition(): Int {
        if (layout.childCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val child = layout.getChildAt(0) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOfView(child)
    }

    fun findLastAddedPosition(): Int {
        if (layout.childCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val child = layout.getChildAt(layout.childCount - 1) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOfView(child)
    }

}
