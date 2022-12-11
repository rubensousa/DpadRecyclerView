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

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapted from LinearLayoutManager
 * Holds information required for the next layout
 */
internal class LayoutState {

    private val window = LayoutWindow()
    private val scrap = LayoutScrap()

    // The current direction of the layout stage
    var direction: LayoutDirection = LayoutDirection.END
        private set

    // The current direction in which the adapter is traversed
    var itemDirection = ItemDirection.TAIL
        private set

    // Number of pixels that we should fill, in the layout direction.
    var fillSpace = 0
        private set

    // Current position on the adapter to get the next item.
    var currentPosition = 0
        private set

    /**
     * When consuming [scrap], if this value is set to true,
     * we skip removed views since they should not be laid out in post layout step.
     */
    var isPreLayout = false
        private set

    // True if we should start the layout from the opposite direction
    var reverseLayout = false
        private set

    // Recycling will be disabled during layout and enabled during scroll
    var isRecyclingEnabled = true
        private set

    // The extra layout space calculated from the LayoutManager
    var extraLayoutSpaceStart: Int = 0
        private set

    // The extra layout space calculated from the LayoutManager
    var extraLayoutSpaceEnd: Int = 0
        private set

    // How much we can scroll without adding new children at the edges
    var availableScrollSpace: Int = 0
        private set

    // Pixel offset where layout should start
    var checkpoint: Int = 0
        private set

    fun setCurrentPosition(position: Int) {
        currentPosition = position
    }

    fun setPreLayout(isPreLayout: Boolean) {
        this.isPreLayout = isPreLayout
    }

    fun setReverseLayout(enabled: Boolean) {
        reverseLayout = enabled
    }

    fun setRecyclingEnabled(enabled: Boolean) {
        isRecyclingEnabled = enabled
    }

    fun setFillSpace(space: Int) {
        fillSpace = space
    }

    fun getStartOffset() = window.startOffset

    fun getEndOffset() = window.endOffset

    // true if WRAP_CONTENT is used and we need to layout everything
    fun isInfinite() = window.isInfinite

    fun setCheckpoint(offset: Int) {
        checkpoint = offset
    }

    fun updateWindow(pivotInfo: PivotInfo) {
        window.startOffset = pivotInfo.headOffset
        window.endOffset = pivotInfo.tailOffset
    }

    fun offsetWindow(offset: Int) {
        window.offset(offset)
    }

    fun appendWindow(offset: Int) {
        window.append(offset)
        checkpoint += offset
    }

    fun prependWindow(offset: Int) {
        window.prepend(offset)
        checkpoint -= offset
    }

    fun hasMoreItems(state: RecyclerView.State): Boolean {
        return currentPosition >= 0 && currentPosition < state.itemCount
    }

    /**
     * Gets the view for the next element that we should layout.
     * Also updates current item index to the next item, based on the [itemDirection]
     *
     * @return The next element that we should layout.
     */
    fun getNextView(recycler: RecyclerView.Recycler): View? {
        if (scrap.exists()) {
            val scrapView = scrap.getNextScrapView()
            if (scrapView != null) {
                scrap.updateCurrentPositionFromScrap(scrapView)
            }
            return scrapView
        }
        val view = recycler.getViewForPosition(currentPosition)
        currentPosition += itemDirection.value
        return view
    }

    fun setScrap(scrappedViews: List<RecyclerView.ViewHolder>?) {
        scrap.update(scrappedViews)
    }

    fun updateCurrentPositionFromScrap() {
        scrap.updateCurrentPositionFromScrap(ignoredView = null)
    }

    fun isLayingOutStart() = direction == LayoutDirection.START

    fun isLayingOutEnd() = direction == LayoutDirection.END

    fun setExtraLayoutSpace(extraSpace: Int) {
        extraLayoutSpaceStart = extraSpace
        extraLayoutSpaceEnd = extraSpace
    }

    fun setExtraLayoutSpaceStart(extraSpace: Int) {
        extraLayoutSpaceStart = extraSpace
    }

    fun setExtraLayoutSpaceEnd(extraSpace: Int) {
        extraLayoutSpaceEnd = extraSpace
    }

    fun setStartDirection() {
        direction = LayoutDirection.START
        itemDirection = if (reverseLayout) {
            ItemDirection.TAIL
        } else {
            ItemDirection.HEAD
        }
    }

    fun setEndDirection() {
        direction = LayoutDirection.END
        itemDirection = if (reverseLayout) {
            ItemDirection.HEAD
        } else {
            ItemDirection.TAIL
        }
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
        window.startOffset = 0
        window.endOffset = 0
    }

    override fun toString(): String {
        return "LayoutState(window=$window, " +
                "direction=$direction, " +
                "fillSpace=$fillSpace, " +
                "currentPosition=$currentPosition, " +
                "isPreLayout=$isPreLayout, " +
                "availableScrollSpace=$availableScrollSpace, " +
                "checkpoint=$checkpoint, "
    }

    fun isUsingScrap(): Boolean = scrap.exists()

    /**
     * Represents the current layout structure
     */
    private inner class LayoutWindow {

        /**
         * Current start position of the laid out views
         */
        var startOffset = 0

        /**
         * Current end position of the laid out views
         */
        var endOffset = 0

        /**
         * Used when there is no limit in how many views can be laid out.
         */
        var isInfinite = false

        fun append(offset: Int) {
            endOffset += offset
        }

        fun prepend(offset: Int) {
            startOffset -= offset
        }

        fun offset(offset: Int) {
            startOffset += offset
            endOffset += offset
        }

        override fun toString(): String {
            return "LayoutWindow(startOffset=$startOffset, endOffset=$endOffset, isInfinite=$isInfinite)"
        }
    }

    /**
     * Holds views pending recycling or removal
     */
    private inner class LayoutScrap {

        private var scrappedViews: List<RecyclerView.ViewHolder>? = null

        fun exists(): Boolean {
            return scrappedViews != null
        }

        fun update(views: List<RecyclerView.ViewHolder>?) {
            scrappedViews = views
        }

        /**
         * Returns the next item from the scrap list.
         */
        fun getNextScrapView(): View? {
            scrappedViews?.forEach { viewHolder ->
                val view = viewHolder.itemView
                val layoutParams = view.layoutParams as RecyclerView.LayoutParams
                if (!layoutParams.isItemRemoved
                    && currentPosition == layoutParams.viewLayoutPosition
                ) {
                    return view
                }
            }
            return null
        }

        /**
         * Updates the layout position to the one from the nearest view to the current position
         * to ensure layout continuity.
         *
         * New current item position will be: VH.layoutPosition + itemDirection
         */
        fun updateCurrentPositionFromScrap(ignoredView: View?) {
            val closestView = findClosestViewToCurrentPosition(ignoredView)
            currentPosition = if (closestView == null) {
                RecyclerView.NO_POSITION
            } else {
                (closestView.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
            }
        }

        private fun findClosestViewToCurrentPosition(ignoredView: View?): View? {
            var closest: View? = null
            var closestDistance = Int.MAX_VALUE
            scrappedViews?.forEach { viewHolder ->
                val view = viewHolder.itemView
                val layoutParams = view.layoutParams as RecyclerView.LayoutParams
                val distance =
                    (layoutParams.viewLayoutPosition - currentPosition) * itemDirection.value
                val skipView = view === ignoredView
                        || layoutParams.isItemRemoved
                        || distance < 0  // item is not in current direction
                if (!skipView && distance < closestDistance) {
                    closest = view
                    closestDistance = distance
                    if (distance == 0) {
                        return@forEach
                    }
                }
            }
            return closest
        }
    }


}
