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
import com.rubensousa.dpadrecyclerview.DpadLayoutParams
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.focus.LayoutFocusFinder
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.internal.layoutmanager.scroll.LayoutScroller

/**
 * Successor of DpadLayoutManager built from scratch for performance optimizations
 *
 * TODO:
 * - setRecycleChildrenOnDetach
 * - setExtraSpace
 * - custom alignment
 * - using simple row structure for single spans
 */
class PivotLayoutManager : RecyclerView.LayoutManager() {

    private val configuration = LayoutConfiguration()
    private val viewSelector = ViewSelector()
    private val layoutInfo = LayoutInfo(this, configuration)
    private val layoutArchitect = LayoutArchitect(this, configuration, viewSelector, layoutInfo)
    private val scroller = LayoutScroller(
        this, configuration, layoutArchitect, layoutInfo, viewSelector
    )
    private val focusFinder = LayoutFocusFinder(
        this, configuration, scroller, layoutInfo, viewSelector
    )
    private val accessibilityHelper = LayoutAccessibilityHelper(
        this, configuration, layoutInfo, viewSelector, scroller
    )
    private var dpadRecyclerView: RecyclerView? = null

    fun setDpadRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
        layoutArchitect.setRecyclerView(recyclerView)
        focusFinder.setRecyclerView(recyclerView)
        layoutInfo.setRecyclerView(recyclerView)
    }

    override fun checkLayoutParams(layoutParams: RecyclerView.LayoutParams?): Boolean {
        return layoutParams is DpadLayoutParams
    }

    override fun generateLayoutParams(
        context: Context,
        attrs: AttributeSet
    ): RecyclerView.LayoutParams {
        return DpadLayoutParams(context, attrs)
    }

    override fun generateLayoutParams(layoutParams: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return when (layoutParams) {
            is DpadLayoutParams -> DpadLayoutParams(layoutParams)
            is RecyclerView.LayoutParams -> DpadLayoutParams(layoutParams)
            is ViewGroup.MarginLayoutParams -> DpadLayoutParams(layoutParams)
            else -> DpadLayoutParams(layoutParams)
        }
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return if (configuration.isHorizontal()) {
            DpadLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            DpadLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun getDecoratedLeft(child: View): Int = layoutInfo.getDecoratedLeft(
        child, super.getDecoratedLeft(child)
    )

    override fun getDecoratedTop(child: View): Int = layoutInfo.getDecoratedTop(
        child, super.getDecoratedTop(child)
    )

    override fun getDecoratedRight(child: View): Int = layoutInfo.getDecoratedRight(
        child, super.getDecoratedRight(child)
    )

    override fun getDecoratedBottom(child: View): Int = layoutInfo.getDecoratedBottom(
        child, super.getDecoratedBottom(child)
    )

    override fun getDecoratedBoundsWithMargins(view: View, outBounds: Rect) {
        super.getDecoratedBoundsWithMargins(view, outBounds)
        layoutInfo.getDecoratedBoundsWithMargins(view, outBounds)
    }

    override fun canScrollHorizontally(): Boolean = configuration.isHorizontal()

    override fun canScrollVertically(): Boolean = configuration.isVertical()

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
        viewSelector.onItemsAdded(recyclerView, positionStart, itemCount)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        viewSelector.onItemsChanged(recyclerView)
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        viewSelector.onItemsRemoved(recyclerView, positionStart, itemCount)
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        viewSelector.onItemsMoved(recyclerView, from, to, itemCount)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        viewSelector.onAdapterChanged(oldAdapter, newAdapter)
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
        return viewSelector.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        viewSelector.onRestoreInstanceState(state)
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
