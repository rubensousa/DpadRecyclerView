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

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.RecyclerView.Recycler
import androidx.recyclerview.widget.RecyclerView.State
import com.rubensousa.dpadrecyclerview.BuildConfig
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import kotlin.math.max
import kotlin.math.min

internal abstract class StructureEngineer(
    protected val layoutManager: LayoutManager,
    protected val layoutInfo: LayoutInfo,
    protected val layoutAlignment: LayoutAlignment,
    private val onChildLayoutListener: OnChildLayoutListener
) {

    companion object {
        const val TAG = "StructureEngineer"
        private val DEBUG = BuildConfig.DEBUG
    }

    private var viewBounds = ViewBounds()

    /**
     * Used to update any internal layout state before onLayoutChildren starts its job
     */
    open fun init(layoutState: LayoutState, recyclerViewState: State) {

    }

    protected abstract fun getArchitect(): LayoutArchitect

    protected abstract fun getViewRecycler(): ViewRecycler

    /**
     * Places the pivot in the correct layout position and returns its bounds via [bounds]
     */
    protected abstract fun placePivot(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutState: LayoutState
    )

    /**
     * Places [view] at the end of the current layout and returns its bounds via [bounds]
     * @return layout space consumed by this view
     */
    protected abstract fun appendView(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutState: LayoutState
    ): Int

    /**
     * Places [view] at the start of the current layout and returns its bounds via [bounds]
     * @return layout space consumed by this view
     */
    protected abstract fun prependView(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutState: LayoutState
    ): Int


    /**
     * Make sure all children are in their initial positions before the real layout pass.
     * Also layout more views if needed so that they correctly appear in the next pass
     * instead of fading in.
     */
    fun prelayout(
        pivotPosition: Int,
        layoutState: LayoutState,
        recycler: Recycler,
        recyclerViewState: State
    ) {
        val childCount = layoutInfo.getChildCount()
        val firstView = layoutInfo.getChildAt(0) ?: return
        val lastView = layoutInfo.getChildAt(childCount - 1) ?: return
        val firstPosition = layoutInfo.getOldPositionOf(firstView)
        val lastPosition = layoutInfo.getOldPositionOf(lastView)
        var startOffset = Int.MAX_VALUE
        var endOffset = Int.MIN_VALUE

        for (i in 0 until childCount) {
            val view = layoutManager.getChildAt(i) ?: continue
            val viewHolder = layoutInfo.getChildViewHolder(view) ?: continue
            if (layoutInfo.didChildStateChange(
                    viewHolder, pivotPosition, firstPosition, lastPosition
                )
            ) {
                startOffset = min(startOffset, layoutInfo.getDecoratedStart(view))
                endOffset = max(endOffset, layoutInfo.getDecoratedEnd(view))
            }
        }

        val extraLayoutSpace = max(0, endOffset - startOffset)
        val architect = getArchitect()
        architect.updateForStartPreLayout(
            layoutState, extraLayoutSpace, firstPosition, firstView
        )
        layoutEdge(layoutState, recycler, recyclerViewState)

        architect.updateForEndPreLayout(
            layoutState, extraLayoutSpace, lastPosition, lastView
        )
        layoutEdge(layoutState, recycler, recyclerViewState)
    }

    fun layout(
        pivotPosition: Int,
        layoutState: LayoutState,
        recycler: Recycler,
        recyclerViewState: State
    ) {
        // Start by detaching all existing views.
        // Views not attached again will be animated out if we're running predictive animations
        layoutManager.detachAndScrapAttachedViews(recycler)

        val architect = getArchitect()
        val pivotView = layoutPivot(layoutState, recycler, pivotPosition, recyclerViewState)

        // Layout views before the pivot
        architect.updateLayoutStateBeforePivot(layoutState, pivotPosition)
        layoutEdge(layoutState, recycler, recyclerViewState)

        // Layout views after the pivot
        architect.updateLayoutStateAfterPivot(layoutState, pivotPosition)
        layoutEdge(layoutState, recycler, recyclerViewState)

        layoutForPredictiveAnimations(architect, layoutState, recycler, recyclerViewState)

        // Now that all views are laid out, make sure the pivot is still in the correct position
        alignPivot(pivotView, layoutState, recycler, recyclerViewState)

        // Layout extra space if user requested it
        layoutExtraSpace(architect, layoutState, recycler, recyclerViewState)

        // We might have views we no longer need after aligning the pivot,
        // so recycle them if we're not running animations
        if (!recyclerViewState.willRunSimpleAnimations()
            && !recyclerViewState.willRunPredictiveAnimations()
        ) {
            removeInvisibleViews(recycler, layoutState)
        }
    }

    fun scrollBy(
        offset: Int,
        layoutState: LayoutState,
        recycler: Recycler,
        recyclerViewState: State
    ) {
        val architect = getArchitect()
        // Offset views immediately
        offsetChildren(-offset, layoutState)

        // Now layout the next views and recycle the ones we don't need along the way
        architect.updateLayoutStateForScroll(layoutState, recyclerViewState, offset)
        layoutEdge(layoutState, recycler, recyclerViewState)
    }

    private fun layoutPivot(
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
        placePivot(view, position, viewBounds, layoutState)

        // Trigger a new layout pass for the pivot view
        performLayout(view, viewBounds)

        if (DEBUG) {
            Log.i(TAG, "Laid pivot ${layoutInfo.getLayoutPositionOf(view)} at: $viewBounds")
        }

        viewBounds.setEmpty()

        onChildLayoutListener.onChildLaidOut(view, state)
        return view
    }

    private fun layoutEdge(
        layoutState: LayoutState,
        recycler: RecyclerView.Recycler,
        state: State
    ) {
        var remainingSpace = layoutState.fillSpace
        // Start by recycling children that moved out of bounds
        val viewRecycler = getViewRecycler()
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

            if (DEBUG) {
                Log.i(TAG, "Laid out view ${layoutInfo.getLayoutPositionOf(view)} at: $viewBounds")
            }
            viewBounds.setEmpty()

            // TODO Check if view was removed or changed before consuming this space
            remainingSpace -= consumedSpace
            // We don't need to recycle if we didn't consume any space
            if (consumedSpace > 0) {
                viewRecycler.recycleByLayoutState(recycler, layoutState)
            }
            onChildLayoutListener.onChildLaidOut(view, state)
        }

        // Recycle children in the opposite direction of layout
        // to be sure we don't have any extra views
        viewRecycler.recycleByLayoutState(recycler, layoutState)
    }

    private fun removeInvisibleViews(recycler: Recycler, layoutState: LayoutState) {
        layoutState.setRecyclingEnabled(true)
        getViewRecycler().apply {
            recycleFromStart(recycler, layoutState)
            recycleFromEnd(recycler, layoutState)
        }
    }

    open fun offsetChildren(offset: Int, layoutState: LayoutState) {
        layoutInfo.orientationHelper.offsetChildren(offset)
        layoutState.offsetWindow(offset)
        if (DEBUG) {
            if (layoutInfo.isVertical()) {
                viewBounds.offsetVertical(offset)
            } else {
                viewBounds.offsetHorizontal(offset)
            }
        }
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

    private fun alignPivot(
        pivotView: View,
        layoutState: LayoutState,
        recycler: Recycler, state:
        State
    ) {
        val scrollOffset = layoutAlignment.calculateScrollForAlignment(pivotView)
        val remainingScroll = layoutInfo.getRemainingScroll(state)
        // Offset all views by the existing remaining scroll so that they're still scrolled
        // to their final locations when RecyclerView resumes scrolling
        scrollBy(scrollOffset - remainingScroll, layoutState, recycler, state)
    }

    private fun layoutExtraSpace(
        architect: LayoutArchitect,
        layoutState: LayoutState,
        recycler: Recycler,
        recyclerViewState: State
    ) {
        architect.updateForExtraLayoutStart(layoutState, recyclerViewState)
        layoutEdge(layoutState, recycler, recyclerViewState)

        architect.updateForExtraLayoutEnd(layoutState, recyclerViewState)
        layoutEdge(layoutState, recycler, recyclerViewState)
    }

    /**
     * Layout scrap views that were not laid out in the previous step
     * to ensure animations work as expected.
     */
    private fun layoutForPredictiveAnimations(
        architect: LayoutArchitect,
        layoutState: LayoutState,
        recycler: Recycler,
        state: State
    ) {
        val firstChild = layoutManager.getChildAt(0)
        if (!state.willRunPredictiveAnimations() || firstChild == null || state.isPreLayout) {
            return
        }
        val scrapList = recycler.scrapList
        var scrapExtraStart = 0
        var scrapExtraEnd = 0
        val firstChildPosition = layoutInfo.getLayoutPositionOf(firstChild)
        for (i in 0 until scrapList.size) {
            val scrap = scrapList[i]
            if (layoutInfo.isRemoved(scrap)) {
                continue
            }
            val position = scrap.layoutPosition
            val direction = if (position < firstChildPosition != layoutState.reverseLayout) {
                LayoutState.LayoutDirection.START
            } else {
                LayoutState.LayoutDirection.END
            }
            // TODO This is not correct for grids
            if (direction == LayoutState.LayoutDirection.START) {
                scrapExtraStart += layoutInfo.getDecoratedSize(scrap.itemView)
            } else {
                scrapExtraEnd += layoutInfo.getDecoratedSize(scrap.itemView)
            }
        }

        layoutState.setExtraLayoutSpaceStart(scrapExtraStart)
        layoutState.setExtraLayoutSpaceEnd(scrapExtraEnd)
        layoutState.setScrap(recycler.scrapList)

        if (scrapExtraStart > 0) {
            val anchor = layoutInfo.getChildClosestToStart()
            if (anchor != null) {
                architect.updateLayoutStateForPredictiveStart(
                    layoutState,
                    layoutManager.getPosition(anchor)
                )
                layoutEdge(layoutState, recycler, state)
            }
        }

        if (scrapExtraEnd > 0) {
            val anchor = layoutInfo.getChildClosestToEnd()
            if (anchor != null) {
                architect.updateLayoutStateForPredictiveEnd(
                    layoutState,
                    layoutManager.getPosition(anchor)
                )
                layoutEdge(layoutState, recycler, state)
            }
        }

        layoutState.setScrap(null)
    }

    private fun performLayout(view: View, bounds: ViewBounds) {
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
