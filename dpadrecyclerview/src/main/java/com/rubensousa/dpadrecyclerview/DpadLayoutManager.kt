package com.rubensousa.dpadrecyclerview

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
import com.rubensousa.dpadrecyclerview.internal.DpadFocusManager
import com.rubensousa.dpadrecyclerview.internal.DpadLayoutDelegate
import com.rubensousa.dpadrecyclerview.internal.DpadScroller
import com.rubensousa.dpadrecyclerview.internal.ScrollAlignment

/**
 * A [GridLayoutManager] that supports DPAD navigation
 */
class DpadLayoutManager : GridLayoutManager {

    companion object {
        private val LAYOUT_RECT = Rect()
    }

    val selectedPosition: Int
        get() = focusManager.position

    val subSelectionPosition: Int
        get() = focusManager.subPosition

    private val idleScrollListener = IdleScrollListener()
    private val requestLayoutRunnable = Runnable {
        requestLayout()
    }
    private val selectionListeners = ArrayList<OnViewHolderSelectedListener>()
    private val layoutCompleteListeners = ArrayList<DpadRecyclerView.OnLayoutCompletedListener>()
    private var isAlignmentPending = true
    private var isInLayoutStage = false
    private var extraLayoutSpaceFactor = 0.5f
    private var recyclerView: RecyclerView? = null

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
        isAlignmentPending = false
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
        return focusManager.onRequestChildFocus(parent, state, child, focused)
    }

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
        isAlignmentPending = !isRecyclerViewScrolling()
        focusManager.onItemsAdded(positionStart, itemCount, findFirstVisibleItemPosition())
    }

    override fun onItemsChanged(recyclerView: RecyclerView) {
        super.onItemsChanged(recyclerView)
        focusManager.onItemsChanged()
    }

    override fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {
        super.onItemsRemoved(recyclerView, positionStart, itemCount)
        isAlignmentPending = !isRecyclerViewScrolling()
        focusManager.onItemsRemoved(positionStart, itemCount, findFirstVisibleItemPosition())
    }

    override fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {
        super.onItemsMoved(recyclerView, from, to, itemCount)
        isAlignmentPending = !isRecyclerViewScrolling()
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

    override fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray) {
        if (isHorizontal()) {
            extraLayoutSpace[0] = (width * extraLayoutSpaceFactor).toInt()
            extraLayoutSpace[1] = (width * extraLayoutSpaceFactor).toInt()
        } else {
            extraLayoutSpace[0] = (height * extraLayoutSpaceFactor).toInt()
            extraLayoutSpace[1] = (height * extraLayoutSpaceFactor).toInt()
        }
    }

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {
        val scrolled = super.scrollVerticallyBy(dy, recycler, state)
        val currentRecyclerView = recyclerView
        val remainingScroll = dy - scrolled
        if (isVertical() && remainingScroll != 0 && currentRecyclerView != null) {
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
        val scrolled = super.scrollHorizontallyBy(dx, recycler, state)
        val currentRecyclerView = recyclerView
        val remainingScroll = dx - scrolled
        if (isHorizontal() && remainingScroll != 0 && currentRecyclerView != null) {
            scroller.scroll(currentRecyclerView, remainingScroll)
            return dx
        }
        return scrolled
    }

    override fun onSaveInstanceState(): Parcelable {
        return SavedState(selectedPosition)
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

    fun getRowIndex(position: Int): Int {
        return spanSizeLookup.getSpanGroupIndex(position, spanCount)
    }

    fun setGravity(gravity: Int) {
        delegate.gravity = gravity
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

    fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        scrollAlignment.setParentAlignment(parent)
        scrollAlignment.setChildAlignment(child)
        isAlignmentPending = true
        if (smooth) {
            scroller.scrollToFocusedPosition(requireNotNull(recyclerView), true)
        } else {
            requestLayout()
        }
    }

    fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean) {
        scrollAlignment.setParentAlignment(alignment)
        isAlignmentPending = true
        if (smooth) {
            scroller.scrollToFocusedPosition(requireNotNull(recyclerView), true)
        } else {
            requestLayout()
        }
    }

    fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean) {
        scrollAlignment.setChildAlignment(alignment)
        isAlignmentPending = true
        if (smooth) {
            scroller.scrollToFocusedPosition(requireNotNull(recyclerView), true)
        } else {
            requestLayout()
        }
    }

    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        focusManager.focusOutFront = throughFront
        focusManager.focusOutBack = throughBack
    }

    fun setFocusOppositeOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        focusManager.focusOutOppositeFront = throughFront
        focusManager.focusOutOppositeBack = throughBack
    }

    fun setCircularFocusEnabled(enable: Boolean) {
        focusManager.circularFocusEnabled = enable
    }

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        selectionListeners.add(listener)
    }

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        selectionListeners.remove(listener)
    }

    fun clearOnViewHolderSelectedListeners() {
        selectionListeners.clear()
    }

    fun isRTL() = isLayoutRTL

    fun addOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        layoutCompleteListeners.add(listener)
    }

    fun removeOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        layoutCompleteListeners.remove(listener)
    }

    fun clearOnLayoutCompletedListeners() {
        layoutCompleteListeners.clear()
    }

    fun getCurrentSubSelectionCount(): Int {
        return selectedViewHolder?.getAlignments()?.size ?: 0
    }

    fun selectPosition(position: Int, subPosition: Int, smooth: Boolean) {
        scroller.scrollToPosition(
            requireNotNull(recyclerView), position, subPosition, smooth
        )
    }

    fun selectSubPosition(subPosition: Int, smooth: Boolean) {
        scroller.scrollToPosition(
            requireNotNull(recyclerView), focusManager.position, subPosition, smooth
        )
    }

    internal fun scrollToView(
        recyclerView: RecyclerView, child: View, focused: View?, smooth: Boolean
    ) {
        scroller.scrollToView(recyclerView, child, focused, smooth)
    }

    internal fun isSelectionInProgress() = scroller.isSelectionInProgress

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

        val view = if (selectedPosition == RecyclerView.NO_POSITION) {
            null
        } else {
            findViewByPosition(selectedPosition)
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
                    currentRecyclerView, viewHolder, selectedPosition, subSelectionPosition
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

        val view = if (selectedPosition == RecyclerView.NO_POSITION) {
            null
        } else {
            findViewByPosition(selectedPosition)
        }
        val viewHolder = if (view != null) {
            currentRecyclerView.getChildViewHolder(view)
        } else {
            null
        }

        if (viewHolder != null) {
            selectionListeners.forEach { listener ->
                listener.onViewHolderSelectedAndAligned(
                    currentRecyclerView, viewHolder, selectedPosition, subSelectionPosition
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

    internal fun setRecyclerView(
        recyclerView: RecyclerView?,
        childDrawingOrderEnabled: Boolean = false
    ) {
        if (recyclerView === this.recyclerView) {
            return
        }
        if (recyclerView == null) {
            selectedViewHolder = null
            isAlignmentPending = true
        }
        this.recyclerView?.removeOnScrollListener(idleScrollListener)
        this.recyclerView = recyclerView
        scroller.childDrawingOrderEnabled = childDrawingOrderEnabled
        // Disable flinging since this isn't supposed to be scrollable by touch
        recyclerView?.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                return true
            }
        }
        recyclerView?.addOnScrollListener(idleScrollListener)
    }

    internal fun getChildDrawingOrder(childCount: Int, drawingOrderPosition: Int): Int {
        val view = findViewByPosition(selectedPosition) ?: return drawingOrderPosition
        val focusIndex = recyclerView?.indexOfChild(view) ?: return drawingOrderPosition
        // Scenario: 0 1 2 3 4 5 6 7 8 9, 4 is the focused item
        // drawing order is: 0 1 2 3 9 8 7 6 5 4
        return if (drawingOrderPosition < focusIndex) {
            drawingOrderPosition
        } else if (drawingOrderPosition < childCount - 1) {
            focusIndex + childCount - 1 - drawingOrderPosition
        } else {
            focusIndex
        }
    }

    internal fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean {
        val view = findViewByPosition(selectedPosition) ?: return false
        return view.requestFocus(direction, previouslyFocusedRect)
    }

    /**
     * When [RecyclerView.requestFocus] is called, we need to focus the first focusable child
     */
    internal fun onFocusChanged(gainFocus: Boolean) {
        if (!gainFocus) return
        var index = if (selectedPosition == RecyclerView.NO_POSITION) {
            0
        } else {
            selectedPosition
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

    internal fun onRtlPropertiesChanged() {
        requestLayout()
    }

    internal fun isHorizontal() = delegate.isHorizontal()

    internal fun isVertical() = delegate.isVertical()

    internal fun isInLayoutStage() = isInLayoutStage

    internal fun isAlignmentPending() = isAlignmentPending

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
                previousSelectedPosition = selectedPosition
            } else if (previousSelectedPosition != RecyclerView.NO_POSITION) {
                // If we're no longer scrolling, check if we need to send a new event
                dispatchViewHolderSelectedAndAligned()
                previousSelectedPosition = RecyclerView.NO_POSITION
            }
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
