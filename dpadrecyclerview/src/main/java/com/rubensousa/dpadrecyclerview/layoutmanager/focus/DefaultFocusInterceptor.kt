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

package com.rubensousa.dpadrecyclerview.layoutmanager.focus

import android.view.FocusFinder
import android.view.View
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

/**
 * Implementation for [FocusableDirection.STANDARD]
 */
internal class DefaultFocusInterceptor(
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration,
    private val focusFinder: FocusFinder = FocusFinder.getInstance()
) : FocusInterceptor {

    override fun findFocus(
        recyclerView: DpadRecyclerView,
        focusedView: View,
        position: Int,
        direction: Int
    ): View? {
        val focusAbsoluteDirection = FocusDirection.getAbsoluteDirection(
            direction = direction,
            isVertical = configuration.isVertical(),
            reverseLayout = layoutInfo.shouldReverseLayout()
        )
        // Exit early if the focus finder can't find focus already
        val nextFocusFinderView = focusFinder.findNextFocus(
            recyclerView, focusedView, focusAbsoluteDirection
        ) ?: return null
        val currentViewHolder = layoutInfo.getChildViewHolder(focusedView)
        val nextViewHolder = layoutInfo.getChildViewHolder(nextFocusFinderView)
        /**
         * Check if the focus finder has found another focusable view for the same ViewHolder
         * This might happen when sub positions are used
         */
        if (nextViewHolder === currentViewHolder && nextFocusFinderView !== focusedView) {
            return nextFocusFinderView
        }

        return if (configuration.spanCount == 1) {
            val relativeFocusDirection = FocusDirection.from(
                direction, isVertical = configuration.isVertical(),
                reverseLayout = configuration.reverseLayout
            ) ?: return nextFocusFinderView
            /**
             * If the layout is looping, let the focus finder find the next focusable view
             * if we're searching for focus in the layout direction
             */
            if (layoutInfo.isLoopingAllowed && relativeFocusDirection.isPrimary()) {
                return nextFocusFinderView
            }
            findNextLinearChild(position, relativeFocusDirection)
        } else {
            return nextFocusFinderView
        }
    }

    private fun findNextLinearChild(position: Int, direction: FocusDirection): View? {
        // We only support the main direction
        if (direction.isSecondary()) {
            return null
        }
        val positionIncrement = layoutInfo.getPositionIncrement(
            goingForward = direction == FocusDirection.NEXT_ROW
                    || direction == FocusDirection.NEXT_COLUMN
        )
        val nextPosition = position + positionIncrement
        // Jump early if we're going out of bounds
        if (nextPosition < 0 || nextPosition == layoutInfo.getItemCount()) {
            return null
        }
        return findNextFocusableView(
            fromPosition = nextPosition,
            positionIncrement = positionIncrement
        )
    }

    private fun findNextFocusableView(
        fromPosition: Int,
        positionIncrement: Int
    ): View? {
        var currentPosition = fromPosition
        while (currentPosition >= 0 && currentPosition < layoutInfo.getItemCount()) {
            val view = layoutInfo.findViewByPosition(currentPosition)
            if (view != null && layoutInfo.isViewFocusable(view)) {
                return view
            }
            currentPosition += positionIncrement
        }
        return null
    }
}
