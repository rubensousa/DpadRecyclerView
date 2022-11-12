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

package com.rubensousa.dpadrecyclerview.layout.layout

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

/**
 * Adapted from LinearLayoutManager
 * Saves the temporary layout state until `onLayoutCompleted`
 */
internal class LayoutState {

    companion object {
        const val SCROLL_SPACE_NONE = Int.MIN_VALUE
    }

    /**
     * Enables or disables recycling in some scenarios
     */
    var recycle = true

    /**
     * Pixel offset where layout should start
     */
    var offset = 0

    /**
     * The current direction of the layout stage
     */
    var direction: LayoutDirection = LayoutDirection.START

    /**
     * Defines the direction in which the data adapter is traversed
     */
    var itemDirection = ItemDirection.TAIL

    /**
     * Current position on the adapter to get the next item.
     */
    var currentPosition = 0

    /**
     * Number of pixels that we should fill, in the layout direction.
     */
    var available = 0

    /**
     * Used to pre-layout items that are not yet visible
     */
    var extraFillSpace = 0

    /**
     * The extra layout space calculated from the LayoutManager
     */
    var extraLayoutSpace: Int = 0

    /**
     * How much we can scroll without adding new children
     */
    var availableScrollSpace: Int = 0

    /**
     * Last scroll output from `scrollBy`
     */
    var lastScrollOffset: Int = 0

    /**
     * Used when there is no limit in how many views can be laid out.
     */
    var isInfinite = false

    /**
     * When consuming [scrappedViews], if this value is set to true,
     * we skip removed views since they should not be laid out in post layout step.
     */
    var isPreLayout = false

    /**
     * Views pending recycling/removal
     */
    var scrappedViews: List<RecyclerView.ViewHolder>? = null

    var spanCount = 1

    var reverseLayout = false

    fun isLayingOutStart() = direction == LayoutDirection.START

    fun isLayingOutEnd() = direction == LayoutDirection.START

    fun hasMoreItems(state: RecyclerView.State): Boolean {
        return currentPosition >= 0 && currentPosition < state.itemCount
    }

    fun updateDirectionFromLastScroll() {
       direction =  if (lastScrollOffset >= 0) {
           LayoutDirection.END
        } else {
           LayoutDirection.START
        }
    }

    /**
     * Gets the view for the next element that we should layout.
     * Also updates current item index to the next item, based on the [itemDirection]
     *
     * @return The next element that we should layout.
     */
    fun next(recycler: Recycler): View? {
        if (scrappedViews != null) {
            return nextViewFromScrapList()
        }
        val view = recycler.getViewForPosition(currentPosition)
        currentPosition += itemDirection.value
        return view
    }

    /**
     * Returns the next item from the scrap list.
     *
     * Upon finding a valid VH, sets current item position to VH.itemPosition + mItemDirection
     * @return View if an item in the current position or direction exists if not null.
     */
    private fun nextViewFromScrapList(): View? {
        scrappedViews?.forEachIndexed { _, viewHolder ->
            val view = viewHolder.itemView
            val layoutParams = view.layoutParams as RecyclerView.LayoutParams
            if (!layoutParams.isItemRemoved && currentPosition == layoutParams.viewLayoutPosition) {
                assignPositionFromScrapList(view)
                return view
            }
        }
        return null
    }

    fun assignPositionFromScrapList(ignore: View? = null) {
        val closest = nextViewInLimitedList(ignore)
        currentPosition = if (closest == null) {
            RecyclerView.NO_POSITION
        } else {
            (closest.layoutParams as RecyclerView.LayoutParams).viewLayoutPosition
        }
    }

    fun nextViewInLimitedList(ignore: View?): View? {
        var closest: View? = null
        var closestDistance = Int.MAX_VALUE
        scrappedViews?.forEachIndexed { _, viewHolder ->
            val view = viewHolder.itemView
            val layoutParams = view.layoutParams as RecyclerView.LayoutParams
            val distance = ((layoutParams.viewLayoutPosition - currentPosition) * itemDirection.value)
            val skipView = view === ignore
                    || layoutParams.isItemRemoved
                    || distance < 0  // item is not in current direction
            if (!skipView && distance < closestDistance) {
                closest = view
                closestDistance = distance
                if (distance == 0) {
                   return@forEachIndexed
                }
            }
        }
        return closest
    }

    override fun toString(): String {
        return "LayoutState(offset=$offset, " +
                "nextItemPosition=$currentPosition, " +
                "available=$available, " +
                "extraFillSpace=$extraFillSpace, " +
                "extraLayoutSpace=$extraLayoutSpace," +
                "availableScrollSpace=$availableScrollSpace," +
                "lastScrollOffset=$lastScrollOffset)"
    }


    /**
     * Defines the direction in which the data adapter is traversed
     */
    enum class ItemDirection(val value: Int) {
        HEAD(-1),
        TAIL(1)
    }

    /**
     * Direction in which the layout is being filled.
     * These are absolute directions, so it doesn't consider RTL at all
     */
    enum class LayoutDirection(val value: Int) {
        /**
         * Either left in horizontal or top in vertical
         */
        START(-1),

        /**
         * Either right in horizontal or bottom in vertical
         */
        END(1)
    }

}
