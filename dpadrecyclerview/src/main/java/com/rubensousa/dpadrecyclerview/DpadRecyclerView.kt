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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutManager

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
open class DpadRecyclerView @JvmOverloads constructor(
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

    final override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    final override fun onTouchEvent(e: MotionEvent?): Boolean {
        return false
    }

    final override fun hasOverlappingRendering(): Boolean {
        return delegate.hasOverlappingRendering()
    }

    final override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (keyInterceptListener?.onInterceptKeyEvent(event) == true) {
            return true
        }
        if (super.dispatchKeyEvent(event)) {
            return true
        }
        return unhandledKeyListener?.onUnhandledKey(event) == true
    }

    final override fun dispatchGenericFocusedEvent(event: MotionEvent): Boolean {
        if (motionInterceptListener?.onInterceptMotionEvent(event) == true) {
            return true
        }
        return super.dispatchGenericFocusedEvent(event)
    }

    final override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        delegate.setLayoutManager(layout)
    }

    final override fun focusSearch(focused: View?, direction: Int): View? {
        return delegate.focusSearch(focused, direction)
    }

    final override fun focusSearch(direction: Int): View? {
        return delegate.focusSearch(direction) ?: super.focusSearch(direction)
    }

    final override fun onFocusChanged(
        gainFocus: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        delegate.onFocusChanged(gainFocus)
    }

    final override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean {
        return delegate.onRequestFocusInDescendants(direction, previouslyFocusedRect)
    }

    final override fun removeView(view: View) {
        delegate.removeView(view)
        super.removeView(view)
        delegate.setRemoveViewFinished()
    }

    final override fun removeViewAt(index: Int) {
        delegate.removeViewAt(index)
        super.removeViewAt(index)
        delegate.setRemoveViewFinished()
    }

    final override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return delegate.getChildDrawingOrder(childCount, i)
    }

    final override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        delegate.onRtlPropertiesChanged()
    }

    final override fun smoothScrollBy(dx: Int, dy: Int) {
        delegate.smoothScrollBy(dx, dy)
    }

    final override fun smoothScrollBy(dx: Int, dy: Int, interpolator: Interpolator?) {
        delegate.smoothScrollBy(dx, dy, interpolator)
    }

    /**
     * Enable or disable smooth scrolling to new focused position. By default, this is set to true.
     * When set to false, RecyclerView will scroll immediately to the focused view
     * without any animation.
     *
     * @param enabled true to smooth scroll to the new focused position, false to scroll immediately
     */
    fun setSmoothFocusChangesEnabled(enabled: Boolean) {
        delegate.setSmoothFocusChangesEnabled(enabled)
    }

    /**
     * Changes how RecyclerView will find the next focusable view.
     * Check [FocusableDirection] for all supported directions. Default is [FocusableDirection.STANDARD]
     */
    fun setFocusableDirection(direction: FocusableDirection) {
        delegate.setFocusableDirection(direction)
    }

    /**
     * @return the current [FocusableDirection]. Default is [FocusableDirection.STANDARD]
     */
    fun getFocusableDirection(): FocusableDirection {
        return delegate.getFocusableDirection()
    }

    /**
     * Sets the strategy for calculating extra layout space.
     *
     * Check [ExtraLayoutSpaceStrategy] for more context.
     */
    fun setExtraLayoutSpaceStrategy(strategy: ExtraLayoutSpaceStrategy?) {
        delegate.setExtraLayoutSpaceStrategy(strategy)
    }

    /**
     * Set whether the LayoutManager of this RecyclerView will recycle its children
     * when this RecyclerView is detached from the window.
     *
     * If you are re-using a [RecyclerView.RecycledViewPool], it might be a good idea to set
     * this flag to **true** so that views will be available to other RecyclerViews
     * immediately.
     *
     * Since by default no extra space is laid out,
     * enabling this flag will only produce a different result
     * if a new extra space configuration is passed through [setExtraLayoutSpaceStrategy].
     *
     * @param recycle Whether children should be recycled in detach or not.
     */
    fun setRecycleChildrenOnDetach(recycle: Boolean) {
        delegate.setRecycleChildrenOnDetach(recycle)
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
        delegate.setChildrenDrawingOrderEnabled(enabled)
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
     * Updates the [DpadSpanSizeLookup] used by the layout manager of this RecyclerView.
     * @param spanSizeLookup the new span size configuration
     */
    @Deprecated("Use setSpanSizeLookup(DpadSpanSizeLookup) instead")
    fun setSpanSizeLookup(spanSizeLookup: GridLayoutManager.SpanSizeLookup) {
        setSpanSizeLookup(object : DpadSpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return spanSizeLookup.getSpanSize(position)
            }
        })
    }

    /**
     * Updates the [DpadSpanSizeLookup] used by the layout manager of this RecyclerView.
     * @param spanSizeLookup the new span size configuration
     */
    fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup) {
        delegate.setSpanSizeLookup(spanSizeLookup)
    }

    /**
     * Updates the number of spans of the [PivotLayoutManager] used by this RecyclerView.
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
     * Updates the orientation of the [PivotLayoutManager] used by this RecyclerView
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
     * This is only supported for single rows (i.e 1 span)
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
     * Similar to [LinearLayoutManager.findFirstVisibleItemPosition]
     *
     * @return The adapter position of the first visible item or [RecyclerView.NO_POSITION] if
     * there aren't any visible items
     */
    fun findFirstVisibleItemPosition(): Int {
        return delegate.findFirstVisibleItemPosition()
    }

    /**
     * Similar to [LinearLayoutManager.findFirstCompletelyVisibleItemPosition]
     *
     * @return The adapter position of the first fully visible item or [RecyclerView.NO_POSITION] if
     * there aren't any fully visible items
     */
    fun findFirstCompletelyVisibleItemPosition(): Int {
        return delegate.findFirstCompletelyVisibleItemPosition()
    }

    /**
     * Similar to [LinearLayoutManager.findLastVisibleItemPosition]
     *
     * @return The adapter position of the last visible item or [RecyclerView.NO_POSITION] if
     * there aren't any visible items
     */
    fun findLastVisibleItemPosition(): Int {
        return delegate.findLastVisibleItemPosition()
    }

    /**
     * Similar to [LinearLayoutManager.findLastCompletelyVisibleItemPosition]
     *
     * @return The adapter position of the last fully visible item or [RecyclerView.NO_POSITION] if
     * there aren't any fully visible items
     */
    fun findLastCompletelyVisibleItemPosition(): Int {
        return delegate.findLastCompletelyVisibleItemPosition()
    }

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
     * by the LayoutManager of this RecyclerView
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
