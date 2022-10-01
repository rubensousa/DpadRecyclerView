package com.rubensousa.dpadrecyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.animation.Interpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A [RecyclerView] that scrolls to items on DPAD key events instead of swipe/touch gestures.
 *
 * Items are aligned based on the following configurations:
 * * [ParentAlignment] aligns items in relation to this RecyclerView's dimensions
 * * [ChildAlignment] aligns items in relation to their View's dimensions
 * * Individual ViewHolder configurations returned by [DpadViewHolder.getAlignments]
 *
 * This [DpadRecyclerView] will only scroll automatically when it has focus
 * and receives DPAD key events.
 * To scroll manually to any given item,
 * check [setSelectedPosition], [setSelectedPositionSmooth] and other related methods.
 */
class DpadRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dpadRecyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    companion object {
        const val TAG = "DpadRecyclerView"
    }

    private val delegate = DpadRecyclerViewDelegate(this)
    private var keyInterceptListener: OnKeyInterceptListener? = null
    private var unhandledKeyListener: OnUnhandledKeyListener? = null
    private var motionInterceptListener: OnMotionInterceptListener? = null

    init {
        // The LayoutManager will draw the focused view on top of all other views
        isChildrenDrawingOrderEnabled = true
        delegate.init(context, attrs)
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }

    override fun hasOverlappingRendering(): Boolean {
        return delegate.hasOverlappingRendering()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (keyInterceptListener?.onInterceptKeyEvent(event) == true) {
            return true
        }
        if (super.dispatchKeyEvent(event)) {
            return true
        }
        return unhandledKeyListener?.onUnhandledKey(event) == true
    }

    override fun dispatchGenericFocusedEvent(event: MotionEvent): Boolean {
        if (motionInterceptListener?.onInterceptMotionEvent(event) == true) {
            return true
        }
        return super.dispatchGenericFocusedEvent(event)
    }

    override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        delegate.setLayoutManager(layout)
    }

    override fun focusSearch(focused: View?, direction: Int): View? {
        return delegate.focusSearch(focused, direction)
    }

    override fun focusSearch(direction: Int): View? {
        return delegate.focusSearch(direction) ?: super.focusSearch(direction)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        delegate.onFocusChanged(gainFocus)
    }

    override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean {
        return delegate.onRequestFocusInDescendants(direction, previouslyFocusedRect)
    }

    override fun removeView(view: View) {
        delegate.removeView(view)
        super.removeView(view)
        delegate.setRemoveViewFinished()
    }

    override fun removeViewAt(index: Int) {
        delegate.removeViewAt(index)
        super.removeViewAt(index)
        delegate.setRemoveViewFinished()
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return delegate.getChildDrawingOrder(childCount, i)
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        delegate.onRtlPropertiesChanged()
    }

    override fun smoothScrollBy(dx: Int, dy: Int) {
        delegate.smoothScrollBy(dx, dy)
    }

    override fun smoothScrollBy(dx: Int, dy: Int, interpolator: Interpolator?) {
        delegate.smoothScrollBy(dx, dy, interpolator)
    }

    /**
     * Enable or disable smooth scrolling to new focused position. By default, this is set to true.
     * When set to false, RecyclerView will scroll immediately to the focused view
     * without any animation.
     *
     * @param enabled true to smooth scroll to the new focused position, false to scroll immediately
     */
    fun setSmoothFocusChangesEnabled(enabled: Boolean){
        delegate.setSmoothFocusChangesEnabled(enabled)
    }

    /**
     * Sets the amount of pixels to be used for laying out extra space
     * on the invisible portion of this RecyclerView when scrolling in a given direction.
     *
     * By default, [DpadLayoutManager] already uses a constant entire page as extra space
     * and the value you pass here is passed as extra.
     *
     * E.g: RecyclerView with height of 1080 px has 2 pages of size 1080 invisible but laid out.
     * With extra space of 200 px, the total extra space would be: 2 * (1080 + 200)
     *
     * Default is 0, which means [DpadLayoutManager] will only lay out the default entire page
     * on both scroll directions.
     *
     * @param value Must be equal to or greater than 0
     */
    fun setExtraLayoutSpace(value: Int) {
        delegate.setExtraLayoutSpace(value)
    }

    /**
     * @return custom extra layout space factor set by [setExtraLayoutSpace]
     */
    fun getExtraLayoutSpace(): Int {
        return delegate.getExtraLayoutSpace()
    }

    /**
     * Controls the return value of [View.hasOverlappingRendering].
     * @param enabled true if overlapping rendering is enabled. Default is true
     */
    fun setHasOverlappingRendering(enabled: Boolean) {
        delegate.setHasOverlappingRendering(enabled)
    }

    /**
     * Enables or disables the default rule of drawing the selected view after all other views.
     * Default is true
     *
     * @param enabled True to draw the selected child at last, false otherwise.
     */
    fun setFocusDrawingOrderEnabled(enabled: Boolean) {
        super.setChildrenDrawingOrderEnabled(enabled)
    }

    /**
     * See [setFocusDrawingOrderEnabled]
     * @return true if the selected child view is drawn at last, false otherwise
     */
    fun isFocusDrawingOrderEnabled(): Boolean {
        return super.isChildrenDrawingOrderEnabled()
    }

    /**
     * Disables or enables focus search.
     * @param disabled True to disable focus search, false to enable.
     */
    fun setFocusSearchDisabled(disabled: Boolean) {
        delegate.setFocusSearchDisabled(disabled)
    }

    /**
     * @return True if focus search is disabled.
     */
    fun isFocusSearchDisabled(): Boolean {
        return delegate.isFocusSearchDisabled()
    }

    /**
     * Updates the [GridLayoutManager.SpanSizeLookup] used by the [DpadLayoutManager]
     * of this RecyclerView
     * @param spanSizeLookup the new span configuration
     */
    fun setSpanSizeLookup(spanSizeLookup: GridLayoutManager.SpanSizeLookup) {
        delegate.setSpanSizeLookup(spanSizeLookup)
    }

    /**
     * Updates the number of spans of the [DpadLayoutManager] used by this RecyclerView.
     * @param spans number of columns in vertical orientation,
     * or number of rows in horizontal orientation. Must be greater than 0
     */
    fun setSpanCount(spans: Int) {
        delegate.setSpanCount(spans)
    }

    /**
     * See [setSpanCount]
     */
    fun getSpanCount(): Int = delegate.getSpanCount()

    /**
     * Updates the orientation the [DpadLayoutManager] used by this RecyclerView
     * @param orientation either [RecyclerView.VERTICAL] or [RecyclerView.HORIZONTAL]
     */
    fun setOrientation(orientation: Int) {
        delegate.setOrientation(orientation)
    }

    /**
     * Sets the gravity used for child view positioning.
     * Defaults to [Gravity.TOP] for horizontal orientation
     * and [Gravity.START] for vertical orientation.
     *
     * @param gravity See [Gravity]
     */
    fun setGravity(gravity: Int) {
        delegate.setGravity(gravity)
    }

    /**
     * Updates the parent alignment configuration for child views of this RecyclerView
     * @param alignment the parent alignment configuration
     * @param smooth true if the alignment change should be animated
     */
    fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean = false) {
        delegate.setParentAlignment(alignment, smooth)
    }

    /**
     * @return the current parent alignment configuration
     */
    fun getParentAlignment() = delegate.getParentAlignment()

    /**
     * Updates the child alignment configuration for child views of this RecyclerView
     * @param alignment the child alignment configuration
     * @param smooth true if the alignment change should be animated
     */
    fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean = false) {
        delegate.setChildAlignment(alignment, smooth)
    }

    /**
     * @return the current child alignment configuration
     */
    fun getChildAlignment() = delegate.getChildAlignment()

    /**
     * Updates both parent and child alignments
     * @param parent the parent alignment configuration
     * @param child the child alignment configuration
     * @param smooth true if the alignment change should be animated
     */
    fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        delegate.setAlignments(parent, child, smooth)
    }

    /**
     * Sets whether focus can move out from the front and/or back of the RecyclerView.
     *
     * @param throughFront For the vertical orientation, this controls whether focus can move out
     * from the top.
     * For the horizontal orientation, this controls whether focus can
     * move out the left or right (in RTL) side of the grid.
     *
     * @param throughBack For the vertical orientation, this controls whether focus can move out
     * from the bottom.
     * For the horizontal orientation, this controls whether focus can
     * move out the right or left (in RTL) side of the grid.
     */
    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        delegate.setFocusOutAllowed(throughFront, throughBack)
    }

    /**
     * Sets whether focus can move out from the opposite front and/or back of the RecyclerView
     *
     * @param throughFront For the vertical orientation, this controls whether focus can move out
     * from the left of the grid. For the horizontal orientation, this controls whether focus can
     * move out the top side of the grid.
     *
     * @param throughBack For the vertical orientation, this controls whether focus can move out
     * from the right of the grid. For the horizontal orientation, this controls whether focus can
     * move out the bottom side of the grid.
     */
    fun setFocusOutSideAllowed(throughFront: Boolean, throughBack: Boolean) {
        delegate.setFocusOutSideAllowed(throughFront, throughBack)
    }

    /**
     * Changes the selected item immediately without any scroll animation.
     * @param position adapter position of the item to select
     */
    fun setSelectedPosition(position: Int) {
        delegate.setSelectedPosition(position, smooth = false)
    }

    /**
     * Performs a task on a ViewHolder at a given position after scrolling to it.
     *
     * @param position Adapter position of the item to select
     * @param task     Task to executed on the ViewHolder at the given position
     */
    fun setSelectedPosition(position: Int, task: ViewHolderTask) {
        delegate.setSelectedPosition(position, task, smooth = false)
    }

    /**
     * Changes the selected item and runs an animation to scroll to the target position.
     * @param position Adapter position of the item to select
     */
    fun setSelectedPositionSmooth(position: Int) {
        delegate.setSelectedPosition(position, smooth = true)
    }

    /**
     * Performs a task on a ViewHolder at a given position after scrolling to it.
     *
     * @param position Adapter position of the item to select
     * @param task     Task to executed on the ViewHolder at the given position
     */
    fun setSelectedPositionSmooth(position: Int, task: ViewHolderTask) {
        delegate.setSelectedPosition(position, task, smooth = true)
    }

    /**
     * Changes the main selection and sub selected view immediately without any scroll animation.
     * @param position Adapter position of the item to select
     * @param subPosition index of the alignment from [DpadViewHolder.getAlignments]
     */
    fun setSelectedSubPosition(position: Int, subPosition: Int) {
        delegate.setSelectedSubPosition(position, subPosition, smooth = false)
    }

    /**
     * Changes the sub selected view immediately without any scroll animation.
     * @param subPosition index of the alignment from [DpadViewHolder.getAlignments]
     */
    fun setSelectedSubPosition(subPosition: Int) {
        delegate.setSelectedSubPosition(subPosition, smooth = false)
    }

    /**
     * Changes the sub selected view and runs and animation to scroll to it.
     * @param position Adapter position of the item to select
     * @param subPosition index of the alignment from [DpadViewHolder.getAlignments]
     */
    fun setSelectedSubPositionSmooth(position: Int, subPosition: Int) {
        delegate.setSelectedSubPosition(position, subPosition, smooth = true)
    }

    /**
     * Changes the sub selected view and runs and animation to scroll to it.
     * @param subPosition index of the alignment from [DpadViewHolder.getAlignments]
     */
    fun setSelectedSubPositionSmooth(subPosition: Int) {
        delegate.setSelectedSubPosition(subPosition, smooth = true)
    }

    /**
     * @return the current selected position or [RecyclerView.NO_POSITION] if there's none
     */
    fun getSelectedPosition() = delegate.getSelectedPosition()

    /**
     * @return the current selected sub position or 0 if there's none
     */
    fun getSelectedSubPosition() = delegate.getSelectedSubPosition()

    /**
     * @return the number of available sub positions for the current selected item
     * or 0 if there's none. See [DpadViewHolder.getAlignments]
     */
    fun getCurrentSubPositions() = delegate.getCurrentSubPositions()

    /**
     * Registers a callback to be invoked when an item has been selected
     * @param listener The listener to be invoked.
     */
    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        delegate.addOnViewHolderSelectedListener(listener)
    }

    /**
     * Removes a listener added by [addOnViewHolderSelectedListener]
     * @param listener The listener to be removed.
     */
    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        delegate.removeOnViewHolderSelectedListener(listener)
    }

    /**
     * Clears all existing listeners added by [addOnViewHolderSelectedListener]
     */
    fun clearOnViewHolderSelectedListeners() {
        delegate.clearOnViewHolderSelectedListeners()
    }

    /**
     * Set a custom behavior for [smoothScrollBy]
     * @param behavior Custom behavior or null for the default behavior.
     */
    fun setSmoothScrollBehavior(behavior: SmoothScrollByBehavior?) {
        delegate.smoothScrollByBehavior = behavior
    }

    /**
     * Set a listener that intercepts unhandled key events from [dispatchKeyEvent]
     *
     * @param listener The unhandled key intercept listener.
     */
    fun setOnUnhandledKeyListener(listener: OnUnhandledKeyListener?) {
        unhandledKeyListener = listener
    }

    /**
     * @return the listener set by [setOnUnhandledKeyListener]
     */
    fun getOnUnhandledKeyListener(): OnUnhandledKeyListener? = unhandledKeyListener

    /**
     * Set a listener that intercepts key events received in [dispatchKeyEvent]
     *
     * @param listener The key intercept listener.
     */
    fun setOnKeyInterceptListener(listener: OnKeyInterceptListener?) {
        keyInterceptListener = listener
    }

    /**
     * @return the listener set by [setOnKeyInterceptListener]
     */
    fun getOnKeyInterceptListener(): OnKeyInterceptListener? = keyInterceptListener

    /**
     * Registers a callback to be invoked when this RecyclerView completes a layout pass.
     *
     * @param listener The listener to be invoked.
     */
    fun addOnLayoutCompletedListener(listener: OnLayoutCompletedListener) {
        delegate.addOnLayoutCompletedListener(listener)
    }

    /**
     * Removes a listener added by [addOnLayoutCompletedListener]
     * @param listener The listener to be removed.
     */
    fun removeOnLayoutCompletedListener(listener: OnLayoutCompletedListener) {
        delegate.removeOnLayoutCompletedListener(listener)
    }

    /**
     * Clears all listeners added by [addOnLayoutCompletedListener]
     */
    fun clearOnLayoutCompletedListeners() {
        delegate.clearOnLayoutCompletedListeners()
    }

    /**
     * Sets the generic motion intercept listener.
     *
     * @param listener The motion intercept listener.
     */
    fun setOnMotionInterceptListener(listener: OnMotionInterceptListener?) {
        motionInterceptListener = listener
    }

    /**
     * @return the listener set by [setOnMotionInterceptListener]
     */
    fun getOnMotionInterceptListener(): OnMotionInterceptListener? = motionInterceptListener

    /**
     * @return the [DpadLayoutManager] used by this RecyclerView
     */
    fun getDpadLayoutManager(): DpadLayoutManager {
        return delegate.requireLayout()
    }

    /**
     * Defines behavior of duration and interpolator for [smoothScrollBy].
     */
    interface SmoothScrollByBehavior {
        /**
         * Defines duration in milliseconds of [smoothScrollBy].
         *
         * @param dx x distance in pixels.
         * @param dy y distance in pixels.
         * @return Duration in milliseconds or UNDEFINED_DURATION for default value.
         */
        fun configSmoothScrollByDuration(dx: Int, dy: Int): Int

        /**
         * Defines interpolator of [smoothScrollBy].
         *
         * @param dx x distance in pixels.
         * @param dy y distance in pixels.
         * @return Interpolator to be used or null for default interpolator.
         */
        fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator?
    }

    /**
     * Listener for intercepting key dispatch events.
     */
    interface OnKeyInterceptListener {
        /**
         * @return true if the key dispatch event should be consumed.
         */
        fun onInterceptKeyEvent(event: KeyEvent): Boolean
    }

    /**
     * Listener for intercepting unhandled key events.
     */
    interface OnUnhandledKeyListener {
        /**
         * @return true if the key event should be consumed.
         */
        fun onUnhandledKey(event: KeyEvent): Boolean
    }

    /**
     * Listener for receiving notifications of a completed layout pass
     * by the LayoutManager of this RecyclerView ([DpadLayoutManager.onLayoutCompleted])
     */
    interface OnLayoutCompletedListener {
        /**
         * Called after a full layout calculation has finished.
         * @param state Transient state of RecyclerView
         */
        fun onLayoutCompleted(state: State)
    }

    /**
     * Listener for intercepting generic motion dispatch events.
     */
    interface OnMotionInterceptListener {
        /**
         * @return true if the motion event should be consumed.
         */
        fun onInterceptMotionEvent(event: MotionEvent): Boolean
    }

}

fun RecyclerView.canScrollHorizontally(): Boolean {
    return layoutManager?.canScrollVertically() == true
}

fun RecyclerView.canScrollVertically(): Boolean {
    return layoutManager?.canScrollVertically() == true
}