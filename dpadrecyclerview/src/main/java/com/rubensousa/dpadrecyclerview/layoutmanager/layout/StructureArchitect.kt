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
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.layoutmanager.recycling.ViewRecycler

/**
 * General layout algorithm:
 * 1. Layout the view at the selected position (pivot) and align it to the keyline
 * 2. Starting from the bottom/end of the selected view,
 * fill towards the top/start until there's no more space
 * 3. Starting from the top/end of the selected view,
 * fill towards the top/start until there's no more space
 */
internal abstract class StructureArchitect(
    protected val layoutManager: LayoutManager,
    protected val layoutInfo: LayoutInfo,
    private val viewRecycler: ViewRecycler,
    private val onChildLayoutListener: OnChildLayoutListener
) {

    companion object {
        const val TAG = "StructureArchitect"
    }

    private val viewBounds = Rect()

    /**
     * Places the pivot in the correct layout position and returns its bounds via [bounds]
     */
    protected abstract fun addPivot(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    )

    /**
     * Places [view] at the end of the current layout and returns its bounds via [bounds]
     * @return layout space consumed by this view
     */
    protected abstract fun appendView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int

    /**
     * Places [view] at the start of the current layout and returns its bounds via [bounds]
     * @return layout space consumed by this view
     */
    protected abstract fun prependView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int

    fun layoutPivot(
        layoutState: LayoutState,
        recycler: Recycler,
        position: Int,
        state: State
    ): View {
        val view = recycler.getViewForPosition(position)
        layoutManager.addView(view)
        onChildLayoutListener.onChildCreated(view, state)
        layoutManager.measureChildWithMargins(view, 0, 0)

        // Place the pivot in its keyline position
        addPivot(view, position, viewBounds, layoutState)

        // Trigger a new layout pass for the pivot view
        performLayout(view, viewBounds)

        Log.i(TAG, "Laid pivot ${layoutInfo.getLayoutPositionOf(view)} with bounds: $viewBounds")
        viewBounds.setEmpty()

        // Move the pivot by the remaining scroll
        // so that it slides in correctly after the layout is done
        offsetBy(-layoutInfo.getRemainingScroll(state), layoutState)

        onChildLayoutListener.onChildLaidOut(view, state)
        return view
    }

    fun layoutEdge(
        layoutState: LayoutState,
        recycler: RecyclerView.Recycler,
        state: State
    ) {
        var remainingSpace = layoutState.fillSpace
        // Start by recycling children that moved out of bounds
        viewRecycler.recycleByLayoutState(recycler, layoutState)

        val isAppending = layoutState.isLayingOutEnd()

        // Keep appending or prepending views until we run out of fill space or items
        while (shouldContinueLayout(remainingSpace, layoutState, state)) {
            val currentPosition = layoutState.currentPosition
            val view = layoutState.getNextView(recycler) ?: break
            if (!layoutState.isUsingScrap()) {
                if (isAppending) {
                    layoutManager.addView(view)
                } else {
                    layoutManager.addView(view, 0)
                }
            } else if (isAppending) {
                layoutManager.addDisappearingView(view)
            } else {
                layoutManager.addDisappearingView(view, 0)
            }

            onChildLayoutListener.onChildCreated(view, state)
            layoutManager.measureChildWithMargins(view, 0, 0)

            val consumedSpace = if (isAppending) {
                appendView(view, currentPosition, viewBounds, layoutState)
            } else {
                prependView(view, currentPosition, viewBounds, layoutState)
            }

            performLayout(view, viewBounds)

            Log.i(
                TAG,
                "Laid out view ${layoutInfo.getLayoutPositionOf(view)} with bounds: $viewBounds"
            )
            viewBounds.setEmpty()
            remainingSpace -= consumedSpace
            viewRecycler.recycleByLayoutState(recycler, layoutState)
            onChildLayoutListener.onChildLaidOut(view, state)
        }

        // Recycle children in the opposite direction of layout
        // to be sure we don't have any extra views
        viewRecycler.recycleByLayoutState(recycler, layoutState)
    }

    fun removeInvisibleViews(recycler: Recycler, layoutState: LayoutState) {
        layoutState.setRecyclingEnabled(true)
        viewRecycler.recycleFromStart(recycler, layoutState)
        viewRecycler.recycleFromEnd(recycler, layoutState)
    }

    fun offsetBy(offset: Int, layoutState: LayoutState) {
        layoutInfo.orientationHelper.offsetChildren(-offset)
        layoutState.offsetWindow(-offset)
    }

    fun logChildren() {
        Log.i(TAG, "Children laid out:")
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)!!
            val position = layoutManager.getPosition(child)
            val left = layoutManager.getDecoratedLeft(child)
            val top = layoutManager.getDecoratedTop(child)
            val right = layoutManager.getDecoratedRight(child)
            val bottom = layoutManager.getDecoratedBottom(child)
            Log.i(TAG, "View $position: [$left, $top, $right, $bottom]")
        }
    }

    private fun performLayout(view: View, bounds: Rect) {
        layoutManager.layoutDecoratedWithMargins(
            view, bounds.left, bounds.top, bounds.right, bounds.bottom
        )
    }

    private fun shouldContinueLayout(
        remainingSpace: Int,
        layoutState: LayoutState,
        state: State
    ): Boolean {
        return layoutState.hasMoreItems(state) && (remainingSpace > 0 || layoutState.isInfinite())
    }

}
