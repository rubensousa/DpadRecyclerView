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
    protected val onChildLayoutListener: OnChildLayoutListener
) {

    companion object {
        const val TAG = "StructureEngineer"

        @JvmStatic
        protected val DEBUG = BuildConfig.DEBUG
    }

    // Holds the bounds of the view to be laid out
    protected val viewBounds = ViewBounds()
    private val viewRecycler = ViewRecycler(layoutManager, layoutInfo)
    private val layoutResult = LayoutResult()

    /**
     * Used to update any internal layout state before onLayoutChildren starts its job
     */
    open fun init(layoutRequest: LayoutRequest, recyclerViewState: State) {

    }

    protected abstract fun getArchitect(): LayoutArchitect

    /**
     * Places the pivot in the correct layout position and returns its bounds via [bounds]
     */
    protected abstract fun placePivot(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    )

    /**
     * Places one or multiple views at the end/start of the current layout
     */
    protected abstract fun layoutBlock(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        layoutResult: LayoutResult
    )

    /**
     * Make sure all children are in their initial positions before the real layout pass.
     * Also layout more views if needed so that they correctly appear in the next pass
     * instead of fading in.
     */
    fun preLayoutChildren(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
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
            layoutRequest, extraLayoutSpace, firstPosition, firstView
        )
        layout(layoutRequest, recycler, recyclerViewState)

        architect.updateForEndPreLayout(
            layoutRequest, extraLayoutSpace, lastPosition, lastView
        )
        layout(layoutRequest, recycler, recyclerViewState)
    }

    fun layoutChildren(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        recyclerViewState: State
    ) {
        // Start by detaching all existing views.
        // Views not attached again will be animated out if we're running predictive animations
        layoutManager.detachAndScrapAttachedViews(recycler)

        val architect = getArchitect()
        val pivotView = layoutPivot(layoutRequest, recycler, pivotPosition)

        // Layout views before the pivot
        architect.updateLayoutStateBeforePivot(layoutRequest, pivotView, pivotPosition)
        layout(layoutRequest, recycler, recyclerViewState)

        // Layout views after the pivot
        architect.updateLayoutStateAfterPivot(layoutRequest, pivotView, pivotPosition)
        layout(layoutRequest, recycler, recyclerViewState)

        layoutForPredictiveAnimations(architect, layoutRequest, recycler, recyclerViewState)

        // Now that all views are laid out, make sure the pivot is still in the correct position
        alignPivot(pivotView, layoutRequest, recycler, recyclerViewState)

        // Layout extra space if user requested it
        layoutExtraSpace(architect, layoutRequest, recycler, recyclerViewState)

        // We might have views we no longer need after aligning the pivot,
        // so recycle them if we're not running animations
        if (!recyclerViewState.willRunSimpleAnimations()
            && !recyclerViewState.willRunPredictiveAnimations()
        ) {
            removeInvisibleViews(recycler, layoutRequest)
        }
    }

    fun scrollBy(
        offset: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        recyclerViewState: State
    ): Int {
        val architect = getArchitect()

        // Offset views immediately
        offsetChildren(-offset, layoutRequest)

        // Now layout the next views and recycle the ones we don't need along the way
        architect.updateLayoutStateForScroll(layoutRequest, recyclerViewState, offset)
        layout(layoutRequest, recycler, recyclerViewState)

        return offset
    }

    private fun layoutPivot(layoutRequest: LayoutRequest, recycler: Recycler, position: Int): View {
        val view = recycler.getViewForPosition(position)
        layoutManager.addView(view)
        onChildLayoutListener.onChildCreated(view)
        layoutManager.measureChildWithMargins(view, 0, 0)

        // Place the pivot in its keyline position
        placePivot(view, position, viewBounds, layoutRequest)

        // Trigger a new layout pass for the pivot view
        performLayout(view, viewBounds)

        if (DEBUG) {
            Log.i(TAG, "Laid pivot ${layoutInfo.getLayoutPositionOf(view)} at: $viewBounds")
        }

        viewBounds.setEmpty()

        onChildLayoutListener.onChildLaidOut(view)
        return view
    }

    /**
     * @return new space added to the layout
     */
    private fun layout(layoutRequest: LayoutRequest, recycler: Recycler, state: State): Int {
        var remainingSpace = layoutRequest.fillSpace
        layoutResult.reset()

        // Start by recycling children that moved out of bounds
        viewRecycler.recycleByLayoutRequest(recycler, layoutRequest)

        // Keep appending or prepending views until we run out of fill space or items
        while (shouldContinueLayout(remainingSpace, layoutRequest, state)) {
            layoutBlock(layoutRequest, recycler, layoutResult)

            layoutRequest.offsetCheckpoint(
                layoutResult.consumedSpace * layoutRequest.direction.value
            )

            if (!layoutResult.skipConsumption) {
                remainingSpace -= layoutResult.consumedSpace
            }

            /**
             * We don't need to recycle if we didn't consume any space.
             * If we consumed space, we need to recycle children in the opposite direction of layout
             */
            if (layoutResult.consumedSpace > 0) {
                viewRecycler.recycleByLayoutRequest(recycler, layoutRequest)
            }

            layoutResult.reset()
        }

        // Recycle once again after layout is done
        viewRecycler.recycleByLayoutRequest(recycler, layoutRequest)

        return layoutRequest.fillSpace - remainingSpace
    }

    private fun removeInvisibleViews(recycler: Recycler, layoutRequest: LayoutRequest) {
        layoutRequest.setRecyclingEnabled(true)
        viewRecycler.recycleFromStart(recycler, layoutRequest)
        viewRecycler.recycleFromEnd(recycler, layoutRequest)
    }

    open fun offsetChildren(offset: Int, layoutRequest: LayoutRequest) {
        layoutInfo.orientationHelper.offsetChildren(offset)
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
        layoutRequest: LayoutRequest,
        recycler: Recycler, state:
        State
    ) {
        val scrollOffset = layoutAlignment.calculateScrollForAlignment(pivotView)
        val remainingScroll = layoutInfo.getRemainingScroll(state)
        // Offset all views by the existing remaining scroll so that they're still scrolled
        // to their final locations when RecyclerView resumes scrolling
        scrollBy(scrollOffset - remainingScroll, layoutRequest, recycler, state)
    }

    private fun layoutExtraSpace(
        architect: LayoutArchitect,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        recyclerViewState: State
    ) {
        architect.updateForExtraLayoutStart(layoutRequest, recyclerViewState)
        layout(layoutRequest, recycler, recyclerViewState)

        architect.updateForExtraLayoutEnd(layoutRequest, recyclerViewState)
        layout(layoutRequest, recycler, recyclerViewState)
    }

    /**
     * Layout scrap views that were not laid out in the previous step
     * to ensure animations work as expected.
     */
    private fun layoutForPredictiveAnimations(
        architect: LayoutArchitect,
        layoutRequest: LayoutRequest,
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
            val direction = if (position < firstChildPosition != layoutRequest.reverseLayout) {
                LayoutRequest.LayoutDirection.START
            } else {
                LayoutRequest.LayoutDirection.END
            }
            // TODO This is not correct for grids
            if (direction == LayoutRequest.LayoutDirection.START) {
                scrapExtraStart += layoutInfo.getDecoratedSize(scrap.itemView)
            } else {
                scrapExtraEnd += layoutInfo.getDecoratedSize(scrap.itemView)
            }
        }

        layoutRequest.setExtraLayoutSpaceStart(scrapExtraStart)
        layoutRequest.setExtraLayoutSpaceEnd(scrapExtraEnd)
        layoutRequest.setScrap(recycler.scrapList)

        if (scrapExtraStart > 0) {
            val anchor = layoutInfo.getChildClosestToStart()
            if (anchor != null) {
                architect.updateLayoutStateForPredictiveStart(
                    layoutRequest,
                    layoutManager.getPosition(anchor)
                )
                layout(layoutRequest, recycler, state)
            }
        }

        if (scrapExtraEnd > 0) {
            val anchor = layoutInfo.getChildClosestToEnd()
            if (anchor != null) {
                architect.updateLayoutStateForPredictiveEnd(
                    layoutRequest,
                    layoutManager.getPosition(anchor)
                )
                layout(layoutRequest, recycler, state)
            }
        }

        layoutRequest.setScrap(null)
    }

    protected fun addView(view: View, layoutRequest: LayoutRequest) {
        if (!layoutRequest.isUsingScrap()) {
            if (layoutRequest.isLayingOutEnd()) {
                layoutManager.addView(view)
            } else {
                layoutManager.addView(view, 0)
            }
        } else if (layoutRequest.isLayingOutEnd()) {
            layoutManager.addDisappearingView(view)
        } else {
            layoutManager.addDisappearingView(view, 0)
        }
        onChildLayoutListener.onChildCreated(view)
    }

    /**
     * Views that were removed or changed won't count for the consumed space logic inside [layout]
     */
    protected fun shouldSkipSpaceOf(view: View): Boolean {
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        return layoutParams.isItemRemoved || layoutParams.isItemChanged
    }

    protected fun performLayout(view: View, bounds: ViewBounds) {
        layoutManager.layoutDecoratedWithMargins(
            view, bounds.left, bounds.top, bounds.right, bounds.bottom
        )
    }

    private fun shouldContinueLayout(
        remainingSpace: Int,
        layoutRequest: LayoutRequest,
        state: State
    ): Boolean {
        return layoutRequest.hasMoreItems(state) && (remainingSpace > 0 || layoutRequest.isInfinite)
    }

}
