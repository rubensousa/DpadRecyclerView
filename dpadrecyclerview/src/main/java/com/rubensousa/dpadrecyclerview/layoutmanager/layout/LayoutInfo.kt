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
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration


internal class LayoutInfo(
    private val layout: LayoutManager,
    private val configuration: LayoutConfiguration
) {

    val orientation: Int
        get() = configuration.orientation


    var orientationHelper: OrientationHelper = OrientationHelper.createOrientationHelper(
        layout, configuration.orientation
    )
        private set

    var secondaryOrientationHelper = OrientationHelper.createOrientationHelper(
        layout, getOppositeOrientation()
    )
        private set

    var isScrolling = false
        private set

    var isScrollingToTarget = false
        private set

    var isLayoutInProgress = false
        private set

    var hasLaidOutViews = false
        private set

    private var recyclerView: RecyclerView? = null

    fun getConfiguration() = configuration

    fun isRTL() = layout.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL

    fun isHorizontal() = configuration.isHorizontal()

    fun isVertical() = configuration.isVertical()

    fun updateOrientation() {
        orientationHelper = OrientationHelper.createOrientationHelper(
            layout, configuration.orientation
        )
        secondaryOrientationHelper = OrientationHelper.createOrientationHelper(
            layout, getOppositeOrientation()
        )
    }

    /**
     * Needs to be called after onLayoutChildren when not in pre-layout
     */
    fun onLayoutCompleted() {
        isLayoutInProgress = false
        orientationHelper.onLayoutComplete()
        hasLaidOutViews = layout.childCount > 0
    }

    fun setIsScrolling(isScrolling: Boolean) {
        this.isScrolling = isScrolling
    }

    fun setIsScrollingToTarget(isScrolling: Boolean) {
        isScrollingToTarget = isScrolling
    }

    fun setLayoutInProgress() {
        isLayoutInProgress = true
    }

    fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
    }

    fun getSpanCount() = configuration.spanCount

    fun isGrid() = configuration.spanCount > 1

    fun getSpanSize(position: Int): Int {
        return configuration.spanSizeLookup.getSpanSize(position)
    }

    fun getStartSpanIndex(position: Int): Int {
        return configuration.spanSizeLookup.getCachedSpanIndex(position, configuration.spanCount)
    }

    fun getEndSpanIndex(position: Int): Int {
        return getStartSpanIndex(position) + configuration.spanSizeLookup.getSpanSize(position) - 1
    }

    fun getSpanGroupIndex(position: Int): Int {
        return configuration.spanSizeLookup.getCachedSpanGroupIndex(position, configuration.spanCount)
    }

    fun getAdapterPositionOfChildAt(index: Int): Int {
        val child = layout.getChildAt(index) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOf(child)
    }

    fun getAdapterPositionOf(view: View): Int {
        return getLayoutParams(view).absoluteAdapterPosition
    }

    fun getLayoutPositionOf(view: View): Int {
        return getLayoutParams(view).viewLayoutPosition
    }

    fun getStartAfterPadding() = orientationHelper.startAfterPadding

    fun getSecondaryStartAfterPadding() = secondaryOrientationHelper.startAfterPadding

    fun getEndAfterPadding() = orientationHelper.endAfterPadding

    fun getSecondaryEndAfterPadding() = secondaryOrientationHelper.endAfterPadding

    fun getTotalSpace(): Int = orientationHelper.totalSpace

    fun getSecondaryTotalSpace(): Int = secondaryOrientationHelper.totalSpace

    fun getDecoratedStart(view: View): Int {
        return orientationHelper.getDecoratedStart(view)
    }

    fun getDecoratedEnd(view: View): Int {
        return orientationHelper.getDecoratedEnd(view)
    }

    fun getDecoratedSize(view: View): Int {
        return orientationHelper.getDecoratedMeasurement(view)
    }

    fun hasCreatedLastItem(): Boolean {
        val count = layout.itemCount
        return count == 0 || recyclerView?.findViewHolderForAdapterPosition(count - 1) != null
    }

    fun hasCreatedFirstItem(): Boolean {
        val count = layout.itemCount
        return count == 0 || recyclerView?.findViewHolderForAdapterPosition(0) != null
    }

    fun getLayoutParams(child: View): DpadLayoutParams {
        return child.layoutParams as DpadLayoutParams
    }

    // If the main size is the width, this would be the height and vice-versa
    fun getPerpendicularDecoratedSize(view: View): Int {
        return orientationHelper.getDecoratedMeasurementInOther(view)
    }

    fun isItemFullyVisible(position: Int): Boolean {
        val recyclerView = recyclerView ?: return false
        val itemView = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
            ?: return false
        return itemView.left >= 0
                && itemView.right <= recyclerView.width
                && itemView.top >= 0
                && itemView.bottom <= recyclerView.height
    }

    fun findIndexOf(view: View?): Int {
        if (view != null && view !== recyclerView) {
            val currentView = layout.findContainingItemView(view)
            if (currentView != null) {
                var index = 0
                val count = layout.childCount
                while (index < count) {
                    if (layout.getChildAt(index) === currentView) {
                        return index
                    }
                    index++
                }
            }
        }
        return RecyclerView.NO_POSITION
    }

    fun getChildClosestToStart(): View? {
        return layout.getChildAt(0)
    }

    fun getChildClosestToEnd(): View? {
        return layout.getChildAt(layout.childCount - 1)
    }

    fun findFirstAddedPosition(): Int {
        if (layout.childCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val child = layout.getChildAt(0) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOf(child)
    }

    fun getOldPositionOf(view: View): Int {
        return getChildViewHolder(view)?.oldPosition ?: RecyclerView.NO_POSITION
    }

    fun findLastAddedPosition(): Int {
        if (layout.childCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val child = layout.getChildAt(layout.childCount - 1) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOf(child)
    }

    fun findFirstVisiblePosition(): Int {
        return findFirstChildWithinParentBounds(
            startIndex = 0,
            endIndex = layout.childCount,
            onlyCompletelyVisible = false
        )
    }

    fun findFirstCompletelyVisiblePosition(): Int {
        return findFirstChildWithinParentBounds(
            startIndex = 0,
            endIndex = layout.childCount,
            onlyCompletelyVisible = true
        )
    }

    fun findLastVisiblePosition(): Int {
        return findFirstChildWithinParentBounds(
            startIndex = layout.childCount - 1,
            endIndex = -1,
            onlyCompletelyVisible = false
        )
    }

    fun findLastCompletelyVisiblePosition(): Int {
        return findFirstChildWithinParentBounds(
            startIndex = layout.childCount - 1,
            endIndex = -1,
            onlyCompletelyVisible = true
        )
    }

    /**
     * @param startIndex index at which the search should start
     * @param endIndex index at which the search should stop (not inclusive)
     * @param onlyCompletelyVisible true if we should only find views completely visible
     */
    private fun findFirstChildWithinParentBounds(
        startIndex: Int,
        endIndex: Int,
        onlyCompletelyVisible: Boolean
    ): Int {
        val increment = if (startIndex < endIndex) 1 else -1
        var currentIndex = startIndex
        val parentStart = orientationHelper.startAfterPadding
        val parentEnd = orientationHelper.endAfterPadding
        while (currentIndex != endIndex) {
            val child = layout.getChildAt(currentIndex) ?: continue
            val childStart = getDecoratedStart(child)
            val childEnd = getDecoratedEnd(child)
            val isChildCompletelyVisible = childStart >= parentStart && childEnd <= parentEnd
            if (onlyCompletelyVisible) {
                if (isChildCompletelyVisible) {
                    return getLayoutPositionOf(child)
                }
            } else {
                val isClippedAtStart = childEnd >= parentStart && childStart <= parentStart
                val isClippedAtEnd = childStart <= parentEnd && childEnd >= parentEnd
                if (isChildCompletelyVisible || isClippedAtEnd || isClippedAtStart) {
                    return getLayoutPositionOf(child)
                }
            }
            currentIndex += increment
        }
        return RecyclerView.NO_POSITION
    }

    fun getChildViewHolder(view: View): ViewHolder? {
        return recyclerView?.findContainingViewHolder(view)
    }

    fun findViewByPosition(position: Int): View? {
        return layout.findViewByPosition(position)
    }

    fun findViewByAdapterPosition(position: Int): View? {
        return recyclerView?.findViewHolderForAdapterPosition(position)?.itemView
    }

    fun getChildCount() = layout.childCount

    fun getChildAt(index: Int) = layout.getChildAt(index)

    /**
     * Calculates the view layout order. (e.g. from end to start or start to end)
     * RTL layout support is applied automatically. So if layout is RTL and
     * [LayoutConfiguration.reverseLayout] is true, elements will be laid out starting from left.
     */
    fun shouldReverseLayout(): Boolean {
        return if (configuration.isVertical() || !isRTL()) {
            configuration.reverseLayout
        } else {
            !configuration.reverseLayout
        }
    }

    fun isInfinite(): Boolean {
        return orientationHelper.mode == View.MeasureSpec.UNSPECIFIED && orientationHelper.end == 0
    }

    fun isViewFocusable(view: View): Boolean {
        return view.visibility == View.VISIBLE && view.hasFocusable()
    }

    fun didViewHolderStateChange(
        viewHolder: ViewHolder,
        pivotPosition: Int,
        startOldPosition: Int,
        endOldPosition: Int,
        reverseLayout: Boolean
    ): Boolean {
        val view = viewHolder.itemView
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        // If layout might change
        if (layoutParams.isItemChanged || layoutParams.isItemRemoved || view.isLayoutRequested) {
            return true
        }
        // If focus was lost
        if (view.hasFocus() && pivotPosition != layoutParams.absoluteAdapterPosition) {
            return true
        }
        // If focus was gained
        if (!view.hasFocus() && pivotPosition == layoutParams.absoluteAdapterPosition) {
            return true
        }
        val newPosition = getAdapterPositionOf(view)
        // If it moved outside the previous visible range
        return if (!reverseLayout) {
            newPosition < startOldPosition || newPosition > endOldPosition
        } else {
            newPosition > startOldPosition || newPosition < endOldPosition
        }
    }

    fun getDecorationInsets(view: View, rect: Rect) {
        rect.left = layout.getLeftDecorationWidth(view)
        rect.top = layout.getTopDecorationHeight(view)
        rect.right = layout.getRightDecorationWidth(view)
        rect.bottom = layout.getBottomDecorationHeight(view)
    }

    fun getDecoratedBounds(view: View): ViewBounds {
        val layoutParams = view.layoutParams as DpadLayoutParams
        return ViewBounds(
            left = layout.getDecoratedLeft(view) - layoutParams.leftMargin,
            top = layout.getDecoratedTop(view) - layoutParams.topMargin,
            right = layout.getDecoratedRight(view) + layoutParams.rightMargin,
            bottom = layout.getDecoratedBottom(view) + layoutParams.bottomMargin
        )
    }

    private fun getOppositeOrientation(): Int {
        return if (configuration.isVertical()) {
            RecyclerView.HORIZONTAL
        } else {
            RecyclerView.VERTICAL
        }
    }
}
