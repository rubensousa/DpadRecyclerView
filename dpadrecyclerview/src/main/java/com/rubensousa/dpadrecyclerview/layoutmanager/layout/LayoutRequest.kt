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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout

import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

/**
 * Holds information required for the next layout
 */
internal class LayoutRequest {

    // The current direction of the layout stage
    var direction: LayoutDirection = LayoutDirection.END
        private set

    // The default direction in which the adapter is traversed
    var defaultItemDirection = ItemDirection.TAIL
        private set

    // The direction in which the adapter is traversed for the current layout stage
    var currentItemDirection = ItemDirection.TAIL
        private set

    // Number of pixels that we should fill, in the layout direction.
    var fillSpace = 0
        private set

    // Current position on the adapter to get the next item.
    var currentPosition = 0
        private set

    // True if we should start the layout from the opposite direction
    var reverseLayout = false
        private set

    // Recycling will be disabled during layout and enabled during scroll
    var isRecyclingEnabled = true
        private set

    var extraLayoutSpaceStart: Int = 0
        private set

    var extraLayoutSpaceEnd: Int = 0
        private set

    // How much we can scroll without adding new children at the edges
    var availableScrollSpace: Int = 0
        private set

    // Pixel offset where layout should start
    // For grids, this only serves as an indicator of where the next/previous row should be laid out
    var checkpoint: Int = 0
        private set

    var gravity: Int = Gravity.START
        private set

    var isVertical: Boolean = true
        private set

    // true if WRAP_CONTENT is used and we need to layout everything
    var isInfinite: Boolean = false
        private set

    var isLayingOutScrap: Boolean = false
        private set

    fun init(
        gravity: Int,
        isVertical: Boolean,
        reverseLayout: Boolean,
        infinite: Boolean
    ) {
        this.reverseLayout = reverseLayout
        this.gravity = gravity
        this.isVertical = isVertical
        this.isInfinite = infinite
        isRecyclingEnabled = false
        defaultItemDirection = if (reverseLayout) {
            ItemDirection.HEAD
        } else {
            ItemDirection.TAIL
        }
        currentItemDirection = defaultItemDirection
    }

    fun setCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun setRecyclingEnabled(enabled: Boolean) {
        isRecyclingEnabled = enabled
    }

    fun setFillSpace(space: Int) {
        fillSpace = max(0, space)
    }

    fun setCheckpoint(offset: Int) {
        checkpoint = offset
    }

    fun offsetCheckpoint(offset: Int) {
        checkpoint += offset
    }

    fun moveToNextPosition() {
        currentPosition += currentItemDirection.value
    }

    fun isLayingOutStart() = direction == LayoutDirection.START

    fun isLayingOutEnd() = direction == LayoutDirection.END

    fun setLayingOutScrap(layoutScrap: Boolean) {
        isLayingOutScrap = layoutScrap
    }

    fun setExtraLayoutSpace(start: Int, end: Int) {
        extraLayoutSpaceStart = start
        extraLayoutSpaceEnd = end
    }

    fun setAvailableScrollSpace(space: Int) {
        availableScrollSpace = space
    }

    fun clear() {
        availableScrollSpace = 0
        currentPosition = RecyclerView.NO_POSITION
        extraLayoutSpaceEnd = 0
        extraLayoutSpaceStart = 0
        fillSpace = 0
        checkpoint = 0
    }

    fun append(
        referencePosition: Int,
        itemDirection: ItemDirection = defaultItemDirection,
        block: LayoutRequest.() -> Unit
    ) {
        clear()
        direction = LayoutDirection.END
        currentItemDirection = itemDirection
        currentPosition = referencePosition + currentItemDirection.value
        block(this)
    }

    fun prepend(
        referencePosition: Int,
        itemDirection: ItemDirection = defaultItemDirection.opposite(),
        block: LayoutRequest.() -> Unit
    ) {
        clear()
        direction = LayoutDirection.START
        currentItemDirection = itemDirection
        currentPosition = referencePosition + currentItemDirection.value
        block(this)
    }

    override fun toString(): String {
        return "LayoutRequest(direction=$direction, " +
                "fillSpace=$fillSpace, " +
                "currentPosition=$currentPosition, " +
                "availableScrollSpace=$availableScrollSpace, " +
                "checkpoint=$checkpoint, "
    }

}
