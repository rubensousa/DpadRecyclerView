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

import android.view.FocusFinder
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.internal.ScrollMovement
import com.rubensousa.dpadrecyclerview.internal.ScrollMovementCalculator

internal class TvLayoutFocusFinder(
    private val layout: LayoutManager,
    private val configuration: TvLayoutConfiguration,
    private val scroller: TvLayoutScroller,
    private val layoutInfo: TvLayoutInfo,
    private val selectionState: TvSelectionState
) {

    private var dpadRecyclerView: RecyclerView? = null

    // key - row / value - previous focused span
    private val focusedSpans = LinkedHashMap<Int, Int>()

    fun setRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
    }

    fun onRequestChildFocus(
        parent: RecyclerView,
        state: RecyclerView.State,
        child: View,
        focused: View?
    ): Boolean {
        if (configuration.isFocusSearchDisabled) {
            return true
        }
        val newViewPosition = layoutInfo.getAdapterPositionOfView(child)
        // This could be the last view in DISAPPEARING animation, so ignore immediately
        if (newViewPosition == RecyclerView.NO_POSITION) {
            return true
        }
        val canScrollToView = !scroller.isSelectionInProgress
        if (canScrollToView) {
            saveSpanFocus(newViewPosition)
            scroller.scrollToView(parent, child, focused, configuration.isSmoothFocusChangesEnabled)
        }
        return true
    }

    private fun saveSpanFocus(newPosition: Int) {
        val previousPosition = selectionState.position
        val previousSpanSize = layoutInfo.getSpanSize(previousPosition)
        val newSpanSize = layoutInfo.getSpanSize(newPosition)
        if (previousSpanSize != newSpanSize && previousSpanSize != configuration.spanCount) {
            val row = layoutInfo.getRowIndex(previousPosition)
            focusedSpans[row] = previousPosition
        }
    }

    // TODO
    fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        val recyclerView = dpadRecyclerView ?: return focused
        val focusFinder = FocusFinder.getInstance()
        var result: View? = focusFinder.findNextFocus(recyclerView, focused, direction)
        if (result != null) {
            return result
        }
        result = recyclerView.parent?.focusSearch(focused, direction)
        return result ?: focused
    }

    fun onAddFocusables(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ): Boolean {
        if (configuration.isFocusSearchDisabled) {
            return true
        }
        if (recyclerView.hasFocus()) {
            addFocusablesWhenRecyclerHasFocus(recyclerView, views, direction, focusableMode)
            return true
        }
        val focusableCount = views.size
        val view = layout.findViewByPosition(selectionState.position)
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
        val movement = ScrollMovementCalculator.calculate(
            configuration.isHorizontal(),
            layoutInfo.isRTL(),
            direction
        ) ?: return
        val focused = recyclerView.findFocus()
        // TODO Optimize
        val focusedChildIndex = layoutInfo.findImmediateChildIndex(focused)
        val focusedPosition = layoutInfo.getAdapterPositionOfChildAt(focusedChildIndex)
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
            && configuration.spanCount <= 1
        ) {
            // For single row, cannot navigate to previous/next row.
            return
        }
        if (focusPreviousSpan(focusedPosition, movement, views, direction, focusableMode)) {
            return
        }
        // Add focusables of neighbor depending on the focus search direction.
        val focusedColumn = if (immediateFocusedChild != null) {
            layoutInfo.getColumnIndex(focusedPosition)
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
            val position = layoutInfo.getAdapterPositionOfChildAt(i)
            val childColumn = layoutInfo.getColumnIndex(position)
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
        if (configuration.spanCount == 1
            || (movement != ScrollMovement.PREVIOUS_ROW && movement != ScrollMovement.NEXT_ROW)
        ) {
            return false
        }
        val row = layoutInfo.getRowIndex(focusedPosition)
        val nextRow = if (movement == ScrollMovement.NEXT_ROW) {
            row + 1
        } else {
            row - 1
        }
        var previousPosition = getPreviousSpanFocus(nextRow)
        if (previousPosition == RecyclerView.NO_POSITION || previousPosition >= layout.itemCount) {
            if (layoutInfo.getSpanSize(focusedPosition) == configuration.spanCount) {
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

    private fun getPreviousSpanFocus(row: Int): Int {
        return focusedSpans[row] ?: RecyclerView.NO_POSITION
    }

    private fun isViewFocusable(view: View): Boolean {
        return view.visibility == View.VISIBLE && view.hasFocusable() && view.isFocusable
    }

}
