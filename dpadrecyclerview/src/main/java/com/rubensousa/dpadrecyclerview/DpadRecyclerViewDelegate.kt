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
import android.graphics.Rect
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.animation.Interpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutManagerDelegate

/**
 * Takes care of most of the functionality of [DpadRecyclerView]
 */
internal class DpadRecyclerViewDelegate(private val recyclerView: RecyclerView) {

    var smoothScrollByBehavior: DpadRecyclerView.SmoothScrollByBehavior? = null

    private var layoutDelegate: PivotLayoutManagerDelegate? = null
    private var layoutManagerImpl: LayoutManager? = null
    private var isRetainingFocus = false
    private var hasOverlappingRendering = true
    private val viewHolderTaskExecutor = ViewHolderTaskExecutor()

    fun init(context: Context, attrs: AttributeSet?) {
        layoutDelegate = createLayoutManager(context, attrs)
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
            layoutManager = layoutManagerImpl
        }
    }

    fun setLayoutManager(layoutManager: LayoutManager?) {
        layoutDelegate?.removeOnViewHolderSelectedListener(viewHolderTaskExecutor)
        layoutDelegate?.setRecyclerView(null)
        layoutDelegate = null

        if (layoutManager != null && layoutManager !is PivotLayoutManagerDelegate) {
            throw IllegalArgumentException(
                "Only PivotLayoutManagerDelegate is supported, but got $layoutManager"
            )
        }
        if (layoutManager is PivotLayoutManagerDelegate) {
            layoutManager.setRecyclerView(recyclerView)
            layoutManager.addOnViewHolderSelectedListener(viewHolderTaskExecutor)
            layoutDelegate = layoutManager
        }
    }

    private fun createLayoutManager(
        context: Context,
        attrs: AttributeSet?
    ): PivotLayoutManagerDelegate {
        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.DpadRecyclerView,
            R.attr.dpadRecyclerViewStyle, 0
        )
        val properties = LayoutManager.getProperties(context, attrs, 0, 0)
        val useNewLayoutManager = properties.spanCount == 1
                && typedArray.getBoolean(
            R.styleable.DpadRecyclerView_dpadRecyclerViewPivotLayoutManager,
            DpadRecyclerViewHelper.isNewPivotLayoutManagerEnabled
        )
        layoutManagerImpl = if (useNewLayoutManager) {
            PivotLayoutManager(properties)
        } else {
            DpadLayoutManager(context, properties)
        }
        val layout = layoutManagerImpl as PivotLayoutManagerDelegate
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
        return layoutDelegate?.onInterceptFocusSearch(focused, direction)
    }

    fun onRtlPropertiesChanged() {
        layoutDelegate?.onRtlPropertiesChanged()
    }

    fun onFocusChanged(gainFocus: Boolean) {
        layoutDelegate?.onFocusChanged(gainFocus)
    }

    fun focusSearch(direction: Int): View? {
        val currentLayout = layoutDelegate
        if (recyclerView.isFocused && currentLayout != null) {
            // focusSearch will be called when RecyclerView itself is focused.
            // Calling focusSearch(view, int) to get next sibling of current selected child.
            val view = layoutManagerImpl?.findViewByPosition(currentLayout.getSelectedPosition())
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
            layoutDelegate?.onRequestFocusInDescendants(direction, previouslyFocusedRect) ?: false
        }
    }

    fun setFocusableDirection(direction: FocusableDirection) {
        requireLayout().setFocusableDirection(direction)
    }

    fun getFocusableDirection() = requireLayout().getFocusableDirection()

    fun getChildDrawingOrder(childCount: Int, drawingOrderPosition: Int): Int {
        val selectedPosition = layoutDelegate?.getSelectedPosition() ?: return drawingOrderPosition
        val view =
            layoutManagerImpl?.findViewByPosition(selectedPosition) ?: return drawingOrderPosition
        val focusIndex = recyclerView.indexOfChild(view)
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

    fun setSmoothFocusChangesEnabled(enabled: Boolean) {
        requireLayout().setSmoothFocusChangesEnabled(enabled)
    }

    fun setSpanCount(spans: Int) {
        requireLayout().setSpanCount(spans)
    }

    fun setOrientation(orientation: Int) {
        requireLayout().setOrientation(orientation)
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

    fun getSelectedPosition() = layoutDelegate?.getSelectedPosition() ?: RecyclerView.NO_POSITION

    fun getSelectedSubPosition() = layoutDelegate?.getSelectedSubPosition() ?: 0

    fun getCurrentSubPositions() = layoutDelegate?.getCurrentSubPositions() ?: 0

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

    fun getSpanCount(): Int = layoutDelegate?.getSpanCount() ?: 0

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

    fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup) {
        requireLayout().setSpanSizeLookup(spanSizeLookup)
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

    fun setExtraLayoutSpaceStrategy(strategy: ExtraLayoutSpaceStrategy?) {
        requireLayout().setExtraLayoutSpaceStrategy(strategy)
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

    fun requireLayout(): PivotLayoutManagerDelegate {
        return requireNotNull(layoutDelegate) {
            "LayoutManager is null. You need to call RecyclerView.setLayoutManager"
        }
    }

    fun setChildrenDrawingOrderEnabled(enabled: Boolean) {
        requireLayout().setChildrenDrawingOrderEnabled(enabled)
    }

}
