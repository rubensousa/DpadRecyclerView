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

package com.rubensousa.dpadrecyclerview.layoutmanager

import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager.Properties
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.ExtraLayoutSpaceStrategy
import com.rubensousa.dpadrecyclerview.FocusableDirection
import kotlin.math.max

internal class LayoutConfiguration(properties: Properties) {

    var orientation: Int = RecyclerView.VERTICAL
        private set

    var spanCount = 1
        private set

    var gravity = Gravity.TOP.or(Gravity.START)
        private set

    /**
     * Allow dpad events to navigate out the front of the View at position 0
     * in the same direction of the orientation.
     * For horizontal orientation, this means navigating out from the start of the first view
     * For vertical orientation, this means navigation out from the top of the first view
     */
    var focusOutFront = false
        private set

    /**
     * Allow dpad events to navigate outside the View at the last position
     * in the same direction of the orientation.
     * For horizontal orientation, this means navigating out from the end of the last view
     * For vertical orientation, this means navigation out from the bottom of the last view
     */
    var focusOutBack = false
        private set

    /**
     * Allow dpad events to navigate outside the View at the last position
     * in the opposite direction of the orientation
     * For horizontal orientation, this means navigating out from the top of the last view
     * For vertical orientation, this means navigation out from the start of the last view
     */
    var focusOutSideFront = true
        private set

    /**
     * Allow dpad events to navigate outside the View at the last position
     * in the opposite direction of the orientation
     * For horizontal orientation, this means navigating out from the bottom of the last view
     * For vertical orientation, this means navigation out from the end of the last view
     */
    var focusOutSideBack = true
        private set

    var focusableDirection = FocusableDirection.STANDARD
        private set

    var reverseLayout = false
        private set

    var isScrollEnabled = true
        private set

    /**
     * If true, focus search won't work and there won't be selection changes from any key event
     */
    var isFocusSearchDisabled = false
        private set

    var isFocusSearchEnabledDuringAnimations = false
        private set

    // Number of items to prefetch when first coming on screen with new data
    var initialPrefetchItemCount = 4
        private set

    var isSmoothFocusChangesEnabled = true
        private set

    /**
     * Maximum number of pending focus changes that we can consume
     * while searching for a focusable pivot.
     */
    var maxPendingMoves = 10
        private set

    /**
     * Maximum number of pending scroll alignments after focus changes.
     * This is different from [maxPendingMoves] and by default we don't specify any limit
     */
    var maxPendingAlignments = Int.MAX_VALUE
        private set

    var isChildDrawingOrderEnabled = true
        private set

    var spanSizeLookup: DpadSpanSizeLookup = DpadSpanSizeLookup.default()
        private set

    var smoothScrollSpeedFactor: Float = 1f
        private set

    var extraLayoutSpaceStrategy: ExtraLayoutSpaceStrategy? = null
        private set

    var recycleChildrenOnDetach: Boolean = false
        private set

    init {
        setSpanCount(properties.spanCount)
        setOrientation(properties.orientation)
        setReverseLayout(properties.reverseLayout)
    }

    fun setRecycleChildrenOnDetach(recycle: Boolean) {
        recycleChildrenOnDetach = recycle
    }

    fun setFocusSearchDisabled(isDisabled: Boolean) {
        isFocusSearchDisabled = isDisabled
    }

    fun setSmoothFocusChangesEnabled(isEnabled: Boolean) {
        isSmoothFocusChangesEnabled = isEnabled
    }

    fun setGravity(newGravity: Int) {
        gravity = newGravity
    }

    fun setReverseLayout(isEnabled: Boolean) {
        reverseLayout = isEnabled
    }

    fun setOrientation(newOrientation: Int) {
        require(
            newOrientation == RecyclerView.HORIZONTAL || newOrientation == RecyclerView.VERTICAL
        ) {
            "Invalid orientation value. Must be RecyclerView.HORIZONTAL or RecyclerView.VERTICAL"
        }
        orientation = newOrientation
    }

    fun setSpanCount(count: Int) {
        spanCount = max(1, count)
    }

    fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup) {
        this.spanSizeLookup = spanSizeLookup
    }

    fun setChildDrawingOrderEnabled(enabled: Boolean) {
        isChildDrawingOrderEnabled = enabled
    }

    fun isHorizontal() = orientation == RecyclerView.HORIZONTAL

    fun isVertical() = orientation == RecyclerView.VERTICAL

    fun setFocusOutAllowed(throughFront: Boolean, throughBack: Boolean) {
        focusOutFront = throughFront
        focusOutBack = throughBack
    }

    fun setFocusOutSideAllowed(throughFront: Boolean, throughBack: Boolean) {
        focusOutSideFront = throughFront
        focusOutSideBack = throughBack
    }

    fun setFocusableDirection(direction: FocusableDirection) {
        focusableDirection = direction
    }

    fun setExtraLayoutSpaceStrategy(strategy: ExtraLayoutSpaceStrategy?) {
        extraLayoutSpaceStrategy = strategy
    }

    fun setFocusSearchEnabledDuringAnimations(enabled: Boolean) {
        isFocusSearchEnabledDuringAnimations = enabled
    }

    fun setMaxPendingAlignments(max: Int) {
        require(max > 0)
        maxPendingAlignments = max
    }

    fun setMaxPendingMoves(moves: Int) {
        require(moves > 0)
        maxPendingMoves = moves
    }

    fun setSmoothScrollSpeedFactor(speedFactor: Float) {
        require(speedFactor > 0f)
        smoothScrollSpeedFactor = speedFactor
    }

    fun setInitialPrefetchItemCount(count: Int) {
        require(count >= 0)
        initialPrefetchItemCount = count
    }

}
