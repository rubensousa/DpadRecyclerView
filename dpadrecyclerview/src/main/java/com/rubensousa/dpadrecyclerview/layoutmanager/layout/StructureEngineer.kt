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
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider.RecyclerViewProvider
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider.ScrapViewProvider
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider.ViewProvider
import kotlin.math.abs
import kotlin.math.max

internal abstract class StructureEngineer(
    protected val layoutManager: RecyclerView.LayoutManager,
    protected val layoutInfo: LayoutInfo,
    protected val layoutAlignment: LayoutAlignment
) {

    companion object {
        const val TAG = "StructureEngineer"
    }

    // Holds the bounds of the view to be laid out
    protected val viewBounds = ViewBounds()
    private val extraLayoutSpaceCalculator = ExtraLayoutSpaceCalculator(layoutInfo)
    private val viewRecycler = ViewRecycler(layoutManager, layoutInfo)
    private val preLayoutRequest = PreLayoutRequest()
    private val layoutRequest = LayoutRequest()
    private val layoutResult = LayoutResult()
    private val recyclerViewProvider = RecyclerViewProvider()
    private val scrapViewProvider = ScrapViewProvider()

    /**
     * Used to update any internal layout state before [preLayoutChildren] or [layoutChildren]
     */
    open fun onLayoutStarted(state: RecyclerView.State) {
        layoutRequest.init(
            gravity = layoutInfo.getConfiguration().gravity,
            isVertical = layoutInfo.isVertical(),
            reverseLayout = layoutInfo.shouldReverseLayout(),
            infinite = layoutInfo.isInfinite()
        )
    }

    open fun onLayoutFinished() {

    }

    open fun onLayoutCleared() {

    }

    protected open fun onChildrenOffset(offset: Int) {

    }

    /**
     * Starts a new layout from scratch with the pivot view aligned
     * @return pivot view
     */
    protected abstract fun initLayout(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): View

    /**
     * Layout scrap views that were not laid out in the previous step
     * to ensure animations work as expected.
     */
    protected abstract fun layoutDisappearingViews(
        firstView: View,
        lastView: View,
        layoutRequest: LayoutRequest,
        scrapViewProvider: ScrapViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    )

    /**
     * Places one or multiple views at the end/start of the current layout
     */
    protected abstract fun layoutBlock(
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        layoutResult: LayoutResult
    )

    /**
     * Make sure all children are in their initial positions before the real layout pass.
     * Also layout more views if needed so that they correctly appear in the next pass
     * instead of fading in.
     */
    fun preLayoutChildren(
        pivotPosition: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        recyclerViewProvider.updateRecycler(recycler)
        val childCount = layoutInfo.getChildCount()
        val firstView = layoutInfo.getChildAt(0) ?: return
        val lastView = layoutInfo.getChildAt(childCount - 1) ?: return
        val firstPosition = layoutInfo.getOldPositionOf(firstView)
        val lastPosition = layoutInfo.getOldPositionOf(lastView)
        preLayoutRequest.reset(firstPosition, firstView, lastPosition, lastView)

        for (i in 0 until childCount) {
            val view = layoutManager.getChildAt(i) ?: continue
            val viewHolder = layoutInfo.getChildViewHolder(view) ?: continue
            if (layoutInfo.didViewHolderStateChange(
                    viewHolder, pivotPosition, firstPosition, lastPosition,
                    layoutRequest.reverseLayout
                )
            ) {
                preLayoutRequest.updateOffsets(
                    decoratedStart = layoutInfo.getDecoratedStart(view),
                    decoratedEnd = layoutInfo.getDecoratedEnd(view)
                )
            }
        }

        if (preLayoutRequest.extraLayoutSpace > 0) {
            preLayout(preLayoutRequest, layoutRequest, recycler, state)
        }

        recyclerViewProvider.clearRecycler()
    }

    private fun preLayout(
        preLayoutRequest: PreLayoutRequest,
        layoutRequest: LayoutRequest,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        val firstView = preLayoutRequest.firstView
        if (firstView != null) {
            layoutRequest.prepend(preLayoutRequest.firstPosition) {
                setCheckpoint(layoutInfo.getDecoratedStart(firstView))
                setFillSpace(preLayoutRequest.extraLayoutSpace)
            }
            fill(layoutRequest, recyclerViewProvider, recycler, state)
        }
        val lastView = preLayoutRequest.lastView
        if (lastView != null) {
            layoutRequest.append(preLayoutRequest.lastPosition) {
                setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
                setFillSpace(preLayoutRequest.extraLayoutSpace)
            }
            fill(layoutRequest, recyclerViewProvider, recycler, state)
        }
    }

    fun layoutChildren(
        pivotPosition: Int,
        itemChanges: ItemChanges,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        if (!isNewLayoutRequired(state, itemChanges)) {
            if (DpadRecyclerView.DEBUG) {
                Log.i(TAG, "layout changes are out of bounds, so skip full layout: $itemChanges")
            }
            finishLayout()
            return
        }
        recyclerViewProvider.updateRecycler(recycler)

        // Start by detaching all existing views.
        // Views not attached again will be animated out if we're running predictive animations
        layoutManager.detachAndScrapAttachedViews(recycler)

        // Start by laying out the views around the pivot
        val pivotView = initLayout(
            pivotPosition, layoutRequest, recyclerViewProvider, recycler, state
        )

        // Now relayout detached views to ensure animations work as expected
        layoutScrap(recycler, state)

        // Now that all views are laid out, make sure the pivot is still in the correct position
        alignPivot(pivotView, recycler, state)

        // Layout extra space if user requested it
        layoutExtraSpace(layoutRequest, recyclerViewProvider, recycler, state)

        // We might have views we no longer need after aligning the pivot,
        // so recycle them if we're not running animations
        if (!state.willRunSimpleAnimations() && !state.willRunPredictiveAnimations()) {
            removeInvisibleViews(recycler)
        }

        finishLayout()
    }

    private fun layoutExtraSpace(
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        val firstView = layoutInfo.getChildClosestToStart() ?: return
        layoutRequest.prepend(layoutInfo.getLayoutPositionOf(firstView)) {
            extraLayoutSpaceCalculator.update(layoutRequest, state)
            setCheckpoint(layoutInfo.getDecoratedStart(firstView))
            setFillSpace(extraLayoutSpaceStart)
        }
        fill(layoutRequest, viewProvider, recycler, state)

        val lastView = layoutInfo.getChildClosestToEnd() ?: return
        layoutRequest.append(layoutInfo.getLayoutPositionOf(lastView)) {
            extraLayoutSpaceCalculator.update(layoutRequest, state)
            setCheckpoint(layoutInfo.getDecoratedEnd(lastView))
            setFillSpace(extraLayoutSpaceEnd)
        }
        fill(layoutRequest, viewProvider, recycler, state)
    }

    private fun finishLayout() {
        recyclerViewProvider.clearRecycler()
        layoutAlignment.updateScrollLimits()
        preLayoutRequest.clear()
    }

    private fun layoutScrap(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val firstView = layoutManager.getChildAt(0)
        val lastView = layoutManager.getChildAt(layoutManager.childCount - 1)
        if (firstView != null
            && lastView != null
            && state.willRunPredictiveAnimations()
            && recycler.scrapList.isNotEmpty()
        ) {
            layoutRequest.setLayingOutScrap(true)
            scrapViewProvider.updateScrap(recycler.scrapList)
            layoutDisappearingViews(
                firstView, lastView, layoutRequest, scrapViewProvider, recycler, state
            )
            scrapViewProvider.updateScrap(null)
            layoutRequest.setLayingOutScrap(false)

        }
    }

    /**
     * We only need to do a full layout in the following scenarios:
     *
     * 1. There's a structural change in the adapter
     * 2. There are no items in the current layout
     * 3. Pivot is no longer aligned
     * 4. Item changes affect the current visible window
     */
    private fun isNewLayoutRequired(
        state: RecyclerView.State,
        itemChanges: ItemChanges
    ): Boolean {
        if (state.didStructureChange()
            || !itemChanges.isValid()
            || preLayoutRequest.extraLayoutSpace > 0
        ) {
            return true
        }
        val firstPos = layoutInfo.findFirstAddedPosition()
        val lastPos = layoutInfo.findLastAddedPosition()
        if (firstPos == RecyclerView.NO_POSITION || lastPos == RecyclerView.NO_POSITION) {
            return true
        }
        val changesOutOfBounds = if (!layoutRequest.reverseLayout) {
            itemChanges.isOutOfBounds(firstPos, lastPos)
        } else {
            itemChanges.isOutOfBounds(lastPos, firstPos)
        }
        return !changesOutOfBounds
    }

    fun scrollBy(
        offset: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        recycleChildren: Boolean
    ): Int {
        if (recycleChildren) {
            recyclerViewProvider.updateRecycler(recycler)
        }
        // Update the layout request for scrolling before offsetting the views
        updateLayoutRequestForScroll(layoutRequest, state, offset, recycleChildren)

        // Now offset the views and the next layout checkpoint
        offsetChildren(-offset)

        // Layout the next views and recycle the ones we don't need along the way
        fill(layoutRequest, recyclerViewProvider, recycler, state)

        if (recycleChildren) {
            recyclerViewProvider.clearRecycler()
        }
        layoutRequest.setRecyclingEnabled(false)
        return offset
    }

    private fun updateLayoutRequestForScroll(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State,
        scrollOffset: Int,
        recycleChildren: Boolean
    ) {
        val scrollDistance = abs(scrollOffset)
        layoutRequest.setRecyclingEnabled(recycleChildren)
        if (scrollOffset < 0) {
            val view = layoutInfo.getChildClosestToStart() ?: return
            layoutRequest.prepend(layoutInfo.getLayoutPositionOf(view)) {
                setCheckpoint(layoutInfo.getDecoratedStart(view))
                extraLayoutSpaceCalculator.update(layoutRequest, state)
                val availableScrollSpace = max(0, layoutInfo.getStartAfterPadding() - checkpoint)
                setFillSpace(scrollDistance + extraLayoutSpaceStart - availableScrollSpace)
            }
        } else {
            val view = layoutInfo.getChildClosestToEnd() ?: return
            layoutRequest.append(layoutInfo.getLayoutPositionOf(view)) {
                setCheckpoint(layoutInfo.getDecoratedEnd(view))
                extraLayoutSpaceCalculator.update(layoutRequest, state)
                val availableScrollSpace = max(0, checkpoint - layoutInfo.getEndAfterPadding())
                setFillSpace(scrollDistance + extraLayoutSpaceEnd - availableScrollSpace)
            }
        }
    }

    /**
     * @return new space added to the layout
     */
    protected fun fill(
        layoutRequest: LayoutRequest,
        viewProvider: ViewProvider,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int {
        var remainingSpace = layoutRequest.fillSpace
        layoutResult.reset()

        // Start by recycling children that moved out of bounds
        viewRecycler.recycleByLayoutRequest(recycler, layoutRequest)

        // Keep appending or prepending views until we run out of fill space or items
        while (shouldContinueLayout(remainingSpace, viewProvider, layoutRequest, state)) {
            layoutBlock(layoutRequest, viewProvider, recycler, state, layoutResult)

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

    private fun removeInvisibleViews(recycler: RecyclerView.Recycler) {
        layoutRequest.setRecyclingEnabled(true)
        viewRecycler.recycleFromStart(recycler, layoutRequest)
        viewRecycler.recycleFromEnd(recycler, layoutRequest)
    }

    private fun offsetChildren(offset: Int) {
        layoutInfo.orientationHelper.offsetChildren(offset)
        layoutRequest.offsetCheckpoint(offset)
        onChildrenOffset(offset)
    }

    fun logChildren() {
        Log.i(TAG, "Children laid out ${layoutManager.childCount}:")
        for (i in 0 until layoutManager.childCount) {
            val child = layoutManager.getChildAt(i)!!
            val position = layoutInfo.getLayoutPositionOf(child)
            val left = layoutManager.getDecoratedLeft(child)
            val top = layoutManager.getDecoratedTop(child)
            val right = layoutManager.getDecoratedRight(child)
            val bottom = layoutManager.getDecoratedBottom(child)
            Log.i(TAG, "View $position: [$left, $top, $right, $bottom]")
        }
    }

    private fun alignPivot(
        pivotView: View,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) {
        var remainingScroll = layoutInfo.getRemainingScroll(state)

        /**
         * Offset all views by the existing remaining scroll so that they're still scrolled
         * to their final locations when RecyclerView resumes scrolling.
         * But we can't allow the remaining scroll to exceed the total space available.
         * This might happen when RecyclerView keeps adding up scroll changes
         * from previous alignments without consuming them until the next layout pass.
         * When this happens, we just ignore the existing remaining scroll
         * since it's considered no longer applicable.
         */
        if (abs(remainingScroll) > layoutInfo.getTotalSpace()) {
            remainingScroll = 0
        }

        val scrollOffset = layoutAlignment.calculateScrollForAlignment(pivotView) - remainingScroll
        scrollBy(scrollOffset, recycler, state, recycleChildren = false)
    }

    protected fun addView(view: View, layoutRequest: LayoutRequest) {
        if (!layoutRequest.isLayingOutScrap) {
            if (layoutRequest.isAppending()) {
                layoutManager.addView(view)
            } else {
                layoutManager.addView(view, 0)
            }
        } else if (layoutRequest.isAppending()) {
            layoutManager.addDisappearingView(view)
        } else {
            layoutManager.addDisappearingView(view, 0)
        }
    }

    /**
     * Views that were removed or changed won't count for the consumed space logic inside [fill]
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

    fun clear() {
        layoutManager.removeAllViews()
        layoutRequest.clear()
    }

    private fun shouldContinueLayout(
        remainingSpace: Int,
        viewProvider: ViewProvider,
        layoutRequest: LayoutRequest,
        state: RecyclerView.State
    ): Boolean {
        return viewProvider.hasNext(layoutRequest, state)
                && (remainingSpace > 0 || layoutRequest.isInfinite)
    }

}
