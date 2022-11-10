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

package com.rubensousa.dpadrecyclerview.internal.layoutmanager

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Built from scratch for performance optimizations
 */
class TvLayoutManager : RecyclerView.LayoutManager() {

    private val config = TvLayoutConfiguration()
    private val selectionState = TvSelectionState()
    private val layoutInfo = TvLayoutInfo(this, config)
    private val layoutArchitect = LayoutArchitect(this, config, selectionState, layoutInfo)
    private val scroller = TvLayoutScroller(
        this, config, layoutArchitect, layoutInfo, selectionState
    )
    private val focusFinder = TvLayoutFocusFinder(
        this, config, scroller, layoutInfo, selectionState
    )
    private val accessibilityHelper = LayoutAccessibilityHelper(
        this, config, layoutInfo, selectionState, scroller
    )
    private var dpadRecyclerView: RecyclerView? = null

    fun setDpadRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
        layoutArchitect.setRecyclerView(recyclerView)
        focusFinder.setRecyclerView(recyclerView)
        layoutInfo.setRecyclerView(recyclerView)
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        return layoutArchitect.checkLayoutParams(lp)
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return layoutArchitect.generateDefaultLayoutParams()
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return layoutArchitect.generateLayoutParams(lp)
    }

    override fun generateLayoutParams(
        context: Context, attrs: AttributeSet
    ): RecyclerView.LayoutParams = layoutArchitect.generateLayoutParams(context, attrs)

    override fun getDecoratedLeft(child: View): Int = layoutArchitect.getDecoratedLeft(
        child, super.getDecoratedLeft(child)
    )

    override fun getDecoratedTop(child: View): Int = layoutArchitect.getDecoratedTop(
        child, super.getDecoratedTop(child)
    )

    override fun getDecoratedRight(child: View): Int = layoutArchitect.getDecoratedRight(
        child, super.getDecoratedRight(child)
    )

    override fun getDecoratedBottom(child: View): Int = layoutArchitect.getDecoratedBottom(
        child, super.getDecoratedBottom(child)
    )

    override fun getDecoratedBoundsWithMargins(view: View, outBounds: Rect) {
        super.getDecoratedBoundsWithMargins(view, outBounds)
        layoutArchitect.getDecoratedBoundsWithMargins(view, outBounds)
    }

    override fun canScrollHorizontally(): Boolean = config.isHorizontal()

    override fun canScrollVertically(): Boolean = config.isVertical()

    override fun isAutoMeasureEnabled(): Boolean = true

    override fun supportsPredictiveItemAnimations(): Boolean = true

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        layoutArchitect.onLayoutChildren(recycler, state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        layoutArchitect.onLayoutCompleted(state)
    }

    override fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State?,
        layoutPrefetchRegistry: LayoutPrefetchRegistry
    ) {
        layoutArchitect.collectAdjacentPrefetchPositions(dx, dy, state, layoutPrefetchRegistry)
    }

    override fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: LayoutPrefetchRegistry
    ) {
        layoutArchitect.collectInitialPrefetchPositions(adapterItemCount, layoutPrefetchRegistry)
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int = scroller.scrollHorizontallyBy(dx, recycler, state)

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int = scroller.scrollVerticallyBy(dy, recycler, state)

    override fun scrollToPosition(position: Int) {
        scroller.scrollToPosition(position)
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        scroller.smoothScrollToPosition(recyclerView, state, position)
    }

    override fun startSmoothScroll(smoothScroller: RecyclerView.SmoothScroller?) {
        scroller.startSmoothScroll(smoothScroller)
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        selectionState.onItemsAdded(recyclerView, positionStart, itemCount)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        selectionState.onItemsChanged(recyclerView)
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        selectionState.onItemsRemoved(recyclerView, positionStart, itemCount)
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        selectionState.onItemsMoved(recyclerView, from, to, itemCount)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        selectionState.onAdapterChanged(oldAdapter, newAdapter)
    }

    override fun onRequestChildFocus(
        parent: RecyclerView,
        state: RecyclerView.State,
        child: View,
        focused: View?
    ): Boolean {
        return focusFinder.onRequestChildFocus(parent, state, child, focused)
    }

    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        return focusFinder.onInterceptFocusSearch(focused, direction)
    }

    override fun onAddFocusables(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ): Boolean {
        return focusFinder.onAddFocusables(recyclerView, views, direction, focusableMode)
    }

    // Disabled since only this LayoutManager knows how to position views
    override fun requestChildRectangleOnScreen(
        parent: RecyclerView,
        child: View,
        rect: Rect,
        immediate: Boolean
    ): Boolean = false

    override fun onSaveInstanceState(): Parcelable {
        return selectionState.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        selectionState.onRestoreInstanceState(state)
    }

    override fun getRowCountForAccessibility(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return accessibilityHelper.getRowCountForAccessibility(recycler, state)
    }

    override fun getColumnCountForAccessibility(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int {
        return accessibilityHelper.getColumnCountForAccessibility(recycler, state)
    }

    override fun onInitializeAccessibilityNodeInfo(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        info: AccessibilityNodeInfoCompat
    ) {
        accessibilityHelper.onInitializeAccessibilityNodeInfo(recycler, state, info)
    }

    override fun onInitializeAccessibilityNodeInfoForItem(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        host: View,
        info: AccessibilityNodeInfoCompat
    ) {
        accessibilityHelper.onInitializeAccessibilityNodeInfoForItem(host, info)
    }

    override fun performAccessibilityAction(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        action: Int,
        args: Bundle?
    ): Boolean {
        return accessibilityHelper.performAccessibilityAction(
            dpadRecyclerView, recycler, state, action, args
        )
    }

}
