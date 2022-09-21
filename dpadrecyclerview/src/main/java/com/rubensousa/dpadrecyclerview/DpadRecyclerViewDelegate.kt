package com.rubensousa.dpadrecyclerview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Takes care of most of the functionality of [DpadRecyclerView].
 * You can use this if you use your own [RecyclerView] and can't use [DpadRecyclerView] directly
 */
class DpadRecyclerViewDelegate(private val recyclerView: RecyclerView) {

    var smoothScrollByBehavior: DpadRecyclerView.SmoothScrollByBehavior? = null

    private var layout: DpadLayoutManager? = null
    private var isRetainingFocus = false
    private var hasOverlappingRendering = true
    private val viewHolderTaskExecutor = ViewHolderTaskExecutor()

    fun init(context: Context, attrs: AttributeSet?) {
        layout = createLayoutManager(context, attrs)
        recyclerView.apply {
            // The LayoutManager will restore focus and scroll automatically when needed
            preserveFocusAfterLayout = false

            // Focus a ViewHolder's view first by default if one exists
            descendantFocusability = RecyclerView.FOCUS_AFTER_DESCENDANTS

            // Typically all RecyclerViews have a fixed size, so this is a safe default
            setHasFixedSize(true)

            /**
             * Disable change animation by default due to focus problems when animating.
             * The change animation will create a new temporary view and cause undesired
             * focus animation between the old view and new view.
             */
            val itemAnimator = itemAnimator as DefaultItemAnimator
            itemAnimator.supportsChangeAnimations = false

            setWillNotDraw(true)
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            layoutManager = layout
        }
    }

    fun setLayoutManager(layoutManager: RecyclerView.LayoutManager?) {
        layout?.removeOnViewHolderSelectedListener(viewHolderTaskExecutor)
        layout?.setRecyclerView(null)
        layout = null

        if (layoutManager != null && layoutManager !is DpadLayoutManager) {
            throw IllegalArgumentException(
                "Only DpadGridLayoutManager is supported, but got $layoutManager"
            )
        }
        if (layoutManager is DpadLayoutManager) {
            layoutManager.setRecyclerView(recyclerView, true)
            layoutManager.addOnViewHolderSelectedListener(viewHolderTaskExecutor)
            layout = layoutManager
        }
    }

    private fun createLayoutManager(context: Context, attrs: AttributeSet?): DpadLayoutManager {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.DpadRecyclerView,
            R.attr.dpadRecyclerViewStyle, 0
        )
        val orientation = when (
            typedArray.getInt(
                R.styleable.DpadRecyclerView_android_orientation,
                RecyclerView.VERTICAL
            )
        ) {
            0 -> RecyclerView.HORIZONTAL
            1 -> RecyclerView.VERTICAL
            else -> throw IllegalArgumentException(
                "Orientation must be either HORIZONTAL or VERTICAL"
            )
        }
        val layout = DpadLayoutManager(
            context,
            spanCount = typedArray.getInt(
                R.styleable.DpadRecyclerView_spanCount, 1
            ),
            orientation = orientation,
            reverseLayout = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_reverseLayout, false
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
        layout.setFocusOutSideAllowed(
            throughFront = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutSideFront, true
            ),
            throughBack = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewFocusOutSideBack, true
            )
        )
        layout.setCircularFocusEnabled(
            typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewCircularFocusEnabled, false
            )
        )
        if (typedArray.hasValue(R.styleable.DpadRecyclerView_android_gravity)) {
            layout.setGravity(
                typedArray.getInt(R.styleable.DpadRecyclerView_android_gravity, Gravity.NO_GRAVITY)
            )
        }
        val parentAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.values()[typedArray.getInt(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentEdge,
                ParentAlignment.DEFAULT_EDGE.ordinal
            )],
            offset = typedArray.getDimensionPixelSize(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentOffset,
                ViewAlignment.DEFAULT_OFFSET
            ),
            offsetRatio = typedArray.getFloat(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentOffsetRatio,
                ViewAlignment.DEFAULT_OFFSET_RATIO
            ),
            isOffsetRatioEnabled = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewParentAlignmentOffsetRatioEnabled,
                true
            )
        )
        val childAlignment = ChildAlignment(
            offset = typedArray.getDimensionPixelSize(
                R.styleable.DpadRecyclerView_dpadRecyclerViewChildAlignmentOffset,
                ViewAlignment.DEFAULT_OFFSET
            ),
            offsetRatio = typedArray.getFloat(
                R.styleable.DpadRecyclerView_dpadRecyclerViewChildAlignmentOffsetRatio,
                ViewAlignment.DEFAULT_OFFSET_RATIO
            ),
            isOffsetRatioEnabled = typedArray.getBoolean(
                R.styleable.DpadRecyclerView_dpadRecyclerViewChildAlignmentOffsetRatioEnabled,
                true
            )
        )
        layout.setAlignments(parentAlignment, childAlignment, smooth = false)
        typedArray.recycle()
        return layout
    }

    fun focusSearch(focused: View?, direction: Int): View? {
        if (focused == null) {
            return null
        }
        return layout?.onInterceptFocusSearch(focused, direction)
    }

    fun onRtlPropertiesChanged() {
        layout?.onRtlPropertiesChanged()
    }

    fun onFocusChanged(gainFocus: Boolean) {
        layout?.onFocusChanged(gainFocus)
    }

    fun focusSearch(direction: Int): View? {
        val currentLayout = layout
        if (recyclerView.isFocused && currentLayout != null) {
            // focusSearch will be called when RecyclerView itself is focused.
            // Calling focusSearch(view, int) to get next sibling of current selected child.
            val view = currentLayout.findViewByPosition(currentLayout.selectedPosition)
            if (view != null) {
                return focusSearch(view, direction)
            }
        }
        return null
    }

    fun onRequestFocusInDescendants(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return if (isRetainingFocus) {
            /**
             * Don't focus to child if RecyclerView is already retaining focus temporarily
             * from a previous removeView or removeViewAt
             */
            false
        } else {
            layout?.onRequestFocusInDescendants(direction, previouslyFocusedRect) ?: false
        }
    }

    fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        return layout?.getChildDrawingOrder(childCount, i) ?: i
    }

    fun removeView(view: View) {
        val retainFocusForChild = view.hasFocus() && recyclerView.isFocusable
        if (retainFocusForChild) {
            isRetainingFocus = true
            recyclerView.requestFocus()
        }
    }

    fun removeViewAt(index: Int) {
        val retainFocusForChild = recyclerView.getChildAt(index)?.hasFocus() ?: false
        if (retainFocusForChild) {
            isRetainingFocus = true
            recyclerView.requestFocus()
        }
    }

    fun setRemoveViewFinished() {
        isRetainingFocus = false
    }

    fun smoothScrollBy(dx: Int, dy: Int) {
        smoothScrollByBehavior?.let { behavior ->
            recyclerView.smoothScrollBy(
                dx, dy,
                behavior.configSmoothScrollByInterpolator(dx, dy),
                behavior.configSmoothScrollByDuration(dx, dy)
            )
        } ?: recyclerView.smoothScrollBy(dx, dy, null, RecyclerView.UNDEFINED_DURATION)
    }

    fun smoothScrollBy(dx: Int, dy: Int, interpolator: Interpolator?) {
        smoothScrollByBehavior?.let { behavior ->
            recyclerView.smoothScrollBy(
                dx, dy,
                interpolator,
                behavior.configSmoothScrollByDuration(dx, dy)
            )
        } ?: recyclerView.smoothScrollBy(dx, dy, interpolator, RecyclerView.UNDEFINED_DURATION)
    }

    fun setSpanCount(spans: Int) {
        requireLayout().spanCount = spans
    }

    fun setOrientation(orientation: Int) {
        requireLayout().orientation = orientation
    }

    fun setGravity(gravity: Int) {
        requireLayout().setGravity(gravity)
    }

    fun setSelectedPosition(position: Int, smooth: Boolean) {
        requireLayout().selectPosition(position, subPosition = 0, smooth)
    }

    fun setSelectedPosition(position: Int, task: ViewHolderTask, smooth: Boolean) {
        viewHolderTaskExecutor.schedule(position, task)
        setSelectedPosition(position, smooth)
    }

    fun setSelectedSubPosition(position: Int, subPosition: Int, smooth: Boolean) {
        requireLayout().selectPosition(position, subPosition, smooth)
    }

    fun getSelectedPosition() = layout?.selectedPosition ?: RecyclerView.NO_POSITION

    fun getSelectedSubPosition() = layout?.subSelectionPosition ?: RecyclerView.NO_POSITION

    fun getCurrentSubPositions() = layout?.getCurrentSubSelectionCount() ?: 0

    fun setSelectedSubPosition(subPosition: Int, smooth: Boolean) {
        requireLayout().selectSubPosition(subPosition, smooth)
    }

    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        requireLayout().setFocusOutAllowed(throughFront, throughBack)
    }

    fun setFocusOutSideAllowed(throughFront: Boolean, throughBack: Boolean) {
        requireLayout().setFocusOutSideAllowed(throughFront, throughBack)
    }

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        requireLayout().addOnViewHolderSelectedListener(listener)
    }

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        requireLayout().removeOnViewHolderSelectedListener(listener)
    }

    fun clearOnViewHolderSelectedListeners() {
        requireLayout().clearOnViewHolderSelectedListeners()
    }

    fun getSpanCount(): Int = layout?.spanCount ?: 0

    fun setAlignments(parent: ParentAlignment, child: ChildAlignment, smooth: Boolean) {
        requireLayout().setAlignments(parent, child, smooth)
    }

    fun setParentAlignment(alignment: ParentAlignment, smooth: Boolean = false) {
        requireLayout().setParentAlignment(alignment, smooth)
    }

    fun getParentAlignment() = requireLayout().getParentAlignment()

    fun setChildAlignment(alignment: ChildAlignment, smooth: Boolean = false) {
        requireLayout().setChildAlignment(alignment, smooth)
    }

    fun getChildAlignment() = requireLayout().getChildAlignment()

    fun setSpanSizeLookup(spanSizeLookup: GridLayoutManager.SpanSizeLookup) {
        requireLayout().spanSizeLookup = spanSizeLookup
    }

    fun addOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        requireLayout().addOnLayoutCompletedListener(listener)
    }

    fun removeOnLayoutCompletedListener(listener: DpadRecyclerView.OnLayoutCompletedListener) {
        requireLayout().removeOnLayoutCompletedListener(listener)
    }

    fun clearOnLayoutCompletedListeners() {
        requireLayout().clearOnLayoutCompletedListeners()
    }

    fun hasOverlappingRendering(): Boolean = hasOverlappingRendering

    fun setHasOverlappingRendering(enabled: Boolean) {
        this.hasOverlappingRendering = enabled
    }

    fun setFocusSearchDisabled(disabled: Boolean) {
        recyclerView.descendantFocusability = if (disabled) {
            RecyclerView.FOCUS_BLOCK_DESCENDANTS
        } else {
            RecyclerView.FOCUS_AFTER_DESCENDANTS
        }
        requireLayout().setFocusSearchDisabled(disabled)
    }

    fun isFocusSearchDisabled(): Boolean {
        return requireLayout().isFocusSearchDisabled()
    }

    fun requireLayout(): DpadLayoutManager {
        return requireNotNull(layout) {
            "LayoutManager is null. You need to call RecyclerView.setLayoutManager"
        }
    }

    private class ViewHolderTaskExecutor : OnViewHolderSelectedListener {

        private var targetPosition = RecyclerView.NO_POSITION
        private var pendingTask: ViewHolderTask? = null

        fun schedule(position: Int, task: ViewHolderTask) {
            targetPosition = position
            pendingTask = task
        }

        override fun onViewHolderSelected(
            parent: RecyclerView,
            child: RecyclerView.ViewHolder?,
            position: Int,
            subPosition: Int
        ) {
            if (position == targetPosition
                && child != null
                && pendingTask?.executeWhenAligned == false
            ) {
                executePendingTask(child)
            }
        }

        override fun onViewHolderSelectedAndAligned(
            parent: RecyclerView,
            child: RecyclerView.ViewHolder?,
            position: Int,
            subPosition: Int
        ) {
            if (position == targetPosition
                && child != null
                && pendingTask?.executeWhenAligned == true
            ) {
                executePendingTask(child)
            }
        }

        private fun executePendingTask(viewHolder: RecyclerView.ViewHolder) {
            pendingTask?.execute(viewHolder)
            pendingTask = null
            targetPosition = RecyclerView.NO_POSITION
        }

    }

}
