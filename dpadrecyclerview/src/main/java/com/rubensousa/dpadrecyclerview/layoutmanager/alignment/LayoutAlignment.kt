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

package com.rubensousa.dpadrecyclerview.layoutmanager.alignment

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.internal.ChildScrollAlignment
import com.rubensousa.dpadrecyclerview.internal.ParentScrollAlignment
import com.rubensousa.dpadrecyclerview.internal.ViewHolderScrollAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class LayoutAlignment(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration
) {

    companion object {
        const val TAG = "LayoutAlignment"
    }

    private val parentAlignment = ParentScrollAlignment()
    private val childAlignment = ChildScrollAlignment()
    private val viewHolderAlignment = ViewHolderScrollAlignment()

    fun reset() {
        parentAlignment.reset()
    }

    fun update() {
        parentAlignment.setSize(layoutManager.width, layoutManager.height, layoutInfo.orientation)
        parentAlignment.reverseLayout = configuration.reverseLayout
        parentAlignment.setPadding(
            layoutManager.paddingLeft,
            layoutManager.paddingRight,
            layoutManager.paddingTop,
            layoutManager.paddingBottom,
            layoutInfo.orientation
        )
    }

    fun setParentAlignment(alignment: ParentAlignment) {
        parentAlignment.defaultAlignment = alignment
    }

    fun getParentAlignment() = parentAlignment.defaultAlignment

    fun setChildAlignment(config: ChildAlignment) {
        childAlignment.setAlignment(config)
    }

    fun getChildAlignment() = childAlignment.getAlignment()

    /**
     * Calculates the view center based on the alignment for this view
     * specified by the parent and child alignment configurations.
     */
    fun calculateViewCenterForLayout(view: View): Int {
        updateChildAlignments(view)
        return parentAlignment.calculateKeyline()
    }

    fun findSubPositionOfChild(
        recyclerView: RecyclerView, view: View?, childView: View?
    ): Int {
        if (view == null || childView == null) {
            return 0
        }
        val viewHolder = recyclerView.getChildViewHolder(view)
        if (viewHolder !is DpadViewHolder) {
            return 0
        }
        val alignments = viewHolder.getAlignments()
        if (alignments.isEmpty()) {
            return 0
        }
        var currentChildView = childView
        while (currentChildView !== view && currentChildView != null) {
            if (currentChildView.id != View.NO_ID) {
                alignments.forEachIndexed { index, alignment ->
                    val id = currentChildView?.id
                    if (id != null && id != View.NO_ID) {
                        if (alignment.getFocusViewId() == id) {
                            return index
                        }
                    }
                }
            }
            currentChildView = currentChildView.parent as? View?
        }
        return 0
    }

    fun getCappedScroll(scrollOffset: Int): Int {
        return if (scrollOffset > 0) {
            if (parentAlignment.isMaxUnknown) {
                scrollOffset
            } else if (scrollOffset > parentAlignment.maxScroll) {
                parentAlignment.maxScroll
            } else {
                scrollOffset
            }
        } else if (parentAlignment.isMinUnknown) {
            scrollOffset
        } else if (scrollOffset < parentAlignment.minScroll) {
            parentAlignment.minScroll
        } else {
            scrollOffset
        }
    }

    fun calculateScrollForAlignment(view: View): Int {
        updateScrollLimits()
        updateChildAlignments(view)
        return calculateDistanceToKeyline(view)
    }

    fun getMaxScroll() = parentAlignment.maxScroll

    fun getMinScroll() = parentAlignment.minScroll

    fun calculateScrollOffset(
        recyclerView: RecyclerView,
        view: View,
        childView: View?
    ): Int {
        var scrollOffset = calculateScrollForAlignment(view)
        if (childView != null) {
            scrollOffset = calculateAdjustedAlignedScrollDistance(
                recyclerView, scrollOffset, view, childView
            )
        }
        return scrollOffset
    }

    private fun updateChildAlignments(view: View) {
        val layoutParams = view.layoutParams as DpadLayoutParams
        val viewHolder = layoutInfo.getChildViewHolder(view) ?: return
        val alignments = if (viewHolder is DpadViewHolder) {
            viewHolder.getAlignments()
        } else {
            null
        }
        if (alignments.isNullOrEmpty()) {
            // Use the default child alignment strategy
            // if this ViewHolder didn't request a custom alignment strategy
            childAlignment.updateAlignments(view, layoutParams, layoutInfo.orientation)
        } else {
            viewHolderAlignment.updateAlignments(
                view, layoutParams, alignments, layoutInfo.orientation
            )
        }
    }

    // This can only be called after all views are in their final positions
    fun updateScrollLimits() {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return
        }
        val maxAddedPosition: Int
        val minAddedPosition: Int
        val maxEdgePosition: Int
        val minEdgePosition: Int
        if (!layoutInfo.isRTL()) {
            maxAddedPosition = layoutInfo.findLastAddedPosition()
            maxEdgePosition = itemCount - 1
            minAddedPosition = layoutInfo.findFirstAddedPosition()
            minEdgePosition = 0
        } else {
            maxAddedPosition = layoutInfo.findFirstAddedPosition()
            maxEdgePosition = 0
            minAddedPosition = layoutInfo.findLastAddedPosition()
            minEdgePosition = itemCount - 1
        }
        if (maxAddedPosition < 0 || minAddedPosition < 0) {
            parentAlignment.invalidateScrollMin()
            parentAlignment.invalidateScrollMax()
            return
        }
        val highAvailable = maxAddedPosition == maxEdgePosition
        val lowAvailable = minAddedPosition == minEdgePosition
        if (!highAvailable && parentAlignment.isMaxUnknown
            && !lowAvailable && parentAlignment.isMinUnknown
        ) {
            return
        }
        val maxEdge: Int
        var maxViewCenter = 0
        if (highAvailable) {
            maxEdge = getMaxEdge(maxAddedPosition) ?: Int.MAX_VALUE
            layoutManager.findViewByPosition(maxAddedPosition)?.let { maxChild ->
                maxViewCenter = getViewCenter(maxChild)
                val layoutParams = maxChild.layoutParams as DpadLayoutParams
                val multipleAlignments = layoutParams.getAlignmentPositions()
                if (multipleAlignments != null && multipleAlignments.isNotEmpty()) {
                    maxViewCenter += multipleAlignments.last() - multipleAlignments.first()
                }
            }
        } else {
            maxEdge = Int.MAX_VALUE
            maxViewCenter = Int.MAX_VALUE
        }
        val minEdge: Int
        var minViewCenter = 0
        if (lowAvailable) {
            minEdge = getEdge(minAddedPosition) ?: Int.MIN_VALUE
            layoutManager.findViewByPosition(minAddedPosition)?.let { minChild ->
                minViewCenter = getViewCenter(minChild)
            }
        } else {
            minEdge = Int.MIN_VALUE
            minViewCenter = Int.MIN_VALUE
        }
        parentAlignment.updateMinMax(minEdge, maxEdge, minViewCenter, maxViewCenter)
    }

    private fun getMaxEdge(index: Int): Int? {
        val view = layoutManager.findViewByPosition(index) ?: return null
        return if (layoutInfo.isRTL() && layoutInfo.isHorizontal()) {
            getViewMin(view)
        } else {
            getViewMax(view)
        }
    }

    /**
     * Will return the start coordinate of the view in horizontal mode
     * or the top coordinate in vertical mode
     */
    private fun getEdge(index: Int): Int? {
        val view = layoutManager.findViewByPosition(index) ?: return null
        if (layoutInfo.isRTL() && layoutInfo.isHorizontal()) {
            return getViewMax(view)
        } else {
            return getViewMin(view)
        }
    }

    private fun getViewMin(view: View): Int {
        return layoutInfo.orientationHelper.getDecoratedStart(view)
    }

    private fun getViewMax(view: View): Int {
        return layoutInfo.orientationHelper.getDecoratedEnd(view)
    }

    private fun getViewCenter(view: View): Int {
        return if (layoutInfo.isHorizontal()) {
            getViewCenterX(view)
        } else {
            getViewCenterY(view)
        }
    }

    private fun getViewCenterX(view: View): Int {
        val layoutParams = view.layoutParams as DpadLayoutParams
        return layoutParams.getOpticalLeft(view) + layoutParams.alignX
    }

    private fun getViewCenterY(view: View): Int {
        val layoutParams = view.layoutParams as DpadLayoutParams
        return layoutParams.getOpticalTop(view) + layoutParams.alignY
    }

    /**
     * Return the scroll delta required to make the view selected and aligned.
     * If the returned value is 0, there is no need to scroll.
     */
    private fun calculateDistanceToKeyline(
        view: View,
        subPositionAlignment: ParentAlignment? = null
    ): Int {
        return parentAlignment.calculateScrollDistance(getViewCenter(view), subPositionAlignment)
    }

    private fun calculateAdjustedAlignedScrollDistance(
        recyclerView: RecyclerView, offset: Int, view: View, childView: View
    ): Int {
        var scrollValue = offset
        val subPosition = findSubPositionOfChild(recyclerView, view, childView)
        if (subPosition != 0) {
            val layoutParams = view.layoutParams as DpadLayoutParams
            val alignments = layoutParams.getAlignmentPositions()
            if (alignments != null && alignments.isNotEmpty()) {
                scrollValue += alignments[subPosition] - alignments[0]
            }
        }
        return scrollValue
    }

}
