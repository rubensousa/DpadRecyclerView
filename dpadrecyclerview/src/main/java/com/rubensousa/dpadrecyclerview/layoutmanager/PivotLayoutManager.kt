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
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadLoopDirection
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.OnChildLaidOutListener
import com.rubensousa.dpadrecyclerview.OnViewFocusedListener
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.FocusDispatcher
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.SpanFocusFinder
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutPrefetchCollector
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.PivotLayout
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.DpadScrollbarHelper
import com.rubensousa.dpadrecyclerview.layoutmanager.scroll.LayoutScroller

/**
 * A [RecyclerView.LayoutManager] that builds the layout around a pivot view.
 *
 * It behaves similarly to `GridLayoutManager` with the main difference being how focus is handled.
 */
class PivotLayoutManager(properties: Properties) : RecyclerView.LayoutManager(),
    ItemTouchHelper.ViewDropHandler, RecyclerView.SmoothScroller.ScrollVectorProvider {

    private var layoutDirection: Int = View.LAYOUT_DIRECTION_LTR
    private val configuration = LayoutConfiguration(properties)
    private val layoutInfo = LayoutInfo(this, configuration)
    private val pivotSelector = PivotSelector(this, layoutInfo)
    private val layoutAlignment = LayoutAlignment(this, layoutInfo)
    private val spanFocusFinder = SpanFocusFinder(configuration)
    private val scroller = LayoutScroller(
        this, layoutInfo, layoutAlignment, configuration, pivotSelector, spanFocusFinder
    )
    private val pivotLayout = PivotLayout(
        this, layoutAlignment, configuration, pivotSelector, scroller, layoutInfo
    )
    private val prefetchCollector = LayoutPrefetchCollector(layoutInfo)

    private val focusDispatcher = FocusDispatcher(
        this, configuration, scroller, layoutInfo, pivotSelector, spanFocusFinder
    )
    private val accessibilityHelper = LayoutAccessibilityHelper(
        this, configuration, layoutInfo, pivotSelector, scroller
    )
    private var hadFocusBeforeLayout = false
    private var recyclerView: DpadRecyclerView? = null
    private var isScrollingFromTouchEvent = false
    internal var layoutCompletedListener: DpadRecyclerView.OnLayoutCompletedListener? = null

    override fun checkLayoutParams(layoutParams: RecyclerView.LayoutParams?): Boolean {
        return layoutParams is DpadLayoutParams
    }

    override fun generateLayoutParams(
        context: Context,
        attrs: AttributeSet,
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
        return if (configuration.spanCount == 1) {
            DpadLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else if (configuration.isVertical()) {
            DpadLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        } else {
            DpadLayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun canScrollHorizontally(): Boolean = configuration.isHorizontal()

    override fun canScrollVertically(): Boolean = configuration.isVertical()

    override fun isLayoutReversed(): Boolean = configuration.reverseLayout

    override fun isAutoMeasureEnabled(): Boolean = true

    override fun supportsPredictiveItemAnimations(): Boolean = !layoutInfo.isLoopingAllowed

    override fun prepareForDrop(view: View, target: View, x: Int, y: Int) {
        val targetPos = getPosition(target)
        if (targetPos != RecyclerView.NO_POSITION) {
            scroller.scrollToPosition(targetPos, 0)
        }
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        // If we have focus, save it temporarily since the views will change and we might lose it
        hadFocusBeforeLayout = hasFocus()
        pivotLayout.onLayoutChildren(recycler, state)
        layoutCompletedListener?.onLayoutCompleted(state)
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        pivotLayout.onLayoutCompleted(state)
        if (hadFocusBeforeLayout) {
            focusDispatcher.focusSelectedView()
        }
        if (layoutInfo.isScrollingToTarget) {
            scroller.cancelScrollToTarget()
        }
        pivotSelector.onLayoutCompleted()
        hadFocusBeforeLayout = false
    }

    override fun collectAdjacentPrefetchPositions(
        dx: Int,
        dy: Int,
        state: RecyclerView.State,
        layoutPrefetchRegistry: LayoutPrefetchRegistry,
    ) {
        prefetchCollector.collectAdjacentPrefetchPositions(dx, dy, state, layoutPrefetchRegistry)
    }

    override fun collectInitialPrefetchPositions(
        adapterItemCount: Int,
        layoutPrefetchRegistry: LayoutPrefetchRegistry,
    ) {
        prefetchCollector.collectInitialPrefetchPositions(
            adapterItemCount = adapterItemCount,
            prefetchItemCount = configuration.initialPrefetchItemCount,
            pivotPosition = pivotSelector.position,
            layoutPrefetchRegistry = layoutPrefetchRegistry
        )
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int = pivotLayout.scrollHorizontallyBy(dx, recycler, state)

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int = pivotLayout.scrollVerticallyBy(dy, recycler, state)

    override fun computeHorizontalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset(state)
    }

    override fun computeVerticalScrollOffset(state: RecyclerView.State): Int {
        return computeScrollOffset(state)
    }

    override fun computeHorizontalScrollExtent(state: RecyclerView.State): Int {
        return computeScrollExtent(state)
    }

    override fun computeVerticalScrollExtent(state: RecyclerView.State): Int {
        return computeScrollExtent(state)
    }

    override fun computeHorizontalScrollRange(state: RecyclerView.State): Int {
        return computeScrollRange(state)
    }

    override fun computeVerticalScrollRange(state: RecyclerView.State): Int {
        return computeScrollRange(state)
    }

    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
        if (childCount == 0) {
            return null
        }
        val firstChild = layoutInfo.getChildAt(0) ?: return null
        val firstChildPos = getPosition(firstChild)
        val direction = if (targetPosition < firstChildPos != isLayoutReversed) {
            -1
        } else {
            1
        }
        return if (isHorizontal()) {
            PointF(direction.toFloat(), 0f)
        } else {
            PointF(0f, direction.toFloat())
        }
    }

    private fun computeScrollOffset(state: RecyclerView.State): Int {
        if (childCount == 0) {
            return 0
        }
        return DpadScrollbarHelper.computeScrollOffset(
            state = state,
            orientationHelper = layoutInfo.orientationHelper,
            startChild = layoutInfo.findFirstVisibleChild(),
            endChild = layoutInfo.findLastVisibleChild(),
            lm = this,
            smoothScrollbarEnabled = true,
            reverseLayout = configuration.reverseLayout
        )
    }

    private fun computeScrollExtent(state: RecyclerView.State): Int {
        if (childCount == 0) {
            return 0
        }
        return DpadScrollbarHelper.computeScrollExtent(
            state = state,
            orientationHelper = layoutInfo.orientationHelper,
            startChild = layoutInfo.findFirstVisibleChild(),
            endChild = layoutInfo.findLastVisibleChild(),
            lm = this,
            smoothScrollbarEnabled = true,
        )
    }

    private fun computeScrollRange(state: RecyclerView.State): Int {
        if (childCount == 0) {
            return 0
        }
        return DpadScrollbarHelper.computeScrollRange(
            state = state,
            orientationHelper = layoutInfo.orientationHelper,
            startChild = layoutInfo.findFirstVisibleChild(),
            endChild = layoutInfo.findLastVisibleChild(),
            lm = this,
            smoothScrollbarEnabled = true,
        )
    }

    override fun scrollToPosition(position: Int) {
        scroller.scrollToPosition(position)
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State,
        position: Int,
    ) {
        scroller.scrollToPosition(position, subPosition = 0, smooth = true)
    }

    override fun startSmoothScroll(smoothScroller: RecyclerView.SmoothScroller) {
        scroller.cancelSmoothScroller()
        super.startSmoothScroll(smoothScroller)
        scroller.setSmoothScroller(smoothScroller)
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        layoutInfo.invalidateSpanCache()
        pivotSelector.onItemsAdded(positionStart, itemCount)
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        layoutInfo.invalidateSpanCache()
        pivotSelector.onItemsChanged()
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        layoutInfo.invalidateSpanCache()
        pivotSelector.onItemsRemoved(positionStart, itemCount)
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        layoutInfo.invalidateSpanCache()
        pivotSelector.onItemsMoved(from, to, itemCount)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?,
    ) {
        if (oldAdapter != null) {
            pivotLayout.reset()
            pivotSelector.clear()
        }
    }

    internal fun onFocusChanged(gainFocus: Boolean) {
        // Do nothing if the user is scrolling via touch events
        if (!isScrollingFromTouchEvent) {
            focusDispatcher.onFocusChanged(gainFocus)
        }
    }

    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        return focusDispatcher.onInterceptFocusSearch(recyclerView, focused, direction)
    }

    override fun onAddFocusables(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int,
    ): Boolean {
        return focusDispatcher.onAddFocusables(recyclerView, views, direction, focusableMode)
    }

    fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?,
    ): Boolean {
        return focusDispatcher.onRequestFocusInDescendants(direction, previouslyFocusedRect)
    }

    override fun onRequestChildFocus(
        parent: RecyclerView,
        state: RecyclerView.State,
        child: View,
        focused: View?,
    ): Boolean {
        focusDispatcher.onRequestChildFocus(parent, child, focused)
        return true
    }

    // Disabled since only this LayoutManager knows how to position views
    override fun requestChildRectangleOnScreen(
        parent: RecyclerView,
        child: View,
        rect: Rect,
        immediate: Boolean,
    ): Boolean = false

    override fun onAttachedToWindow(view: RecyclerView) {
        super.onAttachedToWindow(view)
        focusDispatcher.updateParentRecyclerView(view)
        if (configuration.recycleChildrenOnDetach) {
            requestLayout()
        }
    }

    override fun onDetachedFromWindow(view: RecyclerView, recycler: RecyclerView.Recycler) {
        super.onDetachedFromWindow(view, recycler)
        focusDispatcher.clearParentRecyclerView()
        if (configuration.recycleChildrenOnDetach) {
            removeAndRecycleAllViews(recycler)
            recycler.clear()
        }
    }

    override fun getRowCountForAccessibility(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int {
        return accessibilityHelper.getRowCountForAccessibility(state)
    }

    override fun getColumnCountForAccessibility(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
    ): Int {
        return accessibilityHelper.getColumnCountForAccessibility(state)
    }

    override fun onInitializeAccessibilityNodeInfo(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        info: AccessibilityNodeInfoCompat,
    ) {
        accessibilityHelper.onInitializeAccessibilityNodeInfo(recycler, state, info)
    }

    override fun onInitializeAccessibilityNodeInfoForItem(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        host: View,
        info: AccessibilityNodeInfoCompat,
    ) {
        accessibilityHelper.onInitializeAccessibilityNodeInfoForItem(host, info)
    }

    override fun performAccessibilityAction(
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State,
        action: Int,
        args: Bundle?,
    ): Boolean = accessibilityHelper.performAccessibilityAction(recyclerView, state, action)

    override fun onSaveInstanceState(): Parcelable {
        return pivotLayout.onSaveInstanceState()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        pivotLayout.onRestoreInstanceState(state)
    }

    internal fun onRtlPropertiesChanged(layoutDirection: Int) {
        if (this.layoutDirection == layoutDirection) {
            return
        }
        this.layoutDirection = layoutDirection
        requestLayout()
    }

    internal fun updateRecyclerView(recyclerView: DpadRecyclerView?) {
        if (recyclerView == null) {
            focusDispatcher.clearParentRecyclerView()
        }
        this.recyclerView = recyclerView
        layoutInfo.setRecyclerView(recyclerView)
        scroller.setRecyclerView(recyclerView)
        pivotSelector.setRecyclerView(recyclerView)
    }

    internal fun getConfig() = configuration

    internal fun isHorizontal() = configuration.isHorizontal()

    internal fun getScrollOffset(view: View): Int {
        return layoutAlignment.calculateScrollToTarget(view)
    }

    internal fun notifyNestedChildFocus(view: View) {
        pivotSelector.notifyNestedChildFocus(view)
    }

    internal fun select(view: View) {
        val position = layoutInfo.getAdapterPositionOf(view)
        if (position == RecyclerView.NO_POSITION) {
            return
        }
        selectPosition(position = position, subPosition = 0, smooth = true)
    }

    internal fun setScrollingFromTouchEvent(isTouching: Boolean) {
        configuration.setKeepLayoutAnchor(isTouching)
        isScrollingFromTouchEvent = isTouching
    }

    internal fun removeCurrentViewHolderSelection() {
        pivotSelector.removeCurrentViewHolderSelection(clearSelection = isScrollingFromTouchEvent)
    }

    internal fun setIsRetainingFocus(isRetainingFocus: Boolean) {
        pivotSelector.isRetainingFocus = isRetainingFocus
    }

    fun setChildrenDrawingOrderEnabled(enabled: Boolean) {
        configuration.setChildDrawingOrderEnabled(enabled)
    }

    fun setRecycleChildrenOnDetach(recycle: Boolean) {
        configuration.setRecycleChildrenOnDetach(recycle)
    }

    fun setLayoutEnabled(enabled: Boolean) {
        if (configuration.isLayoutEnabled != enabled) {
            configuration.setLayoutEnabled(enabled)
            requestLayout()
        }
    }

    fun isLayoutEnabled(): Boolean = configuration.isLayoutEnabled

    fun setLoopDirection(loopDirection: DpadLoopDirection) {
        if (configuration.loopDirection != loopDirection) {
            configuration.setLoopDirection(loopDirection)
            requestLayout()
        }
    }

    fun getLoopDirection(): DpadLoopDirection = configuration.loopDirection

    fun setGravity(gravity: Int) {
        if (configuration.gravity != gravity) {
            configuration.setGravity(gravity)
            requestLayout()
        }
    }

    fun setOrientation(orientation: Int) {
        if (configuration.orientation != orientation) {
            configuration.setOrientation(orientation)
            layoutInfo.updateOrientation()
            requestLayout()
        }
    }

    fun setReverseLayout(reverseLayout: Boolean) {
        if (configuration.reverseLayout != reverseLayout) {
            configuration.setReverseLayout(reverseLayout)
            requestLayout()
        }
    }

    fun setSpanCount(spanCount: Int) {
        if (configuration.spanCount != spanCount) {
            configuration.setSpanCount(spanCount)
            spanFocusFinder.clearSpanCache()
            pivotLayout.updateStructure()
            requestLayout()
        }
    }

    fun getSpanCount(): Int = configuration.spanCount

    fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup) {
        if (spanSizeLookup !== configuration.spanSizeLookup) {
            configuration.setSpanSizeLookup(spanSizeLookup)
            spanFocusFinder.clearSpanCache()
            requestLayout()
        }
    }

    fun getSpanSizeLookup(): DpadSpanSizeLookup = configuration.spanSizeLookup

    fun setExtraLayoutSpaceStrategy(strategy: ExtraLayoutSpaceStrategy?) {
        configuration.setExtraLayoutSpaceStrategy(strategy)
        requestLayout()
    }

    fun setFocusableDirection(direction: FocusableDirection) {
        configuration.setFocusableDirection(direction)
        focusDispatcher.updateFocusableDirection(direction)
    }

    fun getFocusableDirection(): FocusableDirection = configuration.focusableDirection

    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        configuration.setFocusOutAllowed(throughFront, throughBack)
    }

    fun setFocusOutSideAllowed(throughFront: Boolean, throughBack: Boolean) {
        configuration.setFocusOutSideAllowed(throughFront, throughBack)
    }

    fun setSmoothFocusChangesEnabled(isEnabled: Boolean) {
        configuration.setSmoothFocusChangesEnabled(isEnabled)
    }

    fun setMaxPendingAlignments(max: Int) {
        configuration.setMaxPendingAlignments(max)
    }

    fun getMaxPendingAlignments(): Int {
        return configuration.maxPendingAlignments
    }

    fun setMaxPendingMoves(max: Int) {
        configuration.setMaxPendingMoves(max)
    }

    fun getMaxPendingMoves(): Int {
        return configuration.maxPendingMoves
    }

    fun setSmoothScrollSpeedFactor(speedFactor: Float) {
        configuration.setSmoothScrollSpeedFactor(speedFactor)
    }

    fun getSmoothScrollSpeedFactor(): Float = configuration.smoothScrollSpeedFactor

    fun setScrollEnabled(enabled: Boolean) {
        if (configuration.isScrollEnabled == enabled) {
            return
        }
        configuration.setScrollEnabled(enabled)
        if (enabled) {
            scroller.scrollToSelectedPosition(smooth = configuration.isSmoothFocusChangesEnabled)
        }
    }

    fun setFocusSearchDisabled(disabled: Boolean) {
        configuration.setFocusSearchDisabled(disabled)
    }

    fun setFocusSearchEnabledDuringAnimations(disabled: Boolean) {
        configuration.setFocusSearchEnabledDuringAnimations(disabled)
    }

    fun isFocusSearchDisabled(): Boolean = configuration.isFocusSearchDisabled

    fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        layoutAlignment.setParentAlignment(parent)
        layoutAlignment.setChildAlignment(child)
        scrollToSelectedPositionOrRequestLayout(smooth)
    }

    fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean) {
        layoutAlignment.setParentAlignment(alignment)
        scrollToSelectedPositionOrRequestLayout(smooth)
    }

    fun getParentAlignment(): ParentAlignment = layoutAlignment.getParentAlignment()

    fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean) {
        layoutAlignment.setChildAlignment(alignment)
        scrollToSelectedPositionOrRequestLayout(smooth)
    }

    fun getChildAlignment(): ChildAlignment = layoutAlignment.getChildAlignment()

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        pivotSelector.addOnViewHolderSelectedListener(listener)
    }

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        pivotSelector.removeOnViewHolderSelectedListener(listener)
    }

    fun clearOnViewHolderSelectedListeners() {
        pivotSelector.clearOnViewHolderSelectedListeners()
    }

    fun addOnViewFocusedListener(listener: OnViewFocusedListener) {
        pivotSelector.addOnViewHolderFocusedListener(listener)
    }

    fun removeOnViewFocusedListener(listener: OnViewFocusedListener) {
        pivotSelector.removeOnViewHolderFocusedListener(listener)
    }

    fun clearOnViewFocusedListeners() {
        pivotSelector.clearOnViewHolderFocusedListeners()
    }

    fun selectPosition(position: Int, subPosition: Int, smooth: Boolean) {
        scroller.scrollToPosition(position, subPosition, smooth)
    }

    fun selectSubPosition(subPosition: Int, smooth: Boolean) {
        selectPosition(pivotSelector.position, subPosition, smooth)
    }

    fun getSelectedPosition(): Int = pivotSelector.position

    fun getSelectedSubPosition(): Int = pivotSelector.subPosition

    fun getCurrentSubPositions(): Int = pivotSelector.getCurrentSubPositions()

    fun findFirstVisibleItemPosition(): Int {
        return layoutInfo.findFirstVisiblePosition()
    }

    fun findFirstCompletelyVisibleItemPosition(): Int {
        return layoutInfo.findFirstCompletelyVisiblePosition()
    }

    fun findLastVisibleItemPosition(): Int {
        return layoutInfo.findLastVisiblePosition()
    }

    fun findLastCompletelyVisibleItemPosition(): Int {
        return layoutInfo.findLastCompletelyVisiblePosition()
    }

    fun setOnChildLaidOutListener(listener: OnChildLaidOutListener?) {
        pivotLayout.setOnChildLaidOutListener(listener)
    }

    fun addOnLayoutCompletedListener(
        listener: DpadRecyclerView.OnLayoutCompletedListener,
    ) {
        pivotLayout.addOnLayoutCompletedListener(listener)
    }

    fun removeOnLayoutCompletedListener(
        listener: DpadRecyclerView.OnLayoutCompletedListener,
    ) {
        pivotLayout.removeOnLayoutCompletedListener(listener)
    }

    fun clearOnLayoutCompletedListeners() {
        pivotLayout.clearOnLayoutCompletedListeners()
    }

    private fun scrollToSelectedPositionOrRequestLayout(smooth: Boolean) {
        if (smooth) {
            scroller.scrollToSelectedPosition(smooth = true, requestFocus = false)
        } else {
            requestLayout()
        }
    }

}
