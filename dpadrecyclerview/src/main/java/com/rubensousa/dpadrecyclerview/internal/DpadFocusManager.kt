package com.rubensousa.dpadrecyclerview.internal

import android.view.FocusFinder
import android.view.View
import android.view.ViewGroup
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadGridLayoutManager
import timber.log.Timber

internal class DpadFocusManager(
    private val layout: DpadGridLayoutManager
) {

    /**
     * Allow dpad events to navigate out the front of the View at position 0
     * in the same direction of the orientation.
     * For horizontal orientation, this means navigating out from the start of the first view
     * For vertical orientation, this means navigation out from the top of the first view
     */
    var focusOutFront = false

    /**
     * Allow dpad events to navigate outside the View at the last position
     * in the same direction of the orientation.
     * For horizontal orientation, this means navigating out from the end of the last view
     * For vertical orientation, this means navigation out from the bottom of the last view
     */
    var focusOutBack = false

    /**
     * Allow dpad events to navigate outside the View at the last position
     * in the opposite direction of the orientation
     * For horizontal orientation, this means navigating out from the top of the last view
     * For vertical orientation, this means navigation out from the start of the last view
     */
    var focusOutOppositeFront = true

    /**
     * Allow dpad events to navigate outside the View at the last position
     * in the opposite direction of the orientation
     * For horizontal orientation, this means navigating out from the bottom of the last view
     * For vertical orientation, this means navigation out from the end of the last view
     */
    var focusOutOppositeBack = true

    /**
     * If true, when focus is at a given edge of the grid,
     * it will be redirected to the opposite edge
     */
    var circularFocusEnabled = false

    var position = RecyclerView.NO_POSITION
    var subPosition = 0

    /**
     * The offset to be applied to [position], due to adapter change, on the next layout pass.
     * Set to Int.MIN_VALUE means we should stop adding delta to [position] until next layout cycle.
     * TODO:  This is somewhat duplication of RecyclerView getOldPosition() which is
     * unfortunately cleared after prelayout.
     */
    var positionOffset = 0

    // key - row / value - previous focused span
    private val focusedSpans = LinkedHashMap<Int, Int>()

    fun onInterceptFocusSearch(recyclerView: RecyclerView, focused: View, direction: Int): View? {
        val focusFinder = FocusFinder.getInstance()
        var result: View? = null
        val movement: ScrollMovement? = calculateMovement(
            layout.isHorizontal(), layout.isRTL(), direction
        )
        if (direction == View.FOCUS_FORWARD || direction == View.FOCUS_BACKWARD) {
            // convert direction to absolute direction and see if we have a view there and if not
            // tell LayoutManager to add if it can.
            if (layout.canScrollVertically()) {
                val absDir = if (direction == View.FOCUS_FORWARD) {
                    View.FOCUS_DOWN
                } else {
                    View.FOCUS_UP
                }
                result = focusFinder.findNextFocus(recyclerView, focused, absDir)
            }
            if (layout.canScrollHorizontally()) {
                val absDir = if ((direction == View.FOCUS_FORWARD) xor layout.isRTL()) {
                    View.FOCUS_RIGHT
                } else {
                    View.FOCUS_LEFT
                }
                result = focusFinder.findNextFocus(recyclerView, focused, absDir)
            }
        } else {
            if (circularFocusEnabled && movement != null) {
                result = focusCircular(movement)
            }
            if (result == null) {
                result = focusFinder.findNextFocus(recyclerView, focused, direction)
            }
        }
        if (result != null) {
            return result
        }

        if (recyclerView.descendantFocusability == ViewGroup.FOCUS_BLOCK_DESCENDANTS) {
            return recyclerView.parent.focusSearch(focused, direction)
        }

        val isScrolling = recyclerView.scrollState != RecyclerView.SCROLL_STATE_IDLE
        when (movement) {
            ScrollMovement.NEXT_ROW -> {
                if (isScrolling || !focusOutBack) {
                    result = focused
                }
                if (!layout.hasCreatedLastItem(recyclerView)) {
                    result = focused
                }
            }
            ScrollMovement.PREVIOUS_ROW -> {
                if (isScrolling || !focusOutFront) {
                    result = focused
                }
                if (!layout.hasCreatedFirstItem(recyclerView)) {
                    result = focused
                }
            }
            ScrollMovement.NEXT_COLUMN -> {
                if (isScrolling || !focusOutOppositeBack) {
                    result = focused
                }
            }
            ScrollMovement.PREVIOUS_COLUMN -> {
                if (isScrolling || !focusOutOppositeFront) {
                    result = focused
                }
            }
            else -> {}
        }
        if (result != null) {
            return result
        }
        result = recyclerView.parent?.focusSearch(focused, direction)
        return result ?: focused
    }

    fun onRequestChildFocus(
        parent: RecyclerView,
        state: RecyclerView.State,
        child: View,
        focused: View?
    ): Boolean {
        val adapterPosition = layout.getAdapterPositionOfView(child)
        if (adapterPosition == RecyclerView.NO_POSITION) {
            // This is could be the last view in DISAPPEARING animation
            return true
        }
        if (!layout.isInLayoutStage() && !layout.isSelectionInProgress()) {
            saveSpanFocus(adapterPosition)
            layout.scrollToView(parent, child, focused, smooth = true)
        } else {
            Timber.d("Skipping scrollToView since selection is already in process")
        }
        return true
    }

    private fun saveSpanFocus(newPosition: Int) {
        val previousPosition = position
        val previousSpanSize = layout.spanSizeLookup.getSpanSize(previousPosition)
        val newSpanSize = layout.spanSizeLookup.getSpanSize(newPosition)
        if (previousSpanSize != newSpanSize && previousSpanSize != layout.spanCount) {
            val row = layout.getRowIndex(previousPosition)
            focusedSpans[row] = previousPosition
        }
    }

    private fun getPreviousSpanFocus(row: Int): Int {
        return focusedSpans[row] ?: RecyclerView.NO_POSITION
    }

    fun onAddFocusables(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ): Boolean {
        if (recyclerView.hasFocus()) {
            addFocusablesWhenRecyclerHasFocus(recyclerView, views, direction, focusableMode)
            return true
        }
        val focusableCount = views.size
        val view = layout.findViewByPosition(position)
        view?.addFocusables(views, direction, focusableMode)
        // if still cannot find any, fall through and add itself
        if (views.size != focusableCount) {
            return true
        }
        if (recyclerView.isFocusable) {
            views.add(recyclerView)
        }
        return true
    }

    private fun addFocusablesWhenRecyclerHasFocus(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ) {
        val movement: ScrollMovement = calculateMovement(
            layout.isHorizontal(),
            layout.isRTL(),
            direction
        ) ?: return
        val focused = recyclerView.findFocus()
        // TODO Optimize
        val focusedChildIndex = layout.findImmediateChildIndex(focused)
        val focusedPosition = layout.getAdapterPositionOfChildAt(focusedChildIndex)
        // Even if focusedPos != NO_POSITION, findViewByPosition could return null if the view
        // is ignored or getLayoutPosition does not match the adapter position of focused view.
        val immediateFocusedChild: View? = if (focusedPosition == RecyclerView.NO_POSITION) {
            null
        } else {
            layout.findViewByPosition(focusedPosition)
        }
        // Add focusables of focused item.
        immediateFocusedChild?.addFocusables(views, direction, focusableMode)
        if (layout.childCount == 0) {
            // no grid information, or no child, bail out.
            return
        }
        if ((movement == ScrollMovement.NEXT_COLUMN || movement == ScrollMovement.PREVIOUS_COLUMN)
            && layout.spanCount <= 1
        ) {
            // For single row, cannot navigate to previous/next row.
            return
        }
        if (focusPreviousSpan(focusedPosition, movement, views, direction, focusableMode)) {
            return
        }
        // Add focusables of neighbor depending on the focus search direction.
        val focusedColumn = if (immediateFocusedChild != null) {
            layout.getColumnIndex(focusedPosition)
        } else {
            RecyclerView.NO_POSITION
        }

        val inc = if (movement == ScrollMovement.NEXT_ROW
            || movement == ScrollMovement.NEXT_COLUMN
        ) {
            1
        } else {
            -1
        }
        val loop_end = if (inc > 0) {
            layout.childCount - 1
        } else {
            0
        }
        val loop_start = if (focusedChildIndex == RecyclerView.NO_POSITION) {
            if (inc > 0) {
                0
            } else {
                layout.childCount - 1
            }
        } else {
            focusedChildIndex + inc
        }

        var i = loop_start
        while ((i <= loop_end && inc > 0) || (i >= loop_end && inc < 0)) {
            val child = layout.getChildAt(i)
            if (child == null || !isViewFocusable(child)) {
                i += inc
                continue
            }
            // if there wasn't any focused item, add the very first focusable
            // items and stop.
            if (immediateFocusedChild == null) {
                child.addFocusables(views, direction, focusableMode)
                i += inc
                continue
            }
            val position = layout.getAdapterPositionOfChildAt(i)
            val childColumn = layout.getColumnIndex(position)
            if (movement == ScrollMovement.NEXT_ROW) {
                // Add first focusable item on the same row
                if (position > focusedPosition) {
                    child.addFocusables(views, direction, focusableMode)
                }
            } else if (movement == ScrollMovement.PREVIOUS_ROW) {
                // Add first focusable item on the same row
                if (position < focusedPosition) {
                    child.addFocusables(views, direction, focusableMode)
                }
            } else if (movement == ScrollMovement.NEXT_COLUMN) {
                // Add all focusable items after this item whose row index is bigger
                if (childColumn == focusedColumn) {
                    i += inc
                    continue
                } else if (childColumn < focusedColumn) {
                    break
                }
                child.addFocusables(views, direction, focusableMode)
            } else if (movement == ScrollMovement.PREVIOUS_COLUMN) {
                // Add all focusable items before this item whose column index is smaller
                if (childColumn == focusedColumn) {
                    i += inc
                    continue
                } else if (childColumn > focusedColumn) {
                    break
                }
                child.addFocusables(views, direction, focusableMode)
            }
            i += inc
        }
    }

    private fun focusPreviousSpan(
        focusedPosition: Int,
        movement: ScrollMovement,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ): Boolean {
        if (layout.spanCount == 1
            || (movement != ScrollMovement.PREVIOUS_ROW && movement != ScrollMovement.NEXT_ROW)
        ) {
            return false
        }
        val row = layout.getRowIndex(focusedPosition)
        val nextRow = if (movement == ScrollMovement.NEXT_ROW) {
            row + 1
        } else {
            row - 1
        }
        var previousPosition = getPreviousSpanFocus(nextRow)
        if (previousPosition == RecyclerView.NO_POSITION || previousPosition >= layout.itemCount) {
            if (layout.spanSizeLookup.getSpanSize(focusedPosition) == layout.spanCount) {
                previousPosition = if (movement == ScrollMovement.NEXT_ROW) {
                    focusedPosition + 1
                } else {
                    focusedPosition - 1
                }
            } else {
                return false
            }
        }
        val newView = layout.findViewByPosition(previousPosition) ?: return false
        newView.addFocusables(views, direction, focusableMode)
        focusedSpans.remove(nextRow)
        return true
    }

    private fun focusCircular(movement: ScrollMovement): View? {
        if (movement != ScrollMovement.PREVIOUS_COLUMN && movement != ScrollMovement.NEXT_COLUMN) {
            return null
        }
        val firstColumnIndex = layout.getColumnIndex(position)
        var lastColumnIndex = firstColumnIndex + layout.spanSizeLookup.getSpanSize(position) - 1
        // Do nothing for the middle columns or for items that take the entire span count
        if (layout.spanSizeLookup.getSpanSize(position) == layout.spanCount
            || (firstColumnIndex > 0 && lastColumnIndex < layout.spanCount - 1)
        ) {
            return null
        }

        var circularPosition: Int
        if (movement == ScrollMovement.NEXT_COLUMN && lastColumnIndex == layout.spanCount - 1) {
            val row = layout.getRowIndex(position)
            circularPosition = position - layout.spanCount + 1
            while (circularPosition <= position - 1) {
                val currentRow = layout.getRowIndex(circularPosition)
                val currentColumn = layout.getColumnIndex(circularPosition)
                if (currentRow == row && currentColumn == 0) {
                    break
                }
                circularPosition++
            }
        } else if (movement == ScrollMovement.PREVIOUS_COLUMN && firstColumnIndex == 0) {
            val row = layout.getRowIndex(position)
            circularPosition = position + layout.spanCount - 1
            while (circularPosition >= position + 1) {
                lastColumnIndex = layout.getColumnIndex(circularPosition) +
                        layout.spanSizeLookup.getSpanSize(circularPosition) - 1
                val currentRow = layout.getRowIndex(circularPosition)
                if (currentRow == row && lastColumnIndex == layout.spanCount - 1) {
                    break
                }
                circularPosition--
            }
        } else {
            return null
        }
        return layout.findViewByPosition(circularPosition)
    }

    fun onItemsAdded(positionStart: Int, itemCount: Int, firstVisiblePosition: Int) {
        if (position != RecyclerView.NO_POSITION
            && firstVisiblePosition >= 0
            && positionOffset != Int.MIN_VALUE
        ) {
            val finalPosition = position + positionOffset
            if (positionStart <= finalPosition) {
                positionOffset += itemCount
            }
        }
    }

    fun onItemsChanged() {
        positionOffset = 0
    }

    fun onItemsRemoved(positionStart: Int, itemCount: Int, firstVisiblePosition: Int) {
        if (position != RecyclerView.NO_POSITION
            && firstVisiblePosition >= 0
            && positionOffset != Int.MIN_VALUE
        ) {
            val finalPosition = position + positionOffset
            if (positionStart <= finalPosition) {
                if (positionStart + itemCount > finalPosition) {
                    // stop updating offset after the focused item was removed
                    positionOffset += positionStart - finalPosition
                    position += positionOffset
                    positionOffset = Int.MIN_VALUE
                } else {
                    positionOffset -= itemCount
                }
            }
        }
    }

    fun onItemsMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
        if (position != RecyclerView.NO_POSITION && positionOffset != Int.MIN_VALUE) {
            val finalPosition = position + positionOffset
            if (fromPosition <= finalPosition && finalPosition < fromPosition + itemCount) {
                // moved items include focused position
                positionOffset += toPosition - fromPosition
            } else if (fromPosition < finalPosition && toPosition > finalPosition - itemCount) {
                // move items before focused position to after focused position
                positionOffset -= itemCount
            } else if (fromPosition > finalPosition && toPosition < finalPosition) {
                // move items after focused position to before focused position
                positionOffset += itemCount
            }
        }
    }

    fun onAdapterChanged(oldAdapter: RecyclerView.Adapter<*>?) {
        if (oldAdapter != null) {
            position = RecyclerView.NO_POSITION
            positionOffset = 0
        }
    }

    fun consumePendingFocusChanges(): Boolean {
        var consumed = false
        if (position != RecyclerView.NO_POSITION && positionOffset != Int.MIN_VALUE) {
            position += positionOffset
            consumed = true
        }
        positionOffset = 0
        return consumed
    }

    private fun isViewFocusable(view: View): Boolean {
        return view.visibility == View.VISIBLE && view.hasFocusable() && view.isFocusable
    }

    @VisibleForTesting
    fun calculateMovement(isHorizontal: Boolean, isRTL: Boolean, direction: Int): ScrollMovement? {
        return if (isHorizontal) {
            when (direction) {
                View.FOCUS_LEFT -> {
                    if (isRTL) ScrollMovement.NEXT_ROW else ScrollMovement.PREVIOUS_ROW
                }
                View.FOCUS_RIGHT -> {
                    if (isRTL) ScrollMovement.PREVIOUS_ROW else ScrollMovement.NEXT_ROW
                }
                View.FOCUS_UP -> ScrollMovement.PREVIOUS_COLUMN
                View.FOCUS_DOWN -> ScrollMovement.NEXT_COLUMN
                else -> null
            }
        } else {
            when (direction) {
                View.FOCUS_LEFT -> {
                    if (isRTL) ScrollMovement.NEXT_COLUMN else ScrollMovement.PREVIOUS_COLUMN
                }
                View.FOCUS_RIGHT -> {
                    if (isRTL) ScrollMovement.PREVIOUS_COLUMN else ScrollMovement.NEXT_COLUMN
                }
                View.FOCUS_UP -> ScrollMovement.PREVIOUS_ROW
                View.FOCUS_DOWN -> ScrollMovement.NEXT_ROW
                else -> null
            }
        }
    }

    @VisibleForTesting
    enum class ScrollMovement {
        PREVIOUS_ROW,
        NEXT_ROW,
        PREVIOUS_COLUMN,
        NEXT_COLUMN
    }


}
