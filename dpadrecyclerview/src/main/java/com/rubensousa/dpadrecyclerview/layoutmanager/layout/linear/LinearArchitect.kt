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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutState
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.OnChildLayoutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.StructureArchitect

internal class LinearArchitect(
    layoutManager: LayoutManager,
    layoutInfo: LayoutInfo,
    linearRecycler: LinearRecycler,
    onChildLayoutListener: OnChildLayoutListener,
    private val layoutAlignment: LayoutAlignment,
    private val configuration: LayoutConfiguration
) : StructureArchitect(layoutManager, layoutInfo, linearRecycler, onChildLayoutListener) {

    companion object {
        const val TAG = "RowArchitect"
    }

    override fun addPivot(view: View, position: Int, bounds: Rect, layoutState: LayoutState) {
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view)
        val headOffset = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tailOffset = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)
        if (layoutInfo.isVertical()) {
            bounds.top = headOffset
            bounds.bottom = tailOffset
            applyHorizontalGravity(view, bounds)
        } else {
            bounds.left = headOffset
            bounds.right = tailOffset
            applyVerticalGravity(view, bounds)
        }

        layoutState.updateWindow(start = headOffset, end = tailOffset)
    }

    override fun appendView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            // We need to align this view to an edge or center it, depending on the gravity set
            applyHorizontalGravity(view, bounds)
            bounds.top = layoutState.checkpoint
            bounds.bottom = bounds.top + decoratedSize
            bounds.height()
        } else {
            applyVerticalGravity(view, bounds)
            bounds.left = layoutState.checkpoint
            bounds.right = bounds.left + decoratedSize
            bounds.width()
        }
        layoutState.appendWindow(consumedSpace)
        return consumedSpace
    }

    override fun prependView(
        view: View,
        position: Int,
        bounds: Rect,
        layoutState: LayoutState
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            applyHorizontalGravity(view, bounds)
            bounds.bottom = layoutState.checkpoint
            bounds.top = bounds.bottom - decoratedSize
            bounds.height()
        } else {
            applyVerticalGravity(view, bounds)
            bounds.right = layoutState.checkpoint
            bounds.left = bounds.right - decoratedSize
            bounds.width()
        }
        layoutState.prependWindow(consumedSpace)
        return consumedSpace
    }

    private fun applyHorizontalGravity(view: View, bounds: Rect) {
        val horizontalGravity = if (configuration.reverseLayout) {
            Gravity.getAbsoluteGravity(
                configuration.gravity.and(Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK),
                View.LAYOUT_DIRECTION_RTL
            )
        } else {
            configuration.gravity.and(Gravity.HORIZONTAL_GRAVITY_MASK)
        }
        when (horizontalGravity) {
            Gravity.CENTER, Gravity.CENTER_HORIZONTAL -> {
                val width = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.left = layoutManager.width / 2 - width / 2
                bounds.right = bounds.left + width
            }
            Gravity.RIGHT -> {
                val width = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.right = layoutManager.width - layoutManager.paddingRight
                bounds.left = bounds.right - width
            }
            else -> { // Fallback to left gravity since this is the default expected behavior
                bounds.left = layoutManager.paddingLeft
                bounds.right = bounds.left + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }

    }

    private fun applyVerticalGravity(view: View, bounds: Rect) {
        when (configuration.gravity.and(Gravity.VERTICAL_GRAVITY_MASK)) {
            Gravity.CENTER, Gravity.CENTER_VERTICAL -> {
                val height = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.top = layoutManager.height / 2 - height / 2
                bounds.bottom = bounds.top + height
            }
            Gravity.BOTTOM -> {
                val height = layoutInfo.getPerpendicularDecoratedSize(view)
                bounds.bottom = layoutManager.height - layoutManager.paddingBottom
                bounds.top = bounds.bottom - height
            }
            else -> {  // Fallback to top gravity since this is the default expected behavior
                bounds.top = layoutManager.paddingTop
                bounds.bottom = bounds.top + layoutInfo.getPerpendicularDecoratedSize(view)
            }
        }
    }

}
