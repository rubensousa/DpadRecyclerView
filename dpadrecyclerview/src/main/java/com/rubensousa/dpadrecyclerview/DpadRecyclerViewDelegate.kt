package com.rubensousa.dpadrecyclerview

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator

/**
 * Takes care of most of the functionality of [DpadRecyclerView].
 * You can use this if you use your own [RecyclerView] and can't use [DpadRecyclerView] directly
 */
class DpadRecyclerViewDelegate(private val recyclerView: RecyclerView) {

    var smoothScrollByBehavior: DpadRecyclerView.SmoothScrollByBehavior? = null

    var layout: DpadGridLayoutManager? = null
        private set

    private var isRetainingFocus = false
    private val viewHolderTaskExecutor = ViewHolderTaskExecutor()

    fun init(context: Context, attrs: AttributeSet?) {
        layout = createLayoutManager(context, attrs)
        recyclerView.apply {
            // The LayoutManager will restore focus and scroll automatically when needed
            preserveFocusAfterLayout = false

            // Focus a ViewHolder's view first by default if one exists
            descendantFocusability = RecyclerView.FOCUS_AFTER_DESCENDANTS
            isFocusable = true
            isFocusableInTouchMode = true

            // Typically all RecyclerViews have a fixed size, so this is a safe default
            setHasFixedSize(true)

            /**
             * Disable change animation by default due to focus problems when animating.
             * The change animation will create a new temporary view and cause undesired
             * focus animation between the old view and new view.
             */
            (itemAnimator as SimpleItemAnimator?)?.supportsChangeAnimations = false

            setWillNotDraw(true)
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            layoutManager = layout
        }
    }

    fun setLayoutManager(layoutManager: RecyclerView.LayoutManager?) {
        layout?.setRecyclerView(null)
        layout = null

        if (layoutManager != null && layoutManager !is DpadGridLayoutManager) {
            throw IllegalArgumentException(
                "Only DpadGridLayoutManager is supported, but got $layoutManager"
            )
        }
        if (layoutManager is DpadGridLayoutManager) {
            layoutManager.setRecyclerView(recyclerView, true)
            layoutManager.addOnViewHolderSelectedListener(viewHolderTaskExecutor)
            layout = layoutManager
        }
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
            0 -> RecyclerView.HORIZONTAL
            1 -> RecyclerView.VERTICAL
            else -> throw IllegalArgumentException(
                "Orientation must be either HORIZONTAL or VERTICAL"
            )
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

    fun focusSearch(focused: View?, direction: Int): View? {
        if (focused == null) {
            return null
        }
        return layout?.onInterceptFocusSearch(focused, direction)
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
        layout?.spanCount = spans
    }

    fun setOrientation(orientation: Int) {
        layout?.orientation = orientation
    }

    fun setGravity(gravity: Int) {
        layout?.setGravity(gravity)
    }

    fun setSelectedPosition(position: Int, smooth: Boolean, task: ViewHolderTask) {
        viewHolderTaskExecutor.schedule(position, task)
        setSelectedPosition(position, smooth)
    }

    fun setSelectedPosition(position: Int, smooth: Boolean) {
        if (smooth) {
            recyclerView.smoothScrollToPosition(position)
        } else {
            recyclerView.scrollToPosition(position)
        }
    }

    fun setSelectedPosition(position: Int, subPosition: Int, smooth: Boolean) {
        layout?.selectPosition(position, subPosition, smooth)
    }

    fun getSelectedPosition() = layout?.selectedPosition ?: RecyclerView.NO_POSITION

    fun getSelectedSubPosition() = layout?.subSelectionPosition ?: RecyclerView.NO_POSITION

    fun getCurrentSubPositions() = layout?.getCurrentSubSelectionCount() ?: 0

    fun setSelectedSubPosition(subPosition: Int, smooth: Boolean) {
        layout?.selectSubPosition(subPosition, smooth)
    }

    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        layout?.setFocusOutAllowed(throughFront, throughBack)
    }

    fun setFocusOppositeOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        layout?.setFocusOppositeOutAllowed(throughFront, throughBack)
    }

    fun addOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        layout?.addOnViewHolderSelectedListener(listener)
    }

    fun removeOnViewHolderSelectedListener(listener: OnViewHolderSelectedListener) {
        layout?.removeOnViewHolderSelectedListener(listener)
    }

    fun clearOnChildViewHolderSelectedListeners() {
        layout?.clearOnViewHolderSelectedListeners()
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
            if (position == targetPosition && child != null) {
                executePendingTask(child)
            }
        }

        private fun executePendingTask(viewHolder: RecyclerView.ViewHolder) {
            pendingTask?.run(viewHolder)
            pendingTask = null
            targetPosition = RecyclerView.NO_POSITION
        }

    }

}
