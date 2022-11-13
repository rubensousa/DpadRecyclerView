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
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.FocusableDirection

internal class LayoutConfiguration {

    var orientation: Int = RecyclerView.VERTICAL
        private set

    var spanCount = 1
        private set

    var gravity = Gravity.TOP
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

    // Number of items to prefetch when first coming on screen with new data
    var initialPrefetchItemCount = 4
        private set

    var isSmoothFocusChangesEnabled = true
        private set

    var extraLayoutSpace: Int = 0
        private set

    var stackFromEnd = false
        private set

    var isChildDrawingOrderEnabled = true
        private set

    var spanSizeLookup: DpadSpanSizeLookup = DpadSpanSizeLookup.default()
        private set

    fun setFocusSearchDisabled(isDisabled: Boolean){
        isFocusSearchDisabled = isDisabled
    }

    fun setSmoothFocusChangesEnabled(isEnabled: Boolean) {
        isSmoothFocusChangesEnabled = isEnabled
    }

    fun setGravity(newGravity: Int) {
        gravity = newGravity
    }

    fun setOrientation(newOrientation: Int) {
        orientation = newOrientation
    }

    fun setSpanCount(count: Int) {
        spanCount = count
    }

    fun setSpanSizeLookup(spanSizeLookup: DpadSpanSizeLookup){
        this.spanSizeLookup = spanSizeLookup
    }

    fun setChildDrawingOrderEnabled(enabled: Boolean) {
        isChildDrawingOrderEnabled = enabled
    }

    fun isHorizontal() = orientation == RecyclerView.HORIZONTAL

    fun isVertical() = orientation == RecyclerView.VERTICAL

    fun setExtraLayoutSpace(value: Int) {
        extraLayoutSpace = value
    }

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

}
