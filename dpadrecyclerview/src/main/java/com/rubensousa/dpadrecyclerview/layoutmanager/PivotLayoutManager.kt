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

package com.rubensousa.dpadrecyclerview.layoutmanager

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
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.LayoutFocusFinder
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller

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
    private val viewHolderSelector = ViewHolderSelector()
    private val layoutInfo = LayoutInfo(this, configuration)
    private val architect = LayoutArchitect(this, configuration, viewHolderSelector, layoutInfo)
    private val alignment = LayoutAlignment(layoutInfo, this)
    private val scroller = LayoutScroller(layoutInfo, alignment, architect, viewHolderSelector)
    private val focusFinder = LayoutFocusFinder(
        this, configuration, scroller, layoutInfo, viewHolderSelector
    )
    private val accessibilityHelper = LayoutAccessibilityHelper(
        this, configuration, layoutInfo, viewHolderSelector, scroller
    )
    private var dpadRecyclerView: RecyclerView? = null

    init {
        alignment.setParentAlignment(ParentAlignment(edge = ParentAlignment.Edge.NONE))
    }

    fun setDpadRecyclerView(recyclerView: RecyclerView?) {
        dpadRecyclerView = recyclerView
        architect.setRecyclerView(recyclerView)
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
        architect.onLayoutChildren(recycler, state)
        alignment.onLayoutChildren(
            width, height,
            configuration.reverseLayout,
            paddingLeft, paddingRight, paddingTop, paddingBottom
        )
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        architect.onLayoutCompleted(state)
    }

    override fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State?,
        layoutPrefetchRegistry: LayoutPrefetchRegistry
    ) {
        architect.collectAdjacentPrefetchPositions(dx, dy, state, layoutPrefetchRegistry)
    }

    override fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: LayoutPrefetchRegistry
    ) {
        architect.collectInitialPrefetchPositions(adapterItemCount, layoutPrefetchRegistry)
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int = architect.scrollHorizontallyBy(dx, recycler, state)

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int = architect.scrollVerticallyBy(dy, recycler, state)

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
        viewHolderSelector.onItemsAdded(recyclerView, positionStart, itemCount)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        viewHolderSelector.onItemsChanged(recyclerView)
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        viewHolderSelector.onItemsRemoved(recyclerView, positionStart, itemCount)
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        viewHolderSelector.onItemsMoved(recyclerView, from, to, itemCount)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        viewHolderSelector.onAdapterChanged(oldAdapter, newAdapter)
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
        return viewHolderSelector.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        viewHolderSelector.onRestoreInstanceState(state)
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
    ): Boolean = accessibilityHelper.performAccessibilityAction(dpadRecyclerView, state, action)

}
