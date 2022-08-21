package com.rubensousa.dpadrecyclerview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.animation.Interpolator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

open class DpadRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dpadRecyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    private var isRetainingFocus = false
    private var smoothScrollByBehavior: SmoothScrollByBehavior? = null
    private var keyInterceptListener: OnKeyInterceptListener? = null
    private var unhandledKeyListener: OnUnhandledKeyListener? = null
    private var dpadLayout: DpadGridLayoutManager? = null

    init {
        dpadLayout = createLayoutManager(context, attrs)
        // The LayoutManager will restore focus and scroll position automatically
        preserveFocusAfterLayout = false

        // Focus a ViewHolder's view first by default if one exists
        descendantFocusability = FOCUS_AFTER_DESCENDANTS
        isFocusable = true
        isFocusableInTouchMode = true

        // Typically all RecyclerViews have a fixed size, so this is a safe default
        setHasFixedSize(true)

        // The LayoutManager will draw the focused view on top of all other views
        isChildrenDrawingOrderEnabled = true

        /**
         * Disable change animation by default due to focus problems when animating.
         * The change animation will create a new temporary view and cause undesired
         * focus animation between the old view and new view.
         */
        (itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations = false

        setWillNotDraw(true)
        overScrollMode = OVER_SCROLL_NEVER
        layoutManager = dpadLayout
    }

    private fun createLayoutManager(context: Context, attrs: AttributeSet?): DpadGridLayoutManager {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.DpadRecyclerView,
            R.attr.dpadRecyclerViewStyle, 0
        )
        val orientation = when (
            typedArray.getInt(R.styleable.DpadRecyclerView_dpadRecyclerViewOrientation, 1)
        ) {
            0 -> HORIZONTAL
            1 -> VERTICAL
            else -> throw IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL")
        }
        val layout = DpadGridLayoutManager(
            context,
            spanCount = typedArray.getInt(
                R.styleable.DpadRecyclerView_dpadRecyclerViewSpanCount, 1
            ),
            orientation = orientation,
            reverseLayout = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewReverseLayout, false
            ),
        )
        layout.setFocusOutAllowed(
            throughFront = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutFront, true
            ),
            throughBack = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutBack, true
            )
        )
        layout.setFocusOppositeOutAllowed(
            throughFront = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutOppositeFront, true
            ),
            throughBack = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutOppositeBack, true
            )
        )
        layout.setCircularFocusEnabled(
            typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewCircularFocusEnabled, false
            )
        )
        typedArray.recycle()
        return layout
    }

    final override fun setHasFixedSize(hasFixedSize: Boolean) {
        super.setHasFixedSize(hasFixedSize)
    }

    final override fun setWillNotDraw(willNotDraw: Boolean) {
        super.setWillNotDraw(willNotDraw)
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
        dpadLayout?.setRecyclerView(null)
        dpadLayout = null

        if (layout != null && layout !is DpadGridLayoutManager) {
            throw IllegalArgumentException(
                "Only DpadGridLayoutManager can be used with DpadRecyclerView"
            )
        }
        if (layout is DpadGridLayoutManager) {
            layout.setRecyclerView(this, isChildrenDrawingOrderEnabled)
            dpadLayout = layout
        }
    }

    override fun focusSearch(focused: View?, direction: Int): View? {
        if (focused == null) {
            return null
        }
        return dpadLayout?.onInterceptFocusSearch(focused, direction)
    }

    override fun focusSearch(direction: Int): View? {
        if (isFocused) {
            // focusSearch will be called when RecyclerView itself is focused.
            // Calling focusSearch(view, int) to get next sibling of current selected child.
            dpadLayout?.let { layout ->
                val view = layout.findViewByPosition(layout.selectedPosition)
                if (view != null) {
                    return focusSearch(view, direction)
                }
            }
        }
        return super.focusSearch(direction)
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        dpadLayout?.onFocusChanged(gainFocus)
    }

    override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean {
        return if (isRetainingFocus) {
            /**
             * Don't focus to child if RecyclerView is already retaining focus temporarily
             * from a previous [removeView] or [removeViewAt]
             */
            false
        } else {
            dpadLayout?.onRequestFocusInDescendants(direction, previouslyFocusedRect) ?: false
        }
    }

    override fun removeView(view: View) {
        val retainFocusForChild = view.hasFocus() && isFocusable
        if (retainFocusForChild) {
            isRetainingFocus = true
            requestFocus()
        }
        super.removeView(view)
        isRetainingFocus = false
    }

    override fun removeViewAt(index: Int) {
        val retainFocusForChild = getChildAt(index)?.hasFocus() ?: false
        if (retainFocusForChild) {
            isRetainingFocus = true
            requestFocus()
        }
        super.removeViewAt(index)
        isRetainingFocus = false
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return dpadLayout?.getChildDrawingOrder(childCount, i) ?: i
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        dpadLayout?.onRtlPropertiesChanged()
    }

    override fun smoothScrollBy(dx: Int, dy: Int) {
        smoothScrollByBehavior?.let { behavior ->
            smoothScrollBy(
                dx, dy,
                behavior.configSmoothScrollByInterpolator(dx, dy),
                behavior.configSmoothScrollByDuration(dx, dy)
            )
        } ?: smoothScrollBy(dx, dy, null, UNDEFINED_DURATION)
    }

    override fun smoothScrollBy(dx: Int, dy: Int, interpolator: Interpolator?) {
        smoothScrollByBehavior?.let { behavior ->
            smoothScrollBy(
                dx, dy,
                interpolator,
                behavior.configSmoothScrollByDuration(dx, dy)
            )
        } ?: smoothScrollBy(dx, dy, interpolator, UNDEFINED_DURATION)
    }

    fun setSpanCount(spans: Int) {
        requireDpadGridLayoutManager().spanCount = spans
    }

    fun setOrientation(orientation: Int) {
        requireDpadGridLayoutManager().orientation = orientation
    }

    fun setSelectedPosition(position: Int, smooth: Boolean) {
        if (smooth) {
            smoothScrollToPosition(position)
        } else {
            scrollToPosition(position)
        }
    }

    fun getSelectedPosition() = dpadLayout?.selectedPosition ?: NO_POSITION

    fun getSelectedSubPosition() = dpadLayout?.subSelectionPosition ?: NO_POSITION

    // TODO
    fun getSelectedSubPositionsCount() = 0

    // TODO
    fun setSelectedSubPosition(subPosition: Int, smooth: Boolean) {

    }



    /**
     * Sets whether focus can move out from the front and/or back of the grid view.
     *
     * @param throughFront For the vertical orientation, this controls whether focus can move out
     * from the top of the grid. For the horizontal orientation, this controls whether focus can
     * move out the front side of the grid.
     *
     * @param throughBack For the vertical orientation, this controls whether focus can move out
     * from the bottom of the grid. For the horizontal orientation, this controls whether focus can
     * move out the back side of the grid.
     */
    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        dpadLayout?.setFocusOutAllowed(throughFront, throughBack)
    }

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        requireDpadGridLayoutManager().addOnViewHolderSelectedListener(listener)
    }

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        requireDpadGridLayoutManager().removeOnViewHolderSelectedListener(listener)
    }

    fun clearOnChildViewHolderSelectedListeners() {
        requireDpadGridLayoutManager().clearOnViewHolderSelectedListeners()
    }

    fun setSmoothScrollBehavior(behavior: SmoothScrollByBehavior?) {
        smoothScrollByBehavior = behavior
    }

    fun setParentAlignment(alignment: ParentAlignment) {
        requireDpadGridLayoutManager().setParentAlignment(alignment)
    }

    fun setChildAlignment(alignment: ChildAlignment) {
        requireDpadGridLayoutManager().setChildAlignment(alignment)
    }

    fun setSpanSizeLookup(spanSizeLookup: GridLayoutManager.SpanSizeLookup) {
        requireDpadGridLayoutManager().spanSizeLookup = spanSizeLookup
    }

    fun requireDpadGridLayoutManager(): DpadGridLayoutManager {
        return dpadLayout ?: throw IllegalStateException(
            "There's no DpadGridLayoutManager attached to this RecyclerView"
        )
    }

    fun getSpanCount(): Int = requireDpadGridLayoutManager().spanCount

    fun setOnUnhandledKeyListener(listener: OnUnhandledKeyListener?) {
        unhandledKeyListener = listener
    }

    fun getOnUnhandledKeyListener(): OnUnhandledKeyListener? = unhandledKeyListener

    fun setOnKeyInterceptListener(listener: OnKeyInterceptListener?) {
        keyInterceptListener = listener
    }

    fun addOnLayoutCompletedListener(listener: OnLayoutCompletedListener) {
        dpadLayout?.addOnLayoutCompletedListener(listener)
    }

    /**
     * Removes a callback to be invoked when the RecyclerView completes a full layout calculation.
     * @param listener The listener to be invoked.
     */
    fun removeOnLayoutCompletedListener(listener: OnLayoutCompletedListener) {
        dpadLayout?.removeOnLayoutCompletedListener(listener)
    }

    fun clearOnLayoutCompletedListeners() {
        dpadLayout?.clearOnLayoutCompletedListeners()
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
