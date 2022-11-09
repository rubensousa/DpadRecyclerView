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

package com.rubensousa.dpadrecyclerview.sample.layoutmanager

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

/**
 * Built from scratch for performance optimizations
 */
class TvLayoutManager : RecyclerView.LayoutManager() {

    private val config = TvLayoutConfiguration()
    private val layoutInfo = TvLayoutInfo(config)
    private val layoutArchitect = TvLayoutArchitect(config, layoutInfo)
    private val layoutScroller = TvLayoutScroller(layoutArchitect)
    private val selectionState = TvSelectionState()
    private val accessibilityHelper = TvLayoutAccessibility(
        this, config, layoutInfo, selectionState, layoutScroller
    )
    private var dpadRecyclerView: DpadRecyclerView? = null

    fun setDpadRecyclerView(recyclerView: DpadRecyclerView?) {
        dpadRecyclerView = recyclerView
        accessibilityHelper.setRecyclerView(recyclerView)
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

    // TODO
    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {

    }

    // TODO
    override fun onLayoutCompleted(state: RecyclerView.State?) {
        // Do stuff
    }

    // TODO
    override fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State?,
        layoutPrefetchRegistry: LayoutPrefetchRegistry?
    ) {
        super.collectAdjacentPrefetchPositions(dx, dy, state, layoutPrefetchRegistry)
    }

    // TODO
    override fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: LayoutPrefetchRegistry?
    ) {
        super.collectInitialPrefetchPositions(adapterItemCount, layoutPrefetchRegistry)
    }

    // TODO
    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return 0
    }

    // TODO
    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        return 0
    }

    // TODO
    override fun scrollToPosition(position: Int) {

    }

    // TODO
    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        super.smoothScrollToPosition(recyclerView, state, position)
    }

    // TODO
    override fun startSmoothScroll(smoothScroller: RecyclerView.SmoothScroller?) {

    }

    // TODO
    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsAdded(recyclerView, positionStart, itemCount)
    }

    // TODO
    override fun onItemsChanged(recyclerView: RecyclerView) {
        super.onItemsChanged(recyclerView)
    }

    // TODO
    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount)
    }

    // TODO
    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        super.onItemsMoved(recyclerView, from, to, itemCount)
    }

    // TODO
    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        super.onAdapterChanged(oldAdapter, newAdapter)
    }

    // TODO
    override fun onRequestChildFocus(
        parent: RecyclerView,
        state: RecyclerView.State,
        child: View,
        focused: View?
    ): Boolean {
        return super.onRequestChildFocus(parent, state, child, focused)
    }

    // TODO
    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        return super.onInterceptFocusSearch(focused, direction)
    }

    // TODO
    override fun onAddFocusables(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ): Boolean {
        return super.onAddFocusables(recyclerView, views, direction, focusableMode)
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
        return accessibilityHelper.performAccessibilityAction(recycler, state, action, args)
    }

}
