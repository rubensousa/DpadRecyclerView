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
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

internal class LayoutAlignment(
    private val layoutManager: LayoutManager,
    private val layoutInfo: LayoutInfo
) {

    companion object {
        const val TAG = "LayoutAlignment"
    }

    private val parentAlignment = ParentScrollAlignment()
    private val childAlignment = ChildScrollAlignment()
    private val viewHolderAlignment = ViewHolderScrollAlignment()

    fun update() {
        parentAlignment.updateLayoutInfo(
            layoutManager, layoutInfo.orientation, layoutInfo.shouldReverseLayout()
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

    fun getParentKeyline(): Int {
        return parentAlignment.calculateKeyline()
    }

    fun getViewAtSubPosition(view: View, subPosition: Int): View? {
        val viewHolder = layoutInfo.getChildViewHolder(view)
        val childAlignments = (viewHolder as? DpadViewHolder)?.getAlignments() ?: return null
        if (subPosition >= childAlignments.size) {
            return null
        }
        val subPositionViewId = childAlignments[subPosition].getFocusViewId()
        return view.findViewById(subPositionViewId)
    }

    fun getSubPositionOfView(view: View?, childView: View?): Int {
        if (view == null || childView == null) {
            return 0
        }
        val viewHolder = layoutInfo.getChildViewHolder(view)
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
        val endScrollLimit = if (!layoutInfo.shouldReverseLayout()) {
            parentAlignment.endScrollLimit
        } else {
            parentAlignment.startScrollLimit
        }
        val startScrollLimit = parentAlignment.startScrollLimit
        return if (scrollOffset > 0) {
            if (parentAlignment.isScrollLimitInvalid(endScrollLimit)) {
                scrollOffset
            } else if (scrollOffset > endScrollLimit) {
                endScrollLimit
            } else {
                scrollOffset
            }
        } else if (parentAlignment.isScrollLimitInvalid(startScrollLimit)) {
            scrollOffset
        } else if (scrollOffset < startScrollLimit) {
            startScrollLimit
        } else {
            scrollOffset
        }
    }

    fun calculateScrollForAlignment(view: View): Int {
        updateChildAlignments(view)
        updateScrollLimits()
        return calculateScrollToTarget(view)
    }

    fun calculateScrollOffset(view: View, subPosition: Int): Int {
        val viewAtSubPosition = getViewAtSubPosition(view, subPosition)
        return calculateScrollOffset(view, viewAtSubPosition)
    }

    fun calculateScrollOffset(view: View, childView: View?): Int {
        var scrollOffset = calculateScrollForAlignment(view)
        if (childView != null) {
            scrollOffset = calculateAdjustedAlignedScrollDistance(scrollOffset, view, childView)
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
            childAlignment.updateAlignments(
                view,
                layoutParams,
                layoutInfo.orientation,
                layoutInfo.shouldReverseLayout()
            )
        } else {
            viewHolderAlignment.updateAlignments(
                view,
                layoutParams,
                alignments,
                layoutInfo.orientation,
                layoutInfo.shouldReverseLayout()
            )
        }
    }

    fun updateScrollLimits() {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return
        }
        val endAdapterPos: Int
        val startAdapterPos: Int
        val endLayoutPos: Int
        val startLayoutPos: Int
        if (!layoutInfo.shouldReverseLayout()) {
            endAdapterPos = layoutInfo.findLastAddedPosition()
            endLayoutPos = itemCount - 1
            startAdapterPos = layoutInfo.findFirstAddedPosition()
            startLayoutPos = 0
        } else {
            endAdapterPos = layoutInfo.findFirstAddedPosition()
            endLayoutPos = 0
            startAdapterPos = layoutInfo.findLastAddedPosition()
            startLayoutPos = itemCount - 1
        }
        if (endAdapterPos < 0 || startAdapterPos < 0) {
            parentAlignment.invalidateScrollLimits()
            return
        }
        val isEndAvailable = isEndAvailable(endAdapterPos, endLayoutPos, startLayoutPos)
        val isStartAvailable = isStartAvailable(startAdapterPos, endLayoutPos, startLayoutPos)
        if (!isEndAvailable && parentAlignment.isEndUnknown
            && !isStartAvailable && parentAlignment.isStartUnknown
        ) {
            return
        }
        val endEdge: Int
        var endViewAnchor = 0
        if (isEndAvailable) {
            endEdge = getEndEdge(endAdapterPos) ?: Int.MAX_VALUE
            layoutManager.findViewByPosition(endAdapterPos)?.let { maxChild ->
                endViewAnchor = getAnchor(maxChild)
                val layoutParams = maxChild.layoutParams as DpadLayoutParams
                val multipleAlignments = layoutParams.getAlignmentPositions()
                if (multipleAlignments != null && multipleAlignments.isNotEmpty()) {
                    endViewAnchor += multipleAlignments.last() - multipleAlignments.first()
                }
            }
        } else {
            endEdge = Int.MAX_VALUE
            endViewAnchor = Int.MAX_VALUE
        }
        val startEdge: Int
        var startViewAnchor = 0
        if (isStartAvailable) {
            startEdge = getStartEdge(startAdapterPos) ?: Int.MIN_VALUE
            layoutManager.findViewByPosition(startAdapterPos)?.let { minChild ->
                startViewAnchor = getAnchor(minChild)
            }
        } else {
            startEdge = Int.MIN_VALUE
            startViewAnchor = Int.MIN_VALUE
        }
        if (!layoutInfo.shouldReverseLayout()) {
            parentAlignment.updateEndLimit(endEdge, endViewAnchor)
            parentAlignment.updateStartLimit(startEdge, startViewAnchor)
        } else {
            parentAlignment.updateStartLimit(endEdge, endViewAnchor)
            parentAlignment.updateEndLimit(startEdge, startViewAnchor)
        }
    }

    private fun isEndAvailable(
        adapterPosition: Int,
        maxLayoutPosition: Int,
        minLayoutPosition: Int
    ): Boolean {
        return if (!layoutInfo.shouldReverseLayout()) {
            adapterPosition == maxLayoutPosition
        } else {
            adapterPosition == minLayoutPosition
        }
    }

    private fun isStartAvailable(
        adapterPosition: Int,
        maxLayoutPosition: Int,
        minLayoutPosition: Int
    ): Boolean {
        return if (!layoutInfo.shouldReverseLayout()) {
            adapterPosition == minLayoutPosition
        } else {
            adapterPosition == maxLayoutPosition
        }
    }

    private fun getEndEdge(index: Int): Int? {
        val view = layoutManager.findViewByPosition(index) ?: return null
        return if (!layoutInfo.shouldReverseLayout()) {
            layoutInfo.orientationHelper.getDecoratedEnd(view)
        } else {
            layoutInfo.orientationHelper.getDecoratedStart(view)
        }
    }

    private fun getStartEdge(index: Int): Int? {
        val view = layoutManager.findViewByPosition(index) ?: return null
        return if (!layoutInfo.shouldReverseLayout()) {
            layoutInfo.orientationHelper.getDecoratedStart(view)
        } else {
            layoutInfo.orientationHelper.getDecoratedEnd(view)
        }
    }

    private fun getAnchor(view: View): Int {
        return if (layoutInfo.isHorizontal()) {
            getHorizontalAnchor(view)
        } else {
            getVerticalAnchor(view)
        }
    }

    private fun getHorizontalAnchor(view: View): Int {
        val layoutParams = view.layoutParams as DpadLayoutParams
        return layoutParams.getOpticalLeft(view) + layoutParams.absoluteAnchor
    }

    private fun getVerticalAnchor(view: View): Int {
        val layoutParams = view.layoutParams as DpadLayoutParams
        return layoutParams.getOpticalTop(view) + layoutParams.absoluteAnchor
    }

    /**
     * Return the scroll delta required to make the view selected and aligned.
     * If the returned value is 0, there is no need to scroll.
     */
    private fun calculateScrollToTarget(
        view: View,
        subPositionAlignment: ParentAlignment? = null
    ): Int {
        return parentAlignment.calculateScrollOffset(getAnchor(view), subPositionAlignment)
    }

    private fun calculateAdjustedAlignedScrollDistance(
        offset: Int,
        view: View,
        childView: View
    ): Int {
        var scrollValue = offset
        val subPosition = getSubPositionOfView(view, childView)
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
