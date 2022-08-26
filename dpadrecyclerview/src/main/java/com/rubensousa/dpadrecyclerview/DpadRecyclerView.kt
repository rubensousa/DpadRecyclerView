package com.rubensousa.dpadrecyclerview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.animation.Interpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

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

    init {
        // The LayoutManager will draw the focused view on top of all other views
        isChildrenDrawingOrderEnabled = true
        delegate.init(context, attrs)
    }

    override fun hasOverlappingRendering(): Boolean {
        return delegate.hasOverlappingRendering()
    }

    fun setHasOverlappingRendering(enabled: Boolean) {
        delegate.setHasOverlappingRendering(enabled)
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

    fun setSpanCount(spans: Int) {
        delegate.setSpanCount(spans)
    }

    fun setOrientation(orientation: Int) {
        delegate.setOrientation(orientation)
    }

    fun setGravity(gravity: Int) {
        delegate.setGravity(gravity)
    }

    fun setSelectedPosition(position: Int, smooth: Boolean, task: ViewHolderTask) {
        delegate.setSelectedPosition(position, smooth, task)
    }

    fun setSelectedPosition(position: Int, smooth: Boolean) {
        delegate.setSelectedPosition(position, smooth)
    }

    fun setSelectedPosition(position: Int, subPosition: Int, smooth: Boolean) {
        delegate.setSelectedPosition(position, subPosition, smooth)
    }

    fun getSelectedPosition() = delegate.getSelectedPosition()

    fun getSelectedSubPosition() = delegate.getSelectedSubPosition()

    fun getCurrentSubPositions() = delegate.getCurrentSubPositions()

    fun setSelectedSubPosition(subPosition: Int, smooth: Boolean) {
        delegate.setSelectedSubPosition(subPosition, smooth)
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
     * from the bottom of the grid. For the horizontal orientation, this controls whether focus can
     * move out the back side of the grid.
     */
    fun setFocusOppositeOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        delegate.setFocusOppositeOutAllowed(throughFront, throughBack)
    }

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        delegate.addOnViewHolderSelectedListener(listener)
    }

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        delegate.removeOnViewHolderSelectedListener(listener)
    }

    fun clearOnViewHolderSelectedListeners() {
        delegate.clearOnViewHolderSelectedListeners()
    }

    fun setSmoothScrollBehavior(behavior: SmoothScrollByBehavior?) {
        delegate.smoothScrollByBehavior = behavior
    }

    fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        delegate.setAlignments(parent, child, smooth)
    }

    fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean = false) {
        delegate.setParentAlignment(alignment, smooth)
    }

    fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean = false) {
        delegate.setChildAlignment(alignment, smooth)
    }

    fun setSpanSizeLookup(spanSizeLookup: GridLayoutManager.SpanSizeLookup) {
        delegate.setSpanSizeLookup(spanSizeLookup)
    }

    fun getSpanCount(): Int = delegate.getSpanCount()

    fun setOnUnhandledKeyListener(listener: OnUnhandledKeyListener?) {
        unhandledKeyListener = listener
    }

    fun getOnUnhandledKeyListener(): OnUnhandledKeyListener? = unhandledKeyListener

    fun setOnKeyInterceptListener(listener: OnKeyInterceptListener?) {
        keyInterceptListener = listener
    }

    fun addOnLayoutCompletedListener(listener: OnLayoutCompletedListener) {
        delegate.addOnLayoutCompletedListener(listener)
    }

    /**
     * Removes a callback to be invoked when the RecyclerView completes a full layout calculation.
     * @param listener The listener to be invoked.
     */
    fun removeOnLayoutCompletedListener(listener: OnLayoutCompletedListener) {
       delegate.removeOnLayoutCompletedListener(listener)
    }

    fun clearOnLayoutCompletedListeners() {
        delegate.clearOnLayoutCompletedListeners()
    }

    fun requireDpadLayoutManager(): DpadLayoutManager {
        return delegate.requireLayout()
    }

    /**
     * Defines behavior of duration and interpolator for smoothScrollBy().
     */
    interface SmoothScrollByBehavior {
        /**
         * Defines duration in milliseconds of smoothScrollBy().
         *
         * @param dx x distance in pixels.
         * @param dy y distance in pixels.
         * @return Duration in milliseconds or UNDEFINED_DURATION for default value.
         */
        fun configSmoothScrollByDuration(dx: Int, dy: Int): Int

        /**
         * Defines interpolator of smoothScrollBy().
         *
         * @param dx x distance in pixels.
         * @param dy y distance in pixels.
         * @return Interpolator to be used or null for default interpolator.
         */
        fun configSmoothScrollByInterpolator(dx: Int, dy: Int): Interpolator?
    }

    interface OnKeyInterceptListener {
        /**
         * Returns true if the key dispatch event should be consumed.
         */
        fun onInterceptKeyEvent(event: KeyEvent): Boolean
    }

    /**
     * Listener for intercepting unhandled key events.
     */
    interface OnUnhandledKeyListener {
        /**
         * Returns true if the key event should be consumed.
         */
        fun onUnhandledKey(event: KeyEvent): Boolean
    }

    /**
     * Interface for receiving notification when this DpadRecyclerView
     * has completed a full layout calculation
     */
    interface OnLayoutCompletedListener {
        /**
         * Called after a full layout calculation is finished.
         * @param state Transient state of RecyclerView
         */
        fun onLayoutCompleted(state: State)
    }

}

fun RecyclerView.canScrollHorizontally(): Boolean {
    return layoutManager?.canScrollVertically() == true
}

fun RecyclerView.canScrollVertically(): Boolean {
    return layoutManager?.canScrollVertically() == true
}