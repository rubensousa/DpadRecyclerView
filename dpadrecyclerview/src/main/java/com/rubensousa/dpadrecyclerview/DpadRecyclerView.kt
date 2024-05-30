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

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import androidx.annotation.Px
import androidx.annotation.VisibleForTesting
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutManager

/**
 * A [RecyclerView] that scrolls to items on DPAD key events.
 *
 * Items are aligned based on the following configurations:
 * * [ParentAlignment] aligns items in relation to this RecyclerView's dimensions
 * * [ChildAlignment] aligns items in relation to their View's dimensions
 * * Individual sub position configurations returned by [DpadViewHolder.getSubPositionAlignments]
 *
 * This [DpadRecyclerView] will only scroll automatically when it has focus
 * and receives DPAD key events.
 * To scroll manually to any given item,
 * check [setSelectedPosition], [setSelectedPositionSmooth] and other related methods.
 *
 * When using wrap_content for the main scrolling direction,
 * [DpadRecyclerView] will still measure itself to match its parent's size,
 * but will layout all items at once without any recycling.
 */
open class DpadRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.dpadRecyclerViewStyle
) : RecyclerView(context, attrs, defStyleAttr) {

    internal companion object {
        internal const val TAG = "DpadRecyclerView"
        internal val DEBUG = BuildConfig.DEBUG
    }

    private val viewHolderTaskExecutor = ViewHolderTaskExecutor()
    private val focusableChildDrawingCallback = FocusableChildDrawingCallback()
    private val fadingEdge = FadingEdge()

    private var pivotLayoutManager: PivotLayoutManager? = null
    private var isOverlappingRenderingEnabled = true
    private var isRetainingFocus = false
    private var startedTouchScroll = false
    private var layoutWhileScrollingEnabled = false
    private var hasPendingLayout = false
    private var touchInterceptListener: OnTouchInterceptListener? = null
    private var smoothScrollByBehavior: SmoothScrollByBehavior? = null
    private var keyInterceptListener: OnKeyInterceptListener? = null
    private var unhandledKeyListener: OnUnhandledKeyListener? = null
    private var motionInterceptListener: OnMotionInterceptListener? = null

    init {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.DpadRecyclerView,
            R.attr.dpadRecyclerViewStyle,
            0
        )

        // Set this DpadRecyclerView as focusable by default
        if (!typedArray.hasValue(R.styleable.DpadRecyclerView_android_focusable)) {
            isFocusable = true
        }
        if (!typedArray.hasValue(R.styleable.DpadRecyclerView_android_focusableInTouchMode)) {
            isFocusableInTouchMode = true
        }

        layoutManager = createLayoutManager(typedArray, context, attrs)

        // The LayoutManager will restore focus and scroll automatically when needed
        preserveFocusAfterLayout = false

        // Focus a ViewHolder's view first by default if one exists
        descendantFocusability = FOCUS_AFTER_DESCENDANTS

        // Call setItemAnimator to set it up
        this.itemAnimator = itemAnimator

        val fadingEdgeLength = typedArray.getDimensionPixelOffset(
            R.styleable.DpadRecyclerView_android_fadingEdgeLength, 0
        )
        if (fadingEdgeLength > 0) {
            setFadingEdgeLength(fadingEdgeLength)
        }

        setWillNotDraw(true)
        setChildDrawingOrderCallback(focusableChildDrawingCallback)
        overScrollMode = OVER_SCROLL_NEVER
        typedArray.recycle()
        removeSelectionForRecycledViewHolders()
    }

    private fun createLayoutManager(
        typedArray: TypedArray,
        context: Context,
        attrs: AttributeSet?
    ): PivotLayoutManager {
        val properties = LayoutManager.getProperties(context, attrs, 0, 0)
        val layout = PivotLayoutManager(properties)
        layout.setFocusOutAllowed(
            throughFront = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutFront, true
            ),
            throughBack = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutBack, true
            )
        )
        layout.setFocusOutSideAllowed(
            throughFront = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutSideFront, true
            ),
            throughBack = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutSideBack, true
            )
        )
        layout.setFocusableDirection(
            FocusableDirection.values()[typedArray.getInt(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusableDirection,
                FocusableDirection.STANDARD.ordinal
            )]
        )
        layout.setLoopDirection(
            DpadLoopDirection.values()[typedArray.getInt(
                R.styleable.DpadRecyclerView_dpadRecyclerViewLoopDirection,
                DpadLoopDirection.NONE.ordinal
            )]
        )
        layout.setSmoothFocusChangesEnabled(
            typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewSmoothFocusChangesEnabled, true
            )
        )
        if (typedArray.hasValue(R.styleable.DpadRecyclerView_android_gravity)) {
            layout.setGravity(
                typedArray.getInt(R.styleable.DpadRecyclerView_android_gravity, Gravity.NO_GRAVITY)
            )
        }
        val edge = ParentAlignment.Edge.values()[typedArray.getInt(
            R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentEdge,
            ParentAlignment.Edge.MIN_MAX.ordinal
        )]
        val parentAlignment = ParentAlignment(
            edge = edge,
            offset = typedArray.getDimensionPixelSize(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentOffset,
                ViewAlignment.DEFAULT_OFFSET
            ),
            fraction = typedArray.getFloat(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentFraction,
                ViewAlignment.DEFAULT_FRACTION
            ),
            isFractionEnabled = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentFractionEnabled,
                true
            ),
            preferKeylineOverEdge = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentPreferKeylineOverEdge,
                edge == ParentAlignment.Edge.MAX
            ),
        )
        val childAlignment = ChildAlignment(
            offset = typedArray.getDimensionPixelSize(
                R.styleable.DpadRecyclerView_dpadRecyclerViewChildAlignmentOffset,
                ViewAlignment.DEFAULT_OFFSET
            ),
            fraction = typedArray.getFloat(
                R.styleable.DpadRecyclerView_dpadRecyclerViewChildAlignmentFraction,
                ViewAlignment.DEFAULT_FRACTION
            ),
            isFractionEnabled = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewChildAlignmentFractionEnabled,
                true
            )
        )
        layout.setAlignments(parentAlignment, childAlignment, smooth = false)
        return layout
    }

    final override fun setLayoutManager(layout: LayoutManager?) {
        super.setLayoutManager(layout)
        pivotLayoutManager?.removeOnViewHolderSelectedListener(viewHolderTaskExecutor)
        pivotLayoutManager?.updateRecyclerView(null)
        if (pivotLayoutManager !== layout) {
            pivotLayoutManager?.clearOnLayoutCompletedListeners()
            pivotLayoutManager?.clearOnViewHolderSelectedListeners()
        }
        pivotLayoutManager = null

        if (layout != null && layout !is PivotLayoutManager) {
            throw IllegalArgumentException(
                "Only PivotLayoutManager is supported, but got $layout"
            )
        }
        if (layout is PivotLayoutManager) {
            layout.updateRecyclerView(this)
            layout.addOnViewHolderSelectedListener(viewHolderTaskExecutor)
            pivotLayoutManager = layout
        }
    }

    final override fun requestLayout() {
        if (isRequestLayoutAllowed()) {
            hasPendingLayout = false
            if (DEBUG) {
                Log.i(TAG, "Layout Requested")
            }
            super.requestLayout()
        } else {
            hasPendingLayout = true
            if (DEBUG) {
                Log.i(TAG, "Layout suppressed until scroll is idle")
            }
        }
    }

    private fun isRequestLayoutAllowed(): Boolean {
        return scrollState == SCROLL_STATE_IDLE || layoutWhileScrollingEnabled
    }

    // Overriding to prevent WRAP_CONTENT behavior by replacing it
    // with the size defined by the parent. Leanback also doesn't support WRAP_CONTENT
    final override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val layout = layoutManager
        if (layout == null) {
            super.onMeasure(widthSpec, heightSpec)
            return
        }
        val layoutParams = layoutParams
        if (getOrientation() == VERTICAL
            && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT
        ) {
            super.onMeasure(
                widthSpec, MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(heightSpec),
                    MeasureSpec.EXACTLY
                )
            )
            return
        } else if (getOrientation() == HORIZONTAL
            && layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT
        ) {
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(
                    MeasureSpec.getSize(widthSpec),
                    MeasureSpec.EXACTLY
                ),
                heightSpec,
            )
            return
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    final override fun setItemAnimator(animator: ItemAnimator?) {
        super.setItemAnimator(animator)
        /**
         * Disable change animation by default due to focus problems when animating.
         * The change animation will create a new temporary view and cause undesired
         * focus animation between the old view and new view.
         */
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    final override fun setWillNotDraw(willNotDraw: Boolean) {
        super.setWillNotDraw(willNotDraw)
    }

    final override fun setHasFixedSize(hasFixedSize: Boolean) {
        super.setHasFixedSize(hasFixedSize)
    }

    final override fun addRecyclerListener(listener: RecyclerListener) {
        super.addRecyclerListener(listener)
    }

    final override fun hasOverlappingRendering(): Boolean {
        return isOverlappingRenderingEnabled
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        touchInterceptListener?.let { listener ->
            if (listener.onInterceptTouchEvent(event)) {
                return true
            }
        }
        return super.dispatchTouchEvent(event)
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

    final override fun focusSearch(direction: Int): View? {
        val currentLayout = pivotLayoutManager
        if (isFocused && currentLayout != null) {
            val view = currentLayout.findViewByPosition(currentLayout.getSelectedPosition())
            return if (view != null) {
                focusSearch(view, direction)
            } else {
                focusSearch(this, direction)
            }
        }
        return super.focusSearch(direction)
    }

    final override fun onFocusChanged(
        gainFocus: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        pivotLayoutManager?.onFocusChanged(gainFocus)
    }

    final override fun onRequestFocusInDescendants(
        direction: Int,
        previouslyFocusedRect: Rect?
    ): Boolean {
        if (isRetainingFocus) {
            /**
             * Don't focus to child if DpadRecyclerView is already retaining focus temporarily
             * from a previous [removeView] or [removeViewAt]
             */
            return false
        }
        return pivotLayoutManager?.onRequestFocusInDescendants(direction, previouslyFocusedRect)
            ?: false
    }

    final override fun removeView(view: View) {
        isRetainingFocus = view.hasFocus() && isFocusable
        if (isRetainingFocus) {
            requestFocus()
        }
        super.removeView(view)
        isRetainingFocus = false
    }

    final override fun removeViewAt(index: Int) {
        val childHasFocus = getChildAt(index)?.hasFocus() ?: false
        isRetainingFocus = childHasFocus && isFocusable
        if (isRetainingFocus) {
            requestFocus()
        }
        super.removeViewAt(index)
        isRetainingFocus = false
    }

    final override fun setChildDrawingOrderCallback(
        childDrawingOrderCallback: ChildDrawingOrderCallback?
    ) {
        super.setChildDrawingOrderCallback(childDrawingOrderCallback)
    }

    final override fun onRtlPropertiesChanged(layoutDirection: Int) {
        super.onRtlPropertiesChanged(layoutDirection)
        pivotLayoutManager?.onRtlPropertiesChanged(layoutDirection)
    }

    final override fun smoothScrollBy(dx: Int, dy: Int) {
        smoothScrollByBehavior?.let { behavior ->
            smoothScrollBy(
                dx, dy,
                behavior.configSmoothScrollByInterpolator(dx, dy),
                behavior.configSmoothScrollByDuration(dx, dy)
            )
        } ?: smoothScrollBy(dx, dy, null, UNDEFINED_DURATION)
    }

    final override fun smoothScrollBy(dx: Int, dy: Int, interpolator: Interpolator?) {
        smoothScrollByBehavior?.let { behavior ->
            smoothScrollBy(
                dx, dy,
                interpolator,
                behavior.configSmoothScrollByDuration(dx, dy)
            )
        } ?: smoothScrollBy(dx, dy, interpolator, UNDEFINED_DURATION)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        val result = super.startNestedScroll(axes, type)
        if (type == ViewCompat.TYPE_TOUCH) {
            startedTouchScroll = true
        }
        return result
    }

    override fun stopNestedScroll() {
        super.stopNestedScroll()
        startedTouchScroll = false
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            startedTouchScroll = false
            pivotLayoutManager?.setScrollingFromTouchEvent(false)
            if (hasPendingLayout) {
                scheduleLayout()
            }
        } else if (startedTouchScroll) {
            pivotLayoutManager?.setScrollingFromTouchEvent(true)
        }
    }

    private fun scheduleLayout() {
        if (DEBUG) {
            Log.i(TAG, "Scheduling pending layout request")
        }
        /**
         * The delay here is intended because users can request selections
         * while the layout was locked and in that case, we should honor those requests instead
         * of just performing a full layout
         */
        postDelayed({ requestLayout() }, 500L)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fadingEdge.onSizeChanged(w, h, oldw, oldh, this)
    }

    final override fun setFadingEdgeLength(length: Int) {
        super.setFadingEdgeLength(length)
        layoutManager?.let {
            enableMinEdgeFading(true)
            enableMaxEdgeFading(true)
            setMaxEdgeFadingLength(length)
            setMinEdgeFadingLength(length)
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        val applyMinEdgeFading = fadingEdge.isMinFadingEdgeRequired(this)
        val applyMaxEdgeFading = fadingEdge.isMaxFadingEdgeRequired(this)
        if (!applyMaxEdgeFading && !applyMinEdgeFading) {
            super.dispatchDraw(canvas)
            return
        }
        val minFadeLength = if (applyMinEdgeFading) fadingEdge.minShaderLength else 0
        val maxFadeLength = if (applyMaxEdgeFading) fadingEdge.maxShaderLength else 0
        val minEdge = fadingEdge.getMinEdge(this)
        val maxEdge = fadingEdge.getMaxEdge(this)

        val save = canvas.save()
        fadingEdge.clip(minEdge, maxEdge, applyMinEdgeFading, applyMaxEdgeFading, canvas, this)
        super.dispatchDraw(canvas)
        if (minFadeLength > 0) {
            fadingEdge.drawMin(canvas, this)
        }
        if (maxFadeLength > 0) {
            fadingEdge.drawMax(canvas, this)
        }
        canvas.restoreToCount(save)
    }

    /**
     * Sets the strategy for calculating extra layout space.
     *
     * Note that this is not supported if [DpadLoopDirection] is used
     * since that would potentially lead to duplicate items in the layout.
     *
     * Check [ExtraLayoutSpaceStrategy] for more information.
     */
    fun setExtraLayoutSpaceStrategy(strategy: ExtraLayoutSpaceStrategy?) {
        requireLayout().setExtraLayoutSpaceStrategy(strategy)
    }

    /**
     * Set whether the LayoutManager of this RecyclerView will recycle its children
     * when this RecyclerView is detached from the window.
     *
     * If you are re-using a [RecyclerView.RecycledViewPool], it might be a good idea to set
     * this flag to **true** so that views will be available to other RecyclerViews
     * immediately.
     *
     * @param recycle Whether children should be recycled in detach or not.
     */
    fun setRecycleChildrenOnDetach(recycle: Boolean) {
        requireLayout().setRecycleChildrenOnDetach(recycle)
    }

    /**
     * Controls the return value of [View.hasOverlappingRendering].
     * @param enabled true if overlapping rendering is enabled. Default is true
     */
    fun setHasOverlappingRendering(enabled: Boolean) {
        isOverlappingRenderingEnabled = enabled
    }

    /**
     * Allows disabling the layout of children. All children are removed if layout is disabled
     *
     * @param enabled true to enable layout, false otherwise.
     */
    fun setLayoutEnabled(enabled: Boolean) {
        requireLayout().setLayoutEnabled(enabled)
    }

    /**
     * See: [setLayoutEnabled]
     */
    fun isLayoutEnabled(): Boolean = requireLayout().isLayoutEnabled()

    /**
     * Updates the loop direction used by this [DpadRecyclerView].
     * By default, the layout does not loop around the items
     *
     * @param loopDirection the [DpadLoopDirection] to use for looping items
     */
    fun setLoopDirection(loopDirection: DpadLoopDirection) {
        requireLayout().setLoopDirection(loopDirection)
    }

    /**
     * See [setLoopDirection]
     */
    fun getLoopDirection(): DpadLoopDirection = requireLayout().getLoopDirection()

    /**
     * Enables fading out the min edge to transparent.
     * @param enable true if edge fading should be enabled for the left or top of the layout
     */
    fun enableMinEdgeFading(enable: Boolean) {
        fadingEdge.enableMinEdgeFading(enable, this)
    }

    /**
     * @return true if edge fading is enabled for the left or top of the layout
     */
    fun isMinEdgeFadingEnabled(): Boolean = fadingEdge.isFadingMinEdge

    /**
     * Sets the length of the fading effect applied to the min edge in pixels
     */
    fun setMinEdgeFadingLength(@Px length: Int) {
        fadingEdge.setMinEdgeFadingLength(length, this)
    }

    /**
     * See: [setMinEdgeFadingLength]
     */
    fun getMinEdgeFadingLength(): Int = fadingEdge.minShaderLength

    /**
     * Sets the start position of the fading effect applied to the min edge in pixels.
     * Default is 0, which means that the fading effect starts from the min edge (left or top)
     */
    fun setMinEdgeFadingOffset(@Px offset: Int) {
        fadingEdge.setMinEdgeFadingOffset(offset, this)
    }

    /**
     * See: [setMinEdgeFadingOffset]
     */
    fun getMinEdgeFadingOffset(): Int = fadingEdge.minShaderOffset

    /**
     * Enables fading out the max edge to transparent.
     * @param enable true if edge fading should be enabled for the right or bottom of the layout
     */
    fun enableMaxEdgeFading(enable: Boolean) {
        fadingEdge.enableMaxEdgeFading(enable, this)
    }

    /**
     * @return true if edge fading is enabled for the right or bottom of the layout
     */
    fun isMaxEdgeFadingEnabled(): Boolean = fadingEdge.isFadingMaxEdge

    /**
     * Sets the length of the fading effect applied to the max edge in pixels
     */
    fun setMaxEdgeFadingLength(@Px length: Int) {
        fadingEdge.setMaxEdgeFadingLength(length, this)
    }

    /**
     * See: [setMaxEdgeFadingLength]
     */
    fun getMaxEdgeFadingLength(): Int = fadingEdge.maxShaderLength

    /**
     * Sets the length of the fading effect applied to the min edge in pixels
     */
    fun setMaxEdgeFadingOffset(@Px offset: Int) {
        fadingEdge.setMaxEdgeFadingOffset(offset, this)
    }

    /**
     * See: [setMaxEdgeFadingOffset]
     */
    fun getMaxEdgeFadingOffset(): Int = fadingEdge.maxShaderOffset

    /**
     * Enables or disables the default rule of drawing the selected view after all other views.
     * Default is true
     *
     * @param enabled True to draw the selected child at last, false otherwise.
     */
    fun setFocusDrawingOrderEnabled(enabled: Boolean) {
        super.setChildrenDrawingOrderEnabled(enabled)
        requireLayout().setChildrenDrawingOrderEnabled(enabled)
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
        descendantFocusability = if (disabled) {
            FOCUS_BLOCK_DESCENDANTS
        } else {
            FOCUS_AFTER_DESCENDANTS
        }
        requireLayout().setFocusSearchDisabled(disabled)
    }

    /**
     * @return True if focus search is disabled.
     */
    fun isFocusSearchDisabled(): Boolean {
        return requireLayout().isFocusSearchDisabled()
    }

    /**
     * Changes how RecyclerView will find the next focusable view.
     * Check [FocusableDirection] for all supported directions.
     * Default is [FocusableDirection.STANDARD]
     */
    fun setFocusableDirection(direction: FocusableDirection) {
        requireLayout().setFocusableDirection(direction)
    }

    /**
     * @return the current [FocusableDirection]. Default is [FocusableDirection.STANDARD]
     */
    fun getFocusableDirection(): FocusableDirection {
        return requireLayout().getFocusableDirection()
    }

    /**
     * Disables or enables focus search while RecyclerView is animating item changes.
     * See [RecyclerView.isAnimating].
     *
     * This is disabled by default.
     *
     * @param enabled True to enable focus search while RecyclerView is animating item changes,
     * or false to disable
     */
    fun setFocusSearchEnabledDuringAnimations(enabled: Boolean) {
        requireLayout().setFocusSearchEnabledDuringAnimations(enabled)
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
        requireLayout().setFocusOutAllowed(throughFront, throughBack)
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
        requireLayout().setFocusOutSideAllowed(throughFront, throughBack)
    }

    /**
     * See [RecyclerView.LayoutManager.setItemPrefetchEnabled]
     *
     * @param enabled `True` if items should be prefetched in between traversals.
     */
    fun setItemPrefetchEnabled(enabled: Boolean) {
        requireLayout().isItemPrefetchEnabled = enabled
    }

    /**
     * See [RecyclerView.LayoutManager.isItemPrefetchEnabled]
     *
     * @return `True` if items should be prefetched in between traversals.
     */
    fun isItemPrefetchEnabled(): Boolean = requireLayout().isItemPrefetchEnabled

    /**
     * Sets the number of items to prefetch in
     * [RecyclerView.LayoutManager.collectInitialPrefetchPositions], which defines
     * how many inner items should be prefetched when this RecyclerView is nested
     * inside another RecyclerView.
     *
     * @param itemCount Number of items to prefetch
     * @see [RecyclerView.LayoutManager.isItemPrefetchEnabled]
     * @see [getInitialPrefetchItemCount]
     * @see [RecyclerView.LayoutManager.collectInitialPrefetchPositions]
     */
    fun setInitialPrefetchItemCount(itemCount: Int) {
        requireLayout().getConfig().setInitialPrefetchItemCount(itemCount)
    }

    /**
     * @return number of items to prefetch
     * @see [PivotLayoutManager.isItemPrefetchEnabled]
     * @see [setInitialPrefetchItemCount]
     * @see [RecyclerView.LayoutManager.collectInitialPrefetchPositions]
     */
    fun getInitialPrefetchItemCount(): Int = requireLayout().getConfig().initialPrefetchItemCount

    /**
     * Updates the [DpadSpanSizeLookup] used by the layout manager of this RecyclerView.
     * @param spanSizeLookup the new span size configuration
     */
    fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup) {
        requireLayout().setSpanSizeLookup(spanSizeLookup)
    }

    /**
     * See [setSpanSizeLookup]
     */
    fun getSpanSizeLookup(): DpadSpanSizeLookup = requireLayout().getSpanSizeLookup()

    /**
     * Updates the number of spans of the [PivotLayoutManager] used by this RecyclerView.
     * @param spans number of columns in vertical orientation,
     * or number of rows in horizontal orientation. Must be greater than 0
     */
    fun setSpanCount(spans: Int) {
        requireLayout().setSpanCount(spans)
    }

    /**
     * See [setSpanCount]
     */
    fun getSpanCount(): Int = requireLayout().getSpanCount()

    /**
     * Updates the orientation of the [PivotLayoutManager] used by this RecyclerView
     * @param orientation either [RecyclerView.VERTICAL] or [RecyclerView.HORIZONTAL]
     */
    fun setOrientation(orientation: Int) {
        requireLayout().setOrientation(orientation)
    }

    /**
     * @see setOrientation
     */
    fun getOrientation(): Int = requireLayout().getConfig().orientation

    /**
     * Used to reverse item traversal and layout order.
     * This behaves similar to the layout change for RTL views. When set to true, first item is
     * laid out at the end of the UI, second item is laid out before it etc.
     *
     * For horizontal layouts, it depends on the layout direction.
     *
     * When set to true:
     * - If this [DpadRecyclerView] is LTR, then it will layout from RTL.
     * - If it is RTL, it will layout from LTR.
     *
     * @param reverseLayout `True` to reverse the layout order
     */
    fun setReverseLayout(reverseLayout: Boolean) {
        requireLayout().setReverseLayout(reverseLayout)
    }

    /**
     * Returns if views are laid out from the opposite direction of the layout.
     *
     * @return If layout is reversed or not.
     * @see [setReverseLayout]
     */
    fun isLayoutReversed(): Boolean {
        return requireLayout().getConfig().reverseLayout
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
        requireLayout().setGravity(gravity)
    }

    /**
     * Updates the parent alignment configuration for child views of this RecyclerView
     * @param alignment the parent alignment configuration
     * @param smooth true if the alignment change should be animated
     */
    fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean = false) {
        requireLayout().setParentAlignment(alignment, smooth)
    }

    /**
     * @return the current parent alignment configuration
     */
    fun getParentAlignment(): ParentAlignment = requireLayout().getParentAlignment()

    /**
     * Updates the child alignment configuration for child views of this RecyclerView
     * @param alignment the child alignment configuration
     * @param smooth true if the alignment change should be animated
     */
    fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean = false) {
        requireLayout().setChildAlignment(alignment, smooth)
    }

    /**
     * @return the current child alignment configuration
     */
    fun getChildAlignment(): ChildAlignment = requireLayout().getChildAlignment()

    /**
     * Updates both parent and child alignments
     * @param parent the parent alignment configuration
     * @param child the child alignment configuration
     * @param smooth true if the alignment change should be animated
     */
    fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        requireLayout().setAlignments(parent, child, smooth)
    }

    /**
     * Enable or disable smooth scrolling to new focused position. By default, this is set to true.
     * When set to false, RecyclerView will scroll immediately to the focused view
     * without any animation.
     *
     * @param enabled true to smooth scroll to the new focused position, false to scroll immediately
     */
    fun setSmoothFocusChangesEnabled(enabled: Boolean) {
        requireLayout().setSmoothFocusChangesEnabled(enabled)
    }

    /**
     * Whenever the user triggers a focus change via a key event,
     * [DpadRecyclerView] will check if it already has [max] number of pending alignment changes
     * before dispatching focus to the next view.
     *
     * Example: User presses [KeyEvent.KEYCODE_DPAD_RIGHT] 5 times and [max] is 2.
     *
     * If focus is at position N, pressing [KeyEvent.KEYCODE_DPAD_RIGHT] 5 times
     * will only dispatch focus up to position N + max instead of N + 5
     *
     * Once a view is aligned to its final position
     * or [RecyclerView.getScrollState] is [RecyclerView.SCROLL_STATE_IDLE],
     * we consume that alignment change.
     *
     * @param max Maximum number of pending alignment changes
     */
    fun setSmoothScrollMaxPendingAlignments(max: Int) {
        requireLayout().setMaxPendingAlignments(max)
    }

    /**
     * See [setSmoothScrollMaxPendingAlignments]
     */
    fun getSmoothScrollMaxPendingAlignments(): Int {
        return requireLayout().getMaxPendingAlignments()
    }

    /**
     * When the user holds down a key, a lot of key events will be generated by the system.
     * These events are generated a lot faster than this [DpadRecyclerView] can scroll,
     * so these events need to be cached until the user stops pressing the key.
     *
     * If this value is too high, then scrolling will take place a lot longer after the key press
     * stops. And if this value is too low, [DpadRecyclerView] might miss many key events.
     *
     * The default value is 10.
     *
     * It might make sense to decrease this if [setSmoothScrollSpeedFactor] was increased
     * to avoid scrolling for too long after key presses are over.
     *
     * @param max Maximum number of pending key events to be remembered.
     */
    fun setSmoothScrollMaxPendingMoves(max: Int) {
        requireLayout().setMaxPendingMoves(max)
    }

    /**
     * See [setSmoothScrollMaxPendingMoves]
     */
    fun getSmoothScrollMaxPendingMoves(): Int {
        return requireLayout().getMaxPendingMoves()
    }

    /**
     * Set how slow the smooth scroller should run.
     * Example:
     * - When set to 2f, the smooth scroller is twice slower.
     * - When set to 0.5f, the smooth scroller is twice faster.
     *
     * The value is 1f by default.
     *
     * @param smoothScrollSpeedFactor Factor of how slow the smooth scroll is.
     */
    fun setSmoothScrollSpeedFactor(smoothScrollSpeedFactor: Float) {
        requireLayout().setSmoothScrollSpeedFactor(smoothScrollSpeedFactor)
    }

    /**
     * See [setSmoothScrollSpeedFactor].
     *
     * @return Factor of how slow the smooth scroller runs. Default value is 1f.
     */
    fun getSmoothScrollSpeedFactor(): Float {
        return requireLayout().getSmoothScrollSpeedFactor()
    }

    /**
     * Enables or disables scrolling.
     * When this is disabled, [DpadRecyclerView] can still change focus on DPAD events
     * unless [setFocusSearchDisabled] is also set.
     *
     * @param enabled true if scrolling should be enabled, false otherwise
     */
    fun setScrollEnabled(enabled: Boolean) {
        requireLayout().setScrollEnabled(enabled)
    }

    /**
     * See [setScrollEnabled].
     */
    fun isScrollEnabled(): Boolean = requireLayout().getConfig().isScrollEnabled

    /**
     * Changes the selected item immediately without any scroll animation.
     * @param position adapter position of the item to select
     */
    fun setSelectedPosition(position: Int) {
        requireLayout().selectPosition(position, subPosition = 0, smooth = false)
    }

    /**
     * Performs a task on a ViewHolder at a given position after scrolling to it.
     *
     * @param position Adapter position of the item to select
     * @param task     Task to executed on the ViewHolder at the given position
     */
    fun setSelectedPosition(position: Int, task: ViewHolderTask) {
        viewHolderTaskExecutor.schedule(position, task)
        requireLayout().selectPosition(position, subPosition = 0, smooth = false)
    }

    /**
     * Changes the selected item and runs an animation to scroll to the target position.
     * @param position Adapter position of the item to select
     */
    fun setSelectedPositionSmooth(position: Int) {
        requireLayout().selectPosition(position, subPosition = 0, smooth = true)
    }

    /**
     * Performs a task on a ViewHolder at a given position after scrolling to it.
     *
     * @param position Adapter position of the item to select
     * @param task     Task to executed on the ViewHolder at the given position
     */
    fun setSelectedPositionSmooth(position: Int, task: ViewHolderTask) {
        viewHolderTaskExecutor.schedule(position, task)
        requireLayout().selectPosition(position, subPosition = 0, smooth = true)
    }

    /**
     * Changes the main selection and sub selected view immediately without any scroll animation.
     * @param position Adapter position of the item to select
     * @param subPosition index of the alignment from [DpadViewHolder.getSubPositionAlignments]
     */
    fun setSelectedSubPosition(position: Int, subPosition: Int) {
        requireLayout().selectPosition(position, subPosition, smooth = false)
    }

    /**
     * Performs a task on a ViewHolder at a given position and sub position after scrolling to it.
     *
     * @param position Adapter position of the item to select
     * @param subPosition index of the alignment from [DpadViewHolder.getSubPositionAlignments]
     * @param task     Task to executed on the ViewHolder at the given position
     */
    fun setSelectedSubPosition(position: Int, subPosition: Int, task: ViewHolderTask) {
        viewHolderTaskExecutor.schedule(position, subPosition, task)
        requireLayout().selectPosition(position, subPosition, smooth = false)
    }

    /**
     * Changes the sub selected view immediately without any scroll animation.
     * @param subPosition index of the alignment from [DpadViewHolder.getSubPositionAlignments]
     */
    fun setSelectedSubPosition(subPosition: Int) {
        requireLayout().selectSubPosition(subPosition, smooth = false)
    }

    /**
     * Changes the sub selected view and runs and animation to scroll to it.
     * @param subPosition index of the alignment from [DpadViewHolder.getSubPositionAlignments]
     */
    fun setSelectedSubPositionSmooth(subPosition: Int) {
        requireLayout().selectSubPosition(subPosition, smooth = true)
    }

    /**
     * Changes the sub selected view and runs and animation to scroll to it.
     * @param position Adapter position of the item to select
     * @param subPosition index of the alignment from [DpadViewHolder.getSubPositionAlignments]
     */
    fun setSelectedSubPositionSmooth(position: Int, subPosition: Int) {
        requireLayout().selectPosition(position, subPosition, smooth = true)
    }

    /**
     * Performs a task on a ViewHolder at a given position and sub position after scrolling to it.
     *
     * @param position Adapter position of the item to select
     * @param subPosition index of the alignment from [DpadViewHolder.getSubPositionAlignments]
     * @param task     Task to executed on the ViewHolder at the given position
     */
    fun setSelectedSubPositionSmooth(position: Int, subPosition: Int, task: ViewHolderTask) {
        viewHolderTaskExecutor.schedule(position, subPosition, task)
        requireLayout().selectPosition(position, subPosition, smooth = true)
    }

    /**
     * @return the current selected position or [RecyclerView.NO_POSITION] if there's none
     */
    fun getSelectedPosition(): Int = pivotLayoutManager?.getSelectedPosition() ?: NO_POSITION

    /**
     * @return the current selected sub position or 0 if there's none
     */
    fun getSelectedSubPosition(): Int = pivotLayoutManager?.getSelectedSubPosition() ?: NO_POSITION

    /**
     * @return the number of available sub positions for the current selected item
     * or 0 if there's none. See [DpadViewHolder.getSubPositionAlignments]
     */
    fun getCurrentSubPositions(): Int = pivotLayoutManager?.getCurrentSubPositions() ?: 0

    /**
     * Similar to [LinearLayoutManager.findFirstVisibleItemPosition]
     *
     * @return The adapter position of the first visible item or [RecyclerView.NO_POSITION] if
     * there aren't any visible items
     */
    fun findFirstVisibleItemPosition(): Int {
        return pivotLayoutManager?.findFirstVisibleItemPosition() ?: NO_POSITION
    }

    /**
     * Similar to [LinearLayoutManager.findFirstCompletelyVisibleItemPosition]
     *
     * @return The adapter position of the first fully visible item or [RecyclerView.NO_POSITION] if
     * there aren't any fully visible items
     */
    fun findFirstCompletelyVisibleItemPosition(): Int {
        return pivotLayoutManager?.findFirstCompletelyVisibleItemPosition() ?: NO_POSITION
    }

    /**
     * Similar to [LinearLayoutManager.findLastVisibleItemPosition]
     *
     * @return The adapter position of the last visible item or [RecyclerView.NO_POSITION] if
     * there aren't any visible items
     */
    fun findLastVisibleItemPosition(): Int {
        return pivotLayoutManager?.findLastVisibleItemPosition() ?: NO_POSITION
    }

    /**
     * Similar to [LinearLayoutManager.findLastCompletelyVisibleItemPosition]
     *
     * @return The adapter position of the last fully visible item or [RecyclerView.NO_POSITION] if
     * there aren't any fully visible items
     */
    fun findLastCompletelyVisibleItemPosition(): Int {
        return pivotLayoutManager?.findLastCompletelyVisibleItemPosition() ?: NO_POSITION
    }

    /**
     * Registers a callback to be invoked when an item has been selected
     * @param listener The listener to be invoked.
     */
    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        requireLayout().addOnViewHolderSelectedListener(listener)
    }

    /**
     * Removes a listener added by [addOnViewHolderSelectedListener]
     * @param listener The listener to be removed.
     */
    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        requireLayout().removeOnViewHolderSelectedListener(listener)
    }

    /**
     * Clears all existing listeners added by [addOnViewHolderSelectedListener]
     */
    fun clearOnViewHolderSelectedListeners() {
        requireLayout().clearOnViewHolderSelectedListeners()
    }

    /**
     * Registers a callback to be invoked when an item has been focused
     * @param listener The listener to be invoked.
     */
    fun addOnViewFocusedListener(listener: OnViewFocusedListener) {
        requireLayout().addOnViewFocusedListener(listener)
    }

    /**
     * Removes a listener added by [addOnViewFocusedListener]
     * @param listener The listener to be removed.
     */
    fun removeOnViewFocusedListener(listener: OnViewFocusedListener) {
        requireLayout().removeOnViewFocusedListener(listener)
    }

    /**
     * Clears all existing listeners added by [addOnViewFocusedListener]
     */
    fun clearOnViewFocusedListeners() {
        requireLayout().clearOnViewFocusedListeners()
    }

    /**
     * Set a custom behavior for [smoothScrollBy]
     * @param behavior Custom behavior or null for the default behavior.
     */
    fun setSmoothScrollBehavior(behavior: SmoothScrollByBehavior?) {
        smoothScrollByBehavior = behavior
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
        requireLayout().addOnLayoutCompletedListener(listener)
    }

    /**
     * Removes a listener added by [addOnLayoutCompletedListener]
     * @param listener The listener to be removed.
     */
    fun removeOnLayoutCompletedListener(listener: OnLayoutCompletedListener) {
        requireLayout().removeOnLayoutCompletedListener(listener)
    }

    /**
     * Clears all listeners added by [addOnLayoutCompletedListener]
     */
    fun clearOnLayoutCompletedListeners() {
        requireLayout().clearOnLayoutCompletedListeners()
    }

    /**
     * Registers a callback to be invoked when an item of this [DpadRecyclerView] has been laid out.
     *
     * @param listener the listener to be invoked.
     */
    fun setOnChildLaidOutListener(listener: OnChildLaidOutListener?) {
        requireLayout().setOnChildLaidOutListener(listener)
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
     * Sets a listener for intercepting touch events
     *
     * @param listener the touch intercept listener
     */
    fun setOnTouchInterceptListener(listener: OnTouchInterceptListener?) {
        touchInterceptListener = listener
    }

    /**
     * @return the listener set by [setOnMotionInterceptListener]
     */
    fun getOnMotionInterceptListener(): OnMotionInterceptListener? = motionInterceptListener

    /**
     * By default, [DpadRecyclerView] allows triggering a layout-pass during scrolling.
     * However, there might be some cases where someone is interested in disabling this behavior,
     * for example:
     * 1. Compose animations trigger a full unnecessary layout-pass
     * 2. Content jumping around while scrolling is not ideal sometimes
     *
     * @param enabled true if layout requests should be possible while scrolling,
     * or false if they should be postponed until [RecyclerView.SCROLL_STATE_IDLE].
     * Default is true.
     */
    fun setLayoutWhileScrollingEnabled(enabled: Boolean) {
        layoutWhileScrollingEnabled = enabled
    }

    @VisibleForTesting
    internal fun detachFromWindow() {
        onDetachedFromWindow()
    }

    @VisibleForTesting
    internal fun attachToWindow() {
        onAttachedToWindow()
    }

    internal fun onNestedChildFocused(view: View) {
        pivotLayoutManager?.notifyNestedChildFocus(view)
    }

    private fun removeSelectionForRecycledViewHolders() {
        addRecyclerListener { holder ->
            val position = holder.absoluteAdapterPosition
            if (holder is DpadViewHolder
                && position != NO_POSITION
                && position == getSelectedPosition()
            ) {
                pivotLayoutManager?.removeCurrentViewHolderSelection()
            }
        }
    }

    private fun requireLayout(): PivotLayoutManager {
        return requireNotNull(pivotLayoutManager) {
            "PivotLayoutManager is null. Check for unnecessary usages of " +
                    "RecyclerView.setLayoutManager(null) or just set a new PivotLayoutManager."
        }
    }

    /**
     * [PivotLayoutManager] will draw the focused view on top of all other views by default
     */
    private inner class FocusableChildDrawingCallback : ChildDrawingOrderCallback {

        override fun onGetChildDrawingOrder(childCount: Int, i: Int): Int {
            val selectedPosition = pivotLayoutManager?.getSelectedPosition() ?: return i
            val view = pivotLayoutManager?.findViewByPosition(selectedPosition) ?: return i
            val focusIndex = indexOfChild(view)
            // Scenario: 0 1 2 3 4 5 6 7 8 9, 4 is the focused item
            // drawing order is: 0 1 2 3 9 8 7 6 5 4
            return if (i < focusIndex) {
                i
            } else if (i < childCount - 1) {
                focusIndex + childCount - 1 - i
            } else {
                focusIndex
            }
        }

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

    /**
     * Listener for intercepting touch dispatch events
     */
    interface OnTouchInterceptListener {
        /**
         * @return true if event should be consumed
         */
        fun onInterceptTouchEvent(event: MotionEvent): Boolean
    }

}
