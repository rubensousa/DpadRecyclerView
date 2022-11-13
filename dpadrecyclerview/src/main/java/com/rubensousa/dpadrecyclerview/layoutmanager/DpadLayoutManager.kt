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
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.internal.DpadFocusManager
import com.rubensousa.dpadrecyclerview.internal.DpadLayoutDelegate
import com.rubensousa.dpadrecyclerview.internal.DpadScroller
import com.rubensousa.dpadrecyclerview.internal.ScrollAlignment

/**
 * A [GridLayoutManager] that supports DPAD navigation
 */
internal class DpadLayoutManager : GridLayoutManager, PivotLayoutManagerDelegate {

    companion object {
        private val LAYOUT_RECT = Rect()
    }

    private val idleScrollListener = IdleScrollListener()
    private val requestLayoutRunnable = Runnable {
        requestLayout()
    }
    private val selectionListeners = ArrayList<OnViewHolderSelectedListener>()
    private val layoutCompleteListeners = ArrayList<DpadRecyclerView.OnLayoutCompletedListener>()
    private var isAlignmentPending = true
    private var hasFinishedFirstLayout = false
    private var isInLayoutStage = false
    private var extraLayoutSpace = 0
    private var recyclerView: RecyclerView? = null
    private var isSmoothFocusChangesEnabled = true

    // Since the super constructor calls setOrientation internally,
    // we need this to avoid trying to access calls not initialized
    private var isInitialized = false
    private var selectedViewHolder: DpadViewHolder? = null
    private lateinit var delegate: DpadLayoutDelegate
    private lateinit var scrollAlignment: ScrollAlignment
    private lateinit var focusManager: DpadFocusManager
    private lateinit var scroller: DpadScroller

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    constructor(context: Context) : this(context, 1)

    constructor(context: Context, spanCount: Int) : this(
        context, spanCount, RecyclerView.VERTICAL, false
    )

    constructor(context: Context, spanCount: Int, orientation: Int) : this(
        context, spanCount, orientation, false
    )

    constructor(
        context: Context,
        spanCount: Int,
        orientation: Int,
        reverseLayout: Boolean
    ) : super(context, spanCount, orientation, reverseLayout) {
        init()
    }

    private fun init() {
        spanSizeLookup.isSpanIndexCacheEnabled = true
        spanSizeLookup.isSpanGroupIndexCacheEnabled = true
        focusManager = DpadFocusManager(this)
        delegate = DpadLayoutDelegate()
        delegate.orientation = orientation
        delegate.spanCount = spanCount
        scrollAlignment = ScrollAlignment(this)
        scrollAlignment.setOrientation(orientation)
        scroller = DpadScroller(scrollAlignment, focusManager, this)
        isInitialized = true
    }

    override fun setSpanCount(spanCount: Int) {
        if (isInitialized) {
            delegate.spanCount = spanCount
        }
        super.setSpanCount(spanCount)
    }

    override fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup) {
        setSpanSizeLookup(object : SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return spanSizeLookup.getSpanSize(position)
            }
        })
    }

    override fun setOrientation(orientation: Int) {
        super.setOrientation(orientation)
        if (isInitialized) {
            delegate.orientation = orientation
            scrollAlignment.setOrientation(orientation)
        }
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        return delegate.checkLayoutParams(lp)
    }

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return delegate.generateDefaultLayoutParams(orientation)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams): RecyclerView.LayoutParams {
        return delegate.generateLayoutParams(lp)
    }

    override fun generateLayoutParams(
        context: Context, attrs: AttributeSet
    ): RecyclerView.LayoutParams {
        return delegate.generateLayoutParams(context, attrs)
    }

    override fun getDecoratedLeft(child: View): Int {
        return delegate.getDecoratedLeft(child, super.getDecoratedLeft(child))
    }

    override fun getDecoratedTop(child: View): Int {
        return delegate.getDecoratedTop(child, super.getDecoratedTop(child))
    }

    override fun getDecoratedRight(child: View): Int {
        return delegate.getDecoratedRight(child, super.getDecoratedRight(child))
    }

    override fun getDecoratedBottom(child: View): Int {
        return delegate.getDecoratedBottom(child, super.getDecoratedBottom(child))
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
        isInLayoutStage = true
        super.onLayoutChildren(recycler, state)
        if (state.didStructureChange()) {
            scrollAlignment.reset()
        }
        scrollAlignment.updateLayoutState(
            width, height,
            reverseLayout,
            paddingLeft, paddingRight, paddingTop, paddingBottom
        )
        scroller.onLayoutChildren(recyclerView, isAlignmentPending)
    }

    override fun onLayoutCompleted(state: RecyclerView.State) {
        super.onLayoutCompleted(state)
        isInLayoutStage = false
        hasFinishedFirstLayout = true
        isAlignmentPending = scroller.pendingSelectionUpdate
        scroller.onLayoutCompleted(recyclerView)
        layoutCompleteListeners.forEach { listener ->
            listener.onLayoutCompleted(state)
        }
    }

    override fun onDetachedFromWindow(view: RecyclerView?, recycler: RecyclerView.Recycler?) {
        super.onDetachedFromWindow(view, recycler)
        // Reset this here since we might need to realign views
        // after we're attached to the window again
        isAlignmentPending = true
    }

    override fun layoutDecoratedWithMargins(
        child: View, left: Int, top: Int, right: Int, bottom: Int
    ) {
        delegate.layoutDecoratedWithMargins(left, top, right, bottom, width, height, LAYOUT_RECT)
        super.layoutDecoratedWithMargins(
            child, LAYOUT_RECT.left, LAYOUT_RECT.top, LAYOUT_RECT.right, LAYOUT_RECT.bottom
        )
    }

    override fun onRequestChildFocus(
        parent: RecyclerView, state: RecyclerView.State, child: View, focused: View?
    ): Boolean {
        return focusManager.onRequestChildFocus(parent, child, focused)
    }

    // We already align Views during scrolling events, so there's no need to do this
    override fun requestChildRectangleOnScreen(
        parent: RecyclerView,
        child: View,
        rect: Rect,
        immediate: Boolean,
        focusedChildVisible: Boolean
    ): Boolean = false

    // We already align Views during scrolling events, so there's no need to do this
    override fun requestChildRectangleOnScreen(
        parent: RecyclerView, child: View, rect: Rect, immediate: Boolean
    ): Boolean = false

    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        return recyclerView?.let { focusManager.onInterceptFocusSearch(it, focused, direction) }
    }

    override fun onAddFocusables(
        recyclerView: RecyclerView, views: ArrayList<View>, direction: Int, focusableMode: Int
    ): Boolean {
        return focusManager.onAddFocusables(recyclerView, views, direction, focusableMode)
    }

    override fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsAdded(recyclerView, positionStart, itemCount)
        if (!isRecyclerViewScrolling()) {
            isAlignmentPending = true
        }
        focusManager.onItemsAdded(positionStart, itemCount, findFirstVisibleItemPosition())
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        super.onItemsChanged(recyclerView)
        isAlignmentPending = true
        focusManager.onItemsChanged()
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount)
        if (!isRecyclerViewScrolling()) {
            isAlignmentPending = true
        }
        focusManager.onItemsRemoved(positionStart, itemCount, findFirstVisibleItemPosition())
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        super.onItemsMoved(recyclerView, from, to, itemCount)
        if (!isRecyclerViewScrolling()) {
            isAlignmentPending = true
        }
        focusManager.onItemsMoved(from, to, itemCount)
    }

    override fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {
        super.onAdapterChanged(oldAdapter, newAdapter)
        selectedViewHolder = null
        focusManager.onAdapterChanged(oldAdapter)
        if (oldAdapter != null) {
            hasFinishedFirstLayout = false
            isAlignmentPending = true
            isInLayoutStage = false
        }
    }

    override fun scrollToPosition(position: Int) {
        recyclerView?.let { view ->
            scroller.scrollToPosition(view, position, subPosition = 0, smooth = false)
        }
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State?,
        position: Int
    ) {
        scroller.scrollToPosition(recyclerView, position, subPosition = 0, smooth = true)
    }

    override fun startSmoothScroll(smoothScroller: RecyclerView.SmoothScroller) {
        scroller.cancelSmoothScroller()
        super.startSmoothScroll(smoothScroller)
        scroller.setSmoothScroller(smoothScroller)
    }

    override fun calculateExtraLayoutSpace(state: RecyclerView.State, out: IntArray) {
        val totalSpace = scrollAlignment.getTotalSpace()

        // Default extra space must always be the size of the container
        var extraLayoutSpaceStart = totalSpace
        var extraLayoutSpaceEnd = totalSpace

        // Add the extraLayoutSpace defined by the user to the scrolling direction
        if (scroller.scrollDirection != DpadScroller.SCROLL_NONE) {
            if (scroller.scrollDirection == DpadScroller.SCROLL_START) {
                extraLayoutSpaceStart = totalSpace + extraLayoutSpace
            } else {
                extraLayoutSpaceEnd = totalSpace + extraLayoutSpace
            }
        }
        out[0] = extraLayoutSpaceStart
        out[1] = extraLayoutSpaceEnd
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        scroller.updateScrollDirection(dy)
        val scrolled = super.scrollVerticallyBy(dy, recycler, state)
        val currentRecyclerView = recyclerView
        val remainingScroll = dy - scrolled
        if (isVertical() && remainingScroll != 0 && currentRecyclerView != null) {
            isAlignmentPending = true
            scroller.scroll(currentRecyclerView, remainingScroll)
            return dy
        }
        return scrolled
    }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        scroller.updateScrollDirection(dx)
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
        val currentRecyclerView = recyclerView
        val remainingScroll = dx - scrolled
        if (isHorizontal() && remainingScroll != 0 && currentRecyclerView != null) {
            isAlignmentPending = true
            scroller.scroll(currentRecyclerView, remainingScroll)
            return dx
        }
        return scrolled
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(getSelectedPosition())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            focusManager.position = state.selectedPosition
            scroller.pendingSelectionUpdate = state.selectedPosition != RecyclerView.NO_POSITION
        }
    }

    fun getColumnIndex(position: Int): Int {
        return spanSizeLookup.getSpanIndex(position, spanCount)
    }

    fun getEndColumnIndex(position: Int): Int {
        return getColumnIndex(position) + spanSizeLookup.getSpanSize(position) - 1
    }

    fun getRowIndex(position: Int): Int {
        return spanSizeLookup.getSpanGroupIndex(position, spanCount)
    }

    override fun getSelectedPosition(): Int = focusManager.position

    override fun getSelectedSubPosition(): Int = focusManager.subPosition

    override fun getCurrentSubPositions(): Int {
        return selectedViewHolder?.getAlignments()?.size ?: 0
    }

    override fun setExtraLayoutSpace(value: Int) {
        extraLayoutSpace = value
        requestLayout()
    }

    override fun getExtraLayoutSpace() = extraLayoutSpace

    override fun setGravity(gravity: Int) {
        delegate.gravity = gravity
        if (recyclerView == null) {
            return
        }
        isAlignmentPending = true
        requestLayout()
    }

    fun findFirstAddedPosition(): Int {
        if (childCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val child = getChildAt(0) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOfView(child)
    }

    fun findLastAddedPosition(): Int {
        if (childCount == 0) {
            return RecyclerView.NO_POSITION
        }
        val child = getChildAt(childCount - 1) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOfView(child)
    }

    override fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean) {
        scrollAlignment.setParentAlignment(alignment)
        if (recyclerView == null) {
            return
        }
        isAlignmentPending = true
        if (smooth) {
            scroller.scrollToFocusedPosition(requireNotNull(recyclerView), true)
        } else {
            requestLayout()
        }
    }

    override fun getParentAlignment(): ParentAlignment = scrollAlignment.getParentAlignment()

    override fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean) {
        scrollAlignment.setChildAlignment(alignment)
        if (recyclerView == null) {
            return
        }
        isAlignmentPending = true
        if (smooth) {
            scroller.scrollToFocusedPosition(requireNotNull(recyclerView), true)
        } else {
            requestLayout()
        }
    }

    override fun getChildAlignment() = scrollAlignment.getChildAlignment()

    override fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        scrollAlignment.setParentAlignment(parent)
        scrollAlignment.setChildAlignment(child)
        if (recyclerView == null) {
            return
        }
        isAlignmentPending = true
        if (smooth) {
            scroller.scrollToFocusedPosition(requireNotNull(recyclerView), true)
        } else {
            requestLayout()
        }
    }

    override fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        focusManager.focusOutFront = throughFront
        focusManager.focusOutBack = throughBack
    }

    override fun setFocusOutSideAllowed(throughFront: Boolean, throughBack: Boolean) {
        focusManager.focusOutSideFront = throughFront
        focusManager.focusOutSideBack = throughBack
    }

    override fun setFocusSearchDisabled(disabled: Boolean) {
        focusManager.isFocusSearchDisabled = disabled
    }

    override fun isFocusSearchDisabled() = focusManager.isFocusSearchDisabled

    override fun setFocusableDirection(direction: FocusableDirection) {
        focusManager.focusableDirection = direction
    }

    override fun getFocusableDirection() = focusManager.focusableDirection

    override fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        selectionListeners.add(listener)
    }

    override fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        selectionListeners.remove(listener)
    }

    override fun clearOnViewHolderSelectedListeners() {
        selectionListeners.clear()
    }

    fun isRTL() = isLayoutRTL

    override fun addOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        layoutCompleteListeners.add(listener)
    }

    override fun removeOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        layoutCompleteListeners.remove(listener)
    }

    override fun clearOnLayoutCompletedListeners() {
        layoutCompleteListeners.clear()
    }

    override fun selectPosition(position: Int, subPosition: Int, smooth: Boolean) {
        scroller.scrollToPosition(
            requireNotNull(recyclerView), position, subPosition, smooth
        )
    }

    override fun selectSubPosition(subPosition: Int, smooth: Boolean) {
        scroller.scrollToPosition(
            requireNotNull(recyclerView), focusManager.position, subPosition, smooth
        )
    }

    override fun setSmoothFocusChangesEnabled(isEnabled: Boolean) {
        isSmoothFocusChangesEnabled = isEnabled
    }

    internal fun scrollToView(
        recyclerView: RecyclerView, child: View, focused: View?, smooth: Boolean
    ) {
        scroller.scrollToView(recyclerView, child, focused, smooth)
    }

    internal fun isSelectionInProgress() = scroller.isSelectionInProgress

    internal fun scheduleAlignmentIfPending() {
        if (!isAlignmentPending) {
            return
        }
        recyclerView?.let { view ->
            view.post {
                scroller.scrollToFocusedPosition(view, isSmoothFocusChangesEnabled)
            }
        }
    }

    internal fun findImmediateChildIndex(view: View): Int {
        var currentView: View? = view
        if (currentView != null && currentView !== recyclerView) {
            currentView = findContainingItemView(currentView)
            if (currentView != null) {
                var i = 0
                val count = childCount
                while (i < count) {
                    if (getChildAt(i) === currentView) {
                        return i
                    }
                    i++
                }
            }
        }
        return RecyclerView.NO_POSITION
    }

    internal fun dispatchViewHolderSelected() {
        val currentRecyclerView = recyclerView ?: return

        val view = if (getSelectedPosition() == RecyclerView.NO_POSITION) {
            null
        } else {
            findViewByPosition(getSelectedPosition())
        }

        val viewHolder = if (view != null) {
            currentRecyclerView.getChildViewHolder(view)
        } else {
            null
        }

        selectedViewHolder?.onViewHolderDeselected()

        if (viewHolder is DpadViewHolder) {
            selectedViewHolder = viewHolder
            viewHolder.onViewHolderSelected()
        } else {
            selectedViewHolder = null
        }

        if (!hasSelectionListeners()) {
            return
        }

        if (viewHolder != null) {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelected(
                    currentRecyclerView, viewHolder, getSelectedPosition(), getSelectedSubPosition()
                )
            }
        } else {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelected(
                    currentRecyclerView, null, RecyclerView.NO_POSITION, 0
                )
            }
        }

        // Children may request layout when a child selection event occurs (such as a change of
        // padding on the current and previously selected rows).
        // If in layout, a child requesting layout may have been laid out before the selection
        // callback.
        // If it was not, the child will be laid out after the selection callback.
        // If so, the layout request will be honoured though the view system will emit a double-
        // layout warning.
        // If not in layout, we may be scrolling in which case the child layout request will be
        // eaten by recyclerview. Post a requestLayout.
        if (!isInLayoutStage && !currentRecyclerView.isLayoutRequested) {
            val childCount = childCount
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child != null && child.isLayoutRequested) {
                    forceRequestLayout()
                    break
                }
            }
        }
    }

    internal fun dispatchViewHolderSelectedAndAligned() {
        if (!hasSelectionListeners()) {
            return
        }

        val currentRecyclerView = recyclerView ?: return

        val view = if (getSelectedPosition() == RecyclerView.NO_POSITION) {
            null
        } else {
            findViewByPosition(getSelectedPosition())
        }
        val viewHolder = if (view != null) {
            currentRecyclerView.getChildViewHolder(view)
        } else {
            null
        }

        if (viewHolder != null) {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelectedAndAligned(
                    currentRecyclerView, viewHolder, getSelectedPosition(), getSelectedSubPosition()
                )
            }
        } else {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelectedAndAligned(
                    currentRecyclerView, null, RecyclerView.NO_POSITION, 0
                )
            }
        }
    }

    internal fun hasCreatedLastItem(recyclerView: RecyclerView): Boolean {
        val count = itemCount
        return count == 0 || recyclerView.findViewHolderForAdapterPosition(count - 1) != null
    }

    internal fun hasCreatedFirstItem(recyclerView: RecyclerView): Boolean {
        val count = itemCount
        return count == 0 || recyclerView.findViewHolderForAdapterPosition(0) != null
    }

    override fun setRecyclerView(recyclerView: RecyclerView?) {
        if (recyclerView === this.recyclerView) {
            return
        }
        if (recyclerView == null) {
            selectedViewHolder = null
            hasFinishedFirstLayout = false
            isAlignmentPending = true
        }
        this.recyclerView?.removeOnScrollListener(idleScrollListener)
        this.recyclerView = recyclerView
        // Disable flinging since this isn't supposed to be scrollable by touch
        recyclerView?.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                return true
            }
        }
        recyclerView?.addOnScrollListener(idleScrollListener)
    }

    override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean {
        val view = findViewByPosition(getSelectedPosition()) ?: return false
        return view.requestFocus(direction, previouslyFocusedRect)
    }

    /**
     * When [RecyclerView.requestFocus] is called, we need to focus the first focusable child
     */
    override fun onFocusChanged(gainFocus: Boolean) {
        if (!gainFocus) return
        var index = if (getSelectedPosition() == RecyclerView.NO_POSITION) {
            0
        } else {
            getSelectedPosition()
        }
        while (index < itemCount) {
            val view = findViewByPosition(index) ?: break
            if (view.hasFocusable()) {
                view.requestFocus()
                break
            }
            index++
        }
    }

    override fun onRtlPropertiesChanged() {
        requestLayout()
    }

    internal fun isHorizontal() = delegate.isHorizontal()

    internal fun isVertical() = delegate.isVertical()

    internal fun isInLayoutStage() = isInLayoutStage

    internal fun hasFinishedFirstLayout() = hasFinishedFirstLayout

    internal fun getAdapterPositionOfView(view: View): Int {
        val params = view.layoutParams as DpadLayoutParams?
        return if (params == null || params.isItemRemoved) {
            // when item is removed, the position value can be any value.
            RecyclerView.NO_POSITION
        } else {
            params.absoluteAdapterPosition
        }
    }

    internal fun getAdapterPositionOfChildAt(index: Int): Int {
        val child = getChildAt(index) ?: return RecyclerView.NO_POSITION
        return getAdapterPositionOfView(child)
    }

    internal fun isSmoothScrollEnabled(): Boolean {
        return isSmoothFocusChangesEnabled
    }

    override fun setChildrenDrawingOrderEnabled(enabled: Boolean) {
        scroller.childDrawingOrderEnabled = enabled
    }

    private fun hasSelectionListeners(): Boolean {
        return selectionListeners.isNotEmpty()
    }

    private fun isRecyclerViewScrolling(): Boolean {
        val scrollState = recyclerView?.scrollState
        return scrollState != null && scrollState != RecyclerView.SCROLL_STATE_IDLE
    }

    /**
     * RecyclerView prevents us from requesting layout in many cases
     * (during layout, during scroll, etc.)
     * For secondary row size wrap_content support we currently need a
     * second layout pass to update the measured size after having measured
     * and added child views in layoutChildren.
     * Force the second layout by posting a delayed runnable.
     */
    private fun forceRequestLayout() {
        recyclerView?.let {
            ViewCompat.postOnAnimation(it, requestLayoutRunnable)
        }
    }

    /**
     * Takes care of dispatching [OnViewHolderSelectedListener.onViewHolderSelectedAndAligned]
     */
    private inner class IdleScrollListener : RecyclerView.OnScrollListener() {
        private var isScrolling = false
        private var previousSelectedPosition = RecyclerView.NO_POSITION

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val wasScrolling = isScrolling
            isScrolling = newState != RecyclerView.SCROLL_STATE_IDLE
            if (wasScrolling == isScrolling) return
            if (isScrolling) {
                // If we're now scrolling, save the current selection state
                previousSelectedPosition = getSelectedPosition()
            } else if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                // If we're no longer scrolling, check if we need to send a new event
                dispatchViewHolderSelectedAndAligned()
                previousSelectedPosition = RecyclerView.NO_POSITION
            }
            scroller.setIdle()
        }
    }

    data class SavedState(val selectedPosition: Int) : Parcelable {

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        constructor(parcel: Parcel) : this(parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(selectedPosition)
        }

        override fun describeContents(): Int {
            return 0
        }
    }

}
