/*
 * Copyright 2023 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.spacing

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

/**
 * A base [RecyclerView.ItemDecoration] that checks if spacing should be applied
 * for a given position using [DpadSpacingLookup].
 *
 * Subclasses should only be used for instances of [DpadRecyclerView] and not other RecyclerViews.
 */
abstract class DpadSpacingDecoration : RecyclerView.ItemDecoration() {

    private var spacingLookup: DpadSpacingLookup? = null

    /**
     * @see [getItemOffsets]
     */
    abstract fun getItemOffsets(
        outRect: Rect,
        view: View,
        layoutPosition: Int,
        parent: DpadRecyclerView,
        state: RecyclerView.State
    )

    final override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        /**
         *  We need to use the layout position since it's the only valid
         *  source of truth at this stage.
         *  The item could've been removed and adapter position
         *  in that case is set to RecyclerView.NO_POSITION
         */
        val position = layoutParams.viewLayoutPosition
        val viewHolder = parent.getChildViewHolder(view)
        if (shouldApplySpacingForViewHolder(viewHolder, state.itemCount)) {
            getItemOffsets(outRect, view, position, parent as DpadRecyclerView, state)
        } else {
            outRect.setEmpty()
        }
    }

    /**
     * @param spacingLookup an optional [DpadSpacingLookup] to filter layout positions
     * that shouldn't have spacing applied
     */
    fun setSpacingLookup(spacingLookup: DpadSpacingLookup?) {
        this.spacingLookup = spacingLookup
    }

    /**
     * @return true if decoration will be applied for [viewHolder]
     * or false if[spacingLookup] doesn't allow decoration for this [viewHolder]
     */
    private fun shouldApplySpacingForViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        itemCount: Int
    ): Boolean {
        return spacingLookup?.shouldApplySpacing(viewHolder, itemCount) ?: true
    }



}
