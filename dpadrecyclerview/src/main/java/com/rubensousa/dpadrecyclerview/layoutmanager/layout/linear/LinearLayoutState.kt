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

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ItemDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutDirection

/**
 * Adapted from LinearLayoutManager
 * Saves the temporary layout state until `onLayoutCompleted`
 */
internal class LinearLayoutState {

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
    var direction: LayoutDirection = LayoutDirection.END

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
    var fillSpace = 0

    /**
     * Used to pre-layout items that are not yet visible in the layout direction
     */
    var invisibleFillSpace = 0

    /**
     * The extra layout space calculated from the LayoutManager
     */
    var extraLayoutSpaceStart: Int = 0

    /**
     * The extra layout space calculated from the LayoutManager
     */
    var extraLayoutSpaceEnd: Int = 0

    /**
     * How much we can scroll without adding new children at the edges
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

    fun isLayingOutEnd() = direction == LayoutDirection.END

    fun hasMoreItems(state: RecyclerView.State): Boolean {
        return currentPosition >= 0 && currentPosition < state.itemCount
    }

    fun updateDirectionFromLastScroll() {
        direction = if (lastScrollOffset >= 0) {
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
        scrappedViews?.forEach { viewHolder ->
            val view = viewHolder.itemView
            val layoutParams = view.layoutParams as RecyclerView.LayoutParams
            val distance =
                ((layoutParams.viewLayoutPosition - currentPosition) * itemDirection.value)
            val skipView = view === ignore
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

    override fun toString(): String {
        return "LayoutState(offset=$offset, " +
                "currentPosition=$currentPosition, " +
                "fillSpace=$fillSpace, " +
                "invisibleFillSpace=$invisibleFillSpace, " +
                "extraLayoutSpaceStart=$extraLayoutSpaceStart," +
                "extraLayoutSpaceEnd=$extraLayoutSpaceEnd," +
                "availableScrollSpace=$availableScrollSpace," +
                "lastScrollOffset=$lastScrollOffset)"
    }


}
