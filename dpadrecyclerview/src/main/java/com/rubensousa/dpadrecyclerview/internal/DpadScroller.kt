package com.rubensousa.dpadrecyclerview.internal

import android.graphics.PointF
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadGridLayoutManager
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import kotlin.math.sqrt

internal class DpadScroller(
    private val scrollAlignment: ScrollAlignment,
    private val focusManager: DpadFocusManager,
    private val layout: DpadGridLayoutManager
) {

    var smoothScrollSpeedFactor = 1f
    var childDrawingOrderEnabled = false

    var isSelectionInProgress = false
        private set

    private var isAligningFocusedPosition = false
    var pendingSelectionUpdate = false
    private var currentSmoothScroller: GridLinearSmoothScroller? = null

    fun onLayoutChildren(recyclerView: RecyclerView?) {
        // Consume pending focus changes due to adapter changes or save/restore state
        // This is usually true unless in smoothScrolling
        focusManager.consumePendingFocusChanges()
        recyclerView?.let { scrollToFocusedPosition(it, smooth = false) }
    }

    fun onLayoutCompleted(recyclerView: RecyclerView?) {
        recyclerView?.let { view ->
            if (isAligningFocusedPosition) {
                isAligningFocusedPosition = false
                scrollToFocusedPosition(view, smooth = false)
            }
        }
        if (pendingSelectionUpdate) {
            pendingSelectionUpdate = false
            layout.dispatchViewHolderSelected()
            layout.dispatchViewHolderSelectedAndAligned()
        }
    }


    fun scrollToFocusedPosition(recyclerView: RecyclerView, smooth: Boolean) {
        val previousFocusPosition = focusManager.position
        val newItemCount = layout.itemCount
        var newFocusPosition = previousFocusPosition
        if (newItemCount == 0) {
            newFocusPosition = 0
        } else if (newFocusPosition >= newItemCount) {
            newFocusPosition = newItemCount - 1
        } else if (newFocusPosition == RecyclerView.NO_POSITION && newItemCount > 0) {
            newFocusPosition = 0
        }
        scrollToView(recyclerView, layout.findViewByPosition(newFocusPosition), smooth)
    }

    fun setSmoothScroller(smoothScroller: RecyclerView.SmoothScroller) {
        if (smoothScroller.isRunning && smoothScroller is GridLinearSmoothScroller) {
            currentSmoothScroller = smoothScroller
        } else {
            currentSmoothScroller = null
        }
    }

    /**
     * When starting a new SmoothScroller or scrolling to a different location,
     * we don't need the current SmoothScroller.onStopInternal() doing the scroll work.
     */
    fun cancelSmoothScroller() {
        currentSmoothScroller?.skipOnStopInternal = true
    }

    fun scrollToPosition(
        recyclerView: RecyclerView,
        position: Int,
        subPosition: Int,
        smooth: Boolean
    ) {
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        val view = layout.findViewByPosition(position)
        /**
         * scrollToView can only be called if the item position is still valid
         * and no layout request was made.
         * If there's a pending layout request, defer the selection to the next layout pass.
         */
        if (!layout.isSmoothScrolling
            && !recyclerView.isLayoutRequested
            && view != null
            && layout.getAdapterPositionOfView(view) == position
        ) {
            isSelectionInProgress = true
            val subPositionView = findSubPositionView(recyclerView, view, subPosition)
            if (subPositionView == null && subPosition != 0) {
                Log.w(
                    DpadRecyclerView.TAG,
                    "Subposition $position doesn't exist for position $position," +
                            "scroll instead started for subposition 0"
                )
            }
            scrollToView(recyclerView, view, subPositionView, smooth)
            isSelectionInProgress = false
        } else {
            if (smooth && !recyclerView.isLayoutRequested) {
                focusManager.position = position
                focusManager.subPosition = subPosition
                focusManager.positionOffset = Int.MIN_VALUE
                if (!layout.hasFinishedFirstLayout()) {
                    return
                }
                startPositionSmoothScroller(recyclerView, position)
                if (position != focusManager.position) {
                    // gets cropped by adapter size
                    focusManager.position = position
                    focusManager.subPosition = 0
                }
            } else {
                // stopScroll might change focusPosition, so call it before assigning value to
                // focusPosition
                if (layout.isSmoothScrolling) {
                    currentSmoothScroller?.skipOnStopInternal = true
                    recyclerView.stopScroll()
                }
                if (!recyclerView.isLayoutRequested
                    && view != null
                    && layout.getAdapterPositionOfView(view) == position
                ) {
                    isSelectionInProgress = true
                    scrollToView(recyclerView, view, smooth)
                    isSelectionInProgress = false
                } else {
                    /**
                     * The View doesn't exist at this position,
                     * so ask the LayoutManager to scroll immediately to it
                     * and request layout to update the alignment
                     */
                    focusManager.position = position
                    focusManager.subPosition = subPosition
                    focusManager.positionOffset = Int.MIN_VALUE
                    isAligningFocusedPosition = true
                    layout.scrollToPositionWithOffset(position, 0)
                }
            }
        }
    }

    private fun findSubPositionView(
        recyclerView: RecyclerView,
        view: View,
        subPosition: Int
    ): View? {
        val viewHolder = recyclerView.getChildViewHolder(view)
        val childAlignments = (viewHolder as? DpadViewHolder)?.getAlignments() ?: return null
        if (subPosition >= childAlignments.size) {
            return null
        }
        val subPositionViewId = childAlignments[subPosition].getFocusViewId()
        return view.findViewById(subPositionViewId)
    }

    private fun scrollToView(recyclerView: RecyclerView, view: View?, smooth: Boolean) {
        scrollToView(recyclerView, view, subPositionView = view?.findFocus(), smooth)
    }

    fun scrollToView(
        recyclerView: RecyclerView,
        viewHolderView: View?,
        subPositionView: View?,
        smooth: Boolean
    ) {
        val newFocusPosition = if (viewHolderView == null) {
            RecyclerView.NO_POSITION
        } else {
            layout.getAdapterPositionOfView(viewHolderView)
        }
        val newSubFocusPosition = scrollAlignment.findSubPositionOfChild(
            recyclerView, viewHolderView, subPositionView
        )
        if (newFocusPosition != focusManager.position
            || newSubFocusPosition != focusManager.subPosition
        ) {
            focusManager.position = newFocusPosition
            focusManager.subPosition = newSubFocusPosition
            if (!layout.isInLayoutStage()) {
                layout.dispatchViewHolderSelected()
            } else {
                pendingSelectionUpdate = true
            }
            if (childDrawingOrderEnabled) {
                recyclerView.invalidate()
            }
        }
        if (viewHolderView == null) {
            return
        }
        if (subPositionView != null && !subPositionView.hasFocus() && recyclerView.hasFocus()) {
            subPositionView.requestFocus()
        } else if (!viewHolderView.hasFocus() && recyclerView.hasFocus()) {
            viewHolderView.requestFocus()
        }

        scrollAlignment.updateScroll(recyclerView, viewHolderView, subPositionView)
            ?.let { scrollOffset ->
                scroll(
                    recyclerView,
                    scrollOffset,
                    smooth
                )
            }
    }

    private fun scroll(
        recyclerView: RecyclerView,
        offset: Int,
        smooth: Boolean
    ) {
        if (layout.isInLayoutStage()) {
            scroll(recyclerView, offset)
            return
        }
        var scrollX = 0
        var scrollY = 0
        if (layout.isHorizontal()) {
            scrollX = offset
        } else {
            scrollY = offset
        }
        if (smooth) {
            recyclerView.smoothScrollBy(scrollX, scrollY)
        } else {
            recyclerView.scrollBy(scrollX, scrollY)
        }
    }

    private fun calculateScrollAmount(offset: Int): Int {
        var scrollOffset = offset
        // We apply the cap of maxScroll/minScroll to the delta, except for one case:
        // 1. During onLayoutChildren(), it may compensate the remaining scroll delta,
        //    we should honor the request regardless if it goes over minScroll / maxScroll.
        //    (see b/64931938 testScrollAndRemove and testScrollAndRemoveSample1)
        if (!layout.isInLayoutStage()) {
            scrollOffset = scrollAlignment.getCappedScroll(scrollOffset)
        }
        return scrollOffset
    }

    fun scroll(recyclerView: RecyclerView, dx: Int): Int {
        val offset = calculateScrollAmount(dx)
        if (offset == 0) {
            return 0
        }
        offsetChildren(-offset)
        recyclerView.invalidate()
        return offset
    }

    private fun startPositionSmoothScroller(recyclerView: RecyclerView, position: Int) {
        val linearSmoothScroller = object : GridLinearSmoothScroller(recyclerView) {
            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                if (childCount == 0) {
                    return null
                }
                val firstChild = layout.getChildAt(0) ?: return null
                val firstChildPos = layout.getPosition(firstChild)
                val isStart = if (layout.isRTL() && layout.isHorizontal()) {
                    targetPosition > firstChildPos
                } else {
                    targetPosition < firstChildPos
                }
                val direction = if (isStart) {
                    -1f
                } else {
                    1f
                }
                return if (layout.isHorizontal()) {
                    PointF(direction, 0f)
                } else {
                    PointF(0f, direction)
                }
            }
        }
        linearSmoothScroller.targetPosition = position
        layout.startSmoothScroll(linearSmoothScroller)
    }

    private fun offsetChildren(offset: Int) {
        val childCount = layout.childCount
        if (layout.isVertical()) {
            for (i in 0 until childCount) {
                layout.getChildAt(i)?.offsetTopAndBottom(offset)
            }
        } else {
            for (i in 0 until childCount) {
                layout.getChildAt(i)?.offsetLeftAndRight(offset)
            }
        }
    }

    abstract inner class GridLinearSmoothScroller(private val recyclerView: RecyclerView) :
        LinearSmoothScroller(recyclerView.context) {

        var skipOnStopInternal = false

        override fun onStop() {
            super.onStop()
            if (!skipOnStopInternal) {
                onStopInternal()
            }
            if (currentSmoothScroller === this) {
                currentSmoothScroller = null
            }
        }

        open fun onStopInternal() {
            // onTargetFound() may not be called if we hit the "wall" first or get cancelled.
            val targetView = findViewByPosition(targetPosition)
            if (targetView == null) {
                if (targetPosition >= 0) {
                    // if smooth scroller is stopped without target,
                    // immediately jump to the target position.
                    scrollToPosition(recyclerView, targetPosition, 0, false)
                }
                return
            }
            if (focusManager.position != targetPosition) {
                // This should not happen since we cropped value in startPositionSmoothScroller()
                focusManager.position = targetPosition
            }
            if (layout.hasFocus()) {
                isSelectionInProgress = true
                targetView.requestFocus()
                isSelectionInProgress = false
            }
            layout.dispatchViewHolderSelected()
            layout.dispatchViewHolderSelectedAndAligned()
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
            return super.calculateSpeedPerPixel(displayMetrics) * smoothScrollSpeedFactor
        }

        override fun onTargetFound(targetView: View, state: RecyclerView.State, action: Action) {
            scrollAlignment.updateScroll(recyclerView, targetView, null)?.let { offset ->
                var dx = 0
                var dy = 0
                if (layout.isHorizontal()) {
                    dx = offset
                } else {
                    dy = offset
                }
                val distance = sqrt((dx * dx + dy * dy).toDouble()).toInt()
                val time = calculateTimeForDeceleration(distance)
                action.update(dx, dy, time, mDecelerateInterpolator)
            }
        }
    }

}
