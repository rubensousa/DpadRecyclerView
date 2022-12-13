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
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.LayoutFocusFinder
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller

/**
 * Successor of DpadLayoutManager built from scratch for performance optimizations
 * and to support more features.
 * Currently only supports a single row.
 *
 * TODO:
 * - setRecycleChildrenOnDetach
 * - setExtraSpace
 * - using simple row structure for single spans
 */
class PivotLayoutManager(properties: Properties) : RecyclerView.LayoutManager(),
    PivotLayoutManagerDelegate {

    companion object {
        private const val TAG = "PivotLayoutManager"
    }

    private val configuration = LayoutConfiguration()
    private val layoutInfo = LayoutInfo(this, configuration)
    private val pivotState = PivotState(layoutInfo)
    private val layoutAlignment = LayoutAlignment(this, layoutInfo, configuration)
    private val layoutArchitect = LayoutArchitect(
        this, layoutAlignment, configuration, pivotState, layoutInfo
    )
    private val scroller = LayoutScroller(
        this, layoutInfo, layoutAlignment, configuration, pivotState
    )
    private val focusFinder = LayoutFocusFinder(
        this, configuration, scroller, layoutInfo, pivotState
    )
    private val accessibilityHelper = LayoutAccessibilityHelper(
        this, configuration, layoutInfo, pivotState, scroller
    )
    private var hadFocusBeforeLayout = false
    private var recyclerView: RecyclerView? = null

    init {
        setSpanCount(properties.spanCount)
        setOrientation(properties.orientation)
        configuration.setReverseLayout(properties.reverseLayout)
        configuration.setStackFromEnd(properties.stackFromEnd)
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
        hadFocusBeforeLayout = recyclerView?.hasFocus() ?: false
        // If we have focus, save it temporarily since the views will change and we might lose it
        layoutArchitect.onLayoutChildren(recycler, state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        layoutArchitect.onLayoutCompleted(state)
        recyclerView?.apply {
            scroller.onLayoutCompleted(this, requestFocus = hadFocusBeforeLayout)
        }
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
    ): Int = layoutArchitect.scrollHorizontallyBy(dx, recycler, state)

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ): Int = layoutArchitect.scrollVerticallyBy(dy, recycler, state)

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
        pivotState.onItemsAdded(recyclerView, positionStart, itemCount)
        Log.i(TAG, "OnItemsAdded: $positionStart, $itemCount")
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        pivotState.onItemsChanged(recyclerView)
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        pivotState.onItemsRemoved(recyclerView, positionStart, itemCount)
        Log.i(TAG, "OnItemsRemoved: $positionStart, $itemCount")
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        pivotState.onItemsMoved(recyclerView, from, to, itemCount)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        pivotState.onAdapterChanged(oldAdapter, newAdapter)
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
    ): Boolean = accessibilityHelper.performAccessibilityAction(recyclerView, state, action)

    override fun onSaveInstanceState(): Parcelable {
        return pivotState.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        pivotState.onRestoreInstanceState(state)
        scroller.onRestoreInstanceState()
    }

    // Configuration methods

    override fun setRecyclerView(recyclerView: RecyclerView?) {
        this.recyclerView = recyclerView
        focusFinder.setRecyclerView(recyclerView)
        layoutInfo.setRecyclerView(recyclerView)
        scroller.setRecyclerView(recyclerView)
    }

    override fun setChildrenDrawingOrderEnabled(enabled: Boolean) {
        configuration.setChildDrawingOrderEnabled(enabled)
    }

    override fun setGravity(gravity: Int) {
        configuration.setGravity(gravity)
        requestLayout()
    }

    override fun setOrientation(orientation: Int) {
        configuration.setOrientation(orientation)
        layoutInfo.updateOrientation()
        requestLayout()
    }

    override fun setSpanCount(spanCount: Int) {
        configuration.setSpanCount(spanCount)
        requestLayout()
    }

    override fun getSpanCount(): Int = configuration.spanCount

    override fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup) {
        configuration.setSpanSizeLookup(spanSizeLookup)
        requestLayout()
    }

    override fun setExtraLayoutSpace(value: Int) {
        configuration.setExtraLayoutSpace(value)
    }

    override fun getExtraLayoutSpace(): Int = configuration.extraLayoutSpace

    override fun setFocusableDirection(direction: FocusableDirection) {
        configuration.setFocusableDirection(direction)
    }

    override fun getFocusableDirection(): FocusableDirection = configuration.focusableDirection

    override fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        configuration.setFocusOutAllowed(throughFront, throughBack)
    }

    override fun setFocusOutSideAllowed(throughFront: Boolean, throughBack: Boolean) {
        configuration.setFocusOutSideAllowed(throughFront, throughBack)
    }

    override fun setSmoothFocusChangesEnabled(isEnabled: Boolean) {
        configuration.setSmoothFocusChangesEnabled(isEnabled)
    }

    override fun setFocusSearchDisabled(disabled: Boolean) {
        configuration.setFocusSearchDisabled(disabled)
    }

    override fun isFocusSearchDisabled(): Boolean = configuration.isFocusSearchDisabled

    override fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        layoutAlignment.setParentAlignment(parent)
        layoutAlignment.setChildAlignment(child)
        scrollToSelectedPositionOrRequestLayout(smooth, requestFocus = false)
    }

    override fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean) {
        layoutAlignment.setParentAlignment(alignment)
        scrollToSelectedPositionOrRequestLayout(smooth, requestFocus = false)
    }

    override fun getParentAlignment(): ParentAlignment = layoutAlignment.getParentAlignment()

    override fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean) {
        layoutAlignment.setChildAlignment(alignment)
        scrollToSelectedPositionOrRequestLayout(smooth, requestFocus = false)
    }

    override fun getChildAlignment(): ChildAlignment = layoutAlignment.getChildAlignment()

    // Event methods

    override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean = focusFinder.onRequestFocusInDescendants(direction, previouslyFocusedRect)

    override fun onFocusChanged(gainFocus: Boolean) {
        focusFinder.onFocusChanged(gainFocus)
    }

    override fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        pivotState.addOnViewHolderSelectedListener(listener)
    }

    override fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        pivotState.removeOnViewHolderSelectedListener(listener)
    }

    override fun clearOnViewHolderSelectedListeners() {
        pivotState.clearOnViewHolderSelectedListeners()
    }

    override fun onRtlPropertiesChanged() {
        requestLayout()
    }

    override fun selectPosition(position: Int, subPosition: Int, smooth: Boolean) {
        scroller.scrollToPosition(position, subPosition, smooth)
    }

    override fun selectSubPosition(subPosition: Int, smooth: Boolean) {
        selectPosition(pivotState.position, subPosition, smooth)
    }

    override fun getSelectedPosition(): Int = pivotState.position

    override fun getSelectedSubPosition(): Int = pivotState.subPosition

    override fun getCurrentSubPositions(): Int = pivotState.getCurrentSubPositions()

    override fun addOnLayoutCompletedListener(
        listener: DpadRecyclerView.OnLayoutCompletedListener
    ) {
        layoutArchitect.addOnLayoutCompletedListener(listener)
    }

    override fun removeOnLayoutCompletedListener(
        listener: DpadRecyclerView.OnLayoutCompletedListener
    ) {
        layoutArchitect.removeOnLayoutCompletedListener(listener)
    }

    override fun clearOnLayoutCompletedListeners() {
        layoutArchitect.clearOnLayoutCompletedListeners()
    }

    private fun scrollToSelectedPositionOrRequestLayout(smooth: Boolean, requestFocus: Boolean) {
        recyclerView?.apply {
            if (smooth) {
                scroller.scrollToSelectedPosition(this, smooth = true, requestFocus = requestFocus)
            } else {
                requestLayout()
            }
        }
    }

}
