package com.rubensousa.dpadrecyclerview.internal

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.*

internal class ScrollAlignment(
    private val layoutManager: DpadLayoutManager
) {

    private var orientationHelper = OrientationHelper.createOrientationHelper(
        layoutManager, layoutManager.orientation
    )
    private var orientation: Int = layoutManager.orientation
    private val parentAlignment = ParentScrollAlignment()
    private val childAlignment = ChildScrollAlignment()
    private val viewHolderAlignment = ViewHolderScrollAlignment()

    fun setOrientation(orientation: Int) {
        orientationHelper = OrientationHelper.createOrientationHelper(
            layoutManager,
            orientation
        )
        this.orientation = orientation
    }

    fun reset() {
        parentAlignment.reset()
    }

    fun updateLayoutState(
        width: Int, height: Int, reversedFlow: Boolean,
        paddingLeft: Int, paddingRight: Int,
        paddingTop: Int, paddingBottom: Int
    ) {
        parentAlignment.setSize(width, height, orientation)
        parentAlignment.reversedFlow = reversedFlow
        parentAlignment.setPadding(
            paddingLeft,
            paddingRight,
            paddingTop,
            paddingBottom,
            orientation
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

    fun getCappedScroll(offset: Int): Int {
        var scrollOffset = offset
        if (offset > 0) {
            if (!parentAlignment.isMaxUnknown) {
                val maxScroll = parentAlignment.maxScroll
                if (offset > maxScroll) {
                    scrollOffset = maxScroll
                }
            }
        } else if (offset < 0) {
            if (!parentAlignment.isMinUnknown) {
                val minScroll = parentAlignment.minScroll
                if (offset < minScroll) {
                    scrollOffset = minScroll
                }
            }
        }
        return scrollOffset
    }

    fun updateScroll(
        recyclerView: RecyclerView,
        view: View,
        childView: View?
    ): Int? {
        updateScrollLimits(layoutManager)
        updateChildAlignments(recyclerView, view)
        var scrollOffset = calculateAlignedScrollDistance(view)
        if (childView != null) {
            scrollOffset = calculateAdjustedAlignedScrollDistance(
                recyclerView, scrollOffset, view, childView
            )
        }
        return if (scrollOffset != 0) {
            scrollOffset
        } else {
            null
        }
    }

    private fun updateChildAlignments(recyclerView: RecyclerView, view: View) {
        val layoutParams = view.layoutParams as DpadLayoutParams
        val viewHolder = recyclerView.getChildViewHolder(view) ?: return
        val alignments = if (viewHolder is DpadViewHolder) {
            viewHolder.getAlignments()
        } else {
            null
        }
        if (alignments == null || alignments.isEmpty()) {
            // Use the default child alignment strategy
            // if this ViewHolder didn't request a custom alignment strategy
            childAlignment.updateAlignments(view, layoutParams, orientation)
        } else {
            viewHolderAlignment.updateAlignments(view, layoutParams, alignments, orientation)
        }
    }

    // This can only be called after all views are in their final positions
    private fun updateScrollLimits(layoutManager: DpadLayoutManager) {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return
        }
        val maxAddedPosition: Int
        val minAddedPosition: Int
        val maxEdgePosition: Int
        val minEdgePosition: Int
        if (!layoutManager.isRTL()) {
            maxAddedPosition = layoutManager.findLastAddedPosition()
            maxEdgePosition = itemCount - 1
            minAddedPosition = layoutManager.findFirstAddedPosition()
            minEdgePosition = 0
        } else {
            maxAddedPosition = layoutManager.findFirstAddedPosition()
            maxEdgePosition = 0
            minAddedPosition = layoutManager.findLastAddedPosition()
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
        return if (layoutManager.isRTL() && layoutManager.isHorizontal()) {
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
        if (layoutManager.isRTL() && layoutManager.isHorizontal()) {
            return getViewMax(view)
        } else {
            return getViewMin(view)
        }
    }

    private fun getViewMin(view: View): Int {
        return orientationHelper.getDecoratedStart(view)
    }

    private fun getViewMax(view: View): Int {
        return orientationHelper.getDecoratedEnd(view)
    }

    private fun getViewCenter(view: View): Int {
        return if (orientation == RecyclerView.HORIZONTAL) {
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
    private fun calculateAlignedScrollDistance(
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
