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
import kotlin.math.abs

internal abstract class StructureEngineer(
    protected val layoutManager: LayoutManager,
    protected val layoutInfo: LayoutInfo,
    protected val layoutAlignment: LayoutAlignment
) {

    companion object {
        const val TAG = "StructureEngineer"

        @JvmStatic
        protected val DEBUG = BuildConfig.DEBUG
    }

    // Holds the bounds of the view to be laid out
    protected val viewBounds = ViewBounds()
    private val viewRecycler = ViewRecycler(layoutManager, layoutInfo)
    private val preLayoutRequest = PreLayoutRequest()
    private val layoutRequest = LayoutRequest()
    private val layoutResult = LayoutResult()

    /**
     * Used to update any internal layout state before onLayoutChildren starts its job
     */
    open fun onLayoutStarted(state: State) {
        layoutRequest.init(
            gravity = layoutInfo.getConfiguration().gravity,
            isVertical = layoutInfo.isVertical(),
            reverseLayout = layoutInfo.getConfiguration().reverseLayout,
            infinite = layoutInfo.isInfinite()
        )
    }

    open fun onLayoutChildrenFinished() {

    }

    open fun onChildrenOffset(offset: Int) {

    }

    /**
     * Starts a new layout from scratch with the pivot view aligned
     * @return pivot view
     */
    protected abstract fun initLayout(
        pivotPosition: Int,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    ): View

    /**
     * Layout scrap views that were not laid out in the previous step
     * to ensure animations work as expected.
     */
    protected abstract fun predictiveLayout(
        firstView: View,
        lastView: View,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    )

    protected abstract fun layoutExtraSpace(
        layoutRequest: LayoutRequest,
        preLayoutRequest: PreLayoutRequest,
        recycler: Recycler,
        state: State
    )

    protected abstract fun updateLayoutRequestForScroll(
        layoutRequest: LayoutRequest,
        state: State,
        scrollOffset: Int
    )

    protected abstract fun preLayout(
        preLayoutRequest: PreLayoutRequest,
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State
    )

    /**
     * Places one or multiple views at the end/start of the current layout
     */
    protected abstract fun layoutBlock(
        layoutRequest: LayoutRequest,
        recycler: Recycler,
        state: State,
        layoutResult: LayoutResult
    )

    /**
     * Make sure all children are in their initial positions before the real layout pass.
     * Also layout more views if needed so that they correctly appear in the next pass
     * instead of fading in.
     */
    fun preLayoutChildren(pivotPosition: Int, recycler: Recycler, state: State) {
        val childCount = layoutInfo.getChildCount()
        val firstView = layoutInfo.getChildAt(0) ?: return
        val lastView = layoutInfo.getChildAt(childCount - 1) ?: return
        val firstPosition = layoutInfo.getOldPositionOf(firstView)
        val lastPosition = layoutInfo.getOldPositionOf(lastView)
        preLayoutRequest.reset(firstPosition, firstView, lastPosition, lastView)
        val remainingScroll = abs(layoutInfo.getRemainingScroll(state))

        for (i in 0 until childCount) {
            val view = layoutManager.getChildAt(i) ?: continue
            val viewHolder = layoutInfo.getChildViewHolder(view) ?: continue
            if (layoutInfo.didChildStateChange(
                    viewHolder, pivotPosition, firstPosition, lastPosition
                )
            ) {
                preLayoutRequest.updateOffsets(
                    decoratedStart = layoutInfo.getDecoratedStart(view),
                    decoratedEnd = layoutInfo.getDecoratedEnd(view),
                    remainingScroll = remainingScroll
                )
            }
        }

        if (preLayoutRequest.extraLayoutSpace > 0) {
            preLayout(preLayoutRequest, layoutRequest, recycler, state)
        }
    }

    fun layoutChildren(pivotPosition: Int, recycler: Recycler, state: State) {
        // Start by detaching all existing views.
        // Views not attached again will be animated out if we're running predictive animations
        layoutManager.detachAndScrapAttachedViews(recycler)

        // Start by laying out the views around the pivot
        val pivotView = initLayout(pivotPosition, layoutRequest, recycler, state)

        // Now relayout detached views to ensure animations work as expected
        val firstView = layoutManager.getChildAt(0)
        val lastView = layoutManager.getChildAt(layoutManager.childCount - 1)
        if (firstView != null && lastView != null && state.willRunPredictiveAnimations()) {
            layoutRequest.setScrap(recycler.scrapList)
            predictiveLayout(firstView, lastView, layoutRequest, recycler, state)
            layoutRequest.setScrap(null)
        }

        // Now that all views are laid out, make sure the pivot is still in the correct position
        alignPivot(pivotView, recycler, state)

        // Layout extra space if user requested it
        layoutExtraSpace(layoutRequest, preLayoutRequest, recycler, state)

        // We might have views we no longer need after aligning the pivot,
        // so recycle them if we're not running animations
        if (!state.willRunSimpleAnimations() && !state.willRunPredictiveAnimations()) {
            removeInvisibleViews(recycler)
        }

        onLayoutChildrenFinished()
        preLayoutRequest.clear()
    }

    fun scrollBy(offset: Int, recycler: Recycler, state: State): Int {
        // Update the layout request for scrolling before offsetting the views
        updateLayoutRequestForScroll(layoutRequest, state, offset)

        // Enable recycling since we might add new views now
        layoutRequest.setRecyclingEnabled(true)

        // Now offset the views and the next layout checkpoint
        offsetChildren(-offset)

        // Layout the next views and recycle the ones we don't need along the way
        fill(layoutRequest, recycler, state)

        return offset
    }

    /**
     * @return new space added to the layout
     */
    protected fun fill(layoutRequest: LayoutRequest, recycler: Recycler, state: State): Int {
        var remainingSpace = layoutRequest.fillSpace
        layoutResult.reset()

        // Start by recycling children that moved out of bounds
        viewRecycler.recycleByLayoutRequest(recycler, layoutRequest)

        // Keep appending or prepending views until we run out of fill space or items
        while (shouldContinueLayout(remainingSpace, layoutRequest, state)) {
            layoutBlock(layoutRequest, recycler, state, layoutResult)

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

    private fun removeInvisibleViews(recycler: Recycler) {
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

    private fun alignPivot(pivotView: View, recycler: Recycler, state: State) {
        // Offset all views by the existing remaining scroll so that they're still scrolled
        // to their final locations when RecyclerView resumes scrolling
        val remainingScroll = layoutInfo.getRemainingScroll(state)
        val scrollOffset = layoutAlignment.calculateScrollForAlignment(pivotView) - remainingScroll
        updateLayoutRequestForScroll(layoutRequest, state, scrollOffset)
        offsetChildren(-scrollOffset)
        fill(layoutRequest, recycler, state)
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
        layoutRequest.clear()
    }

    private fun shouldContinueLayout(
        remainingSpace: Int,
        layoutRequest: LayoutRequest,
        state: State
    ): Boolean {
        return layoutRequest.hasMoreItems(state) && (remainingSpace > 0 || layoutRequest.isInfinite)
    }

}
