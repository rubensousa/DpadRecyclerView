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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.list

import android.view.Gravity
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.ui.screen.list.LimitedScrollBehavior
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class DpadListViewHolder(
    view: View,
    val dpadRecyclerView: DpadRecyclerView,
    itemLayoutId: Int,
    slowScroll: Boolean,
    reverseLayout: Boolean
) : AbstractListViewHolder(view, dpadRecyclerView, itemLayoutId) {

    private val selectionView = view.findViewById<View>(R.id.selectionOverlayView)

    init {
        dpadRecyclerView.addItemDecoration(
            DpadLinearSpacingDecoration.createHorizontal(
                horizontalItemSpacing = itemView.resources.getDimensionPixelOffset(
                    R.dimen.horizontal_item_spacing
                )
            )
        )
        onViewHolderDeselected()
        if (reverseLayout) {
            setupReversedLayout()
        }
        if (slowScroll) {
            setupSlowScrollingBehavior()
        }
    }

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        selectionView.isActivated = true
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        selectionView.isActivated = false
    }

    private fun setupReversedLayout() {
        val layoutParams = selectionView.layoutParams as FrameLayout.LayoutParams
        layoutParams.marginEnd = layoutParams.marginStart
        layoutParams.gravity = Gravity.CENTER_VERTICAL.or(Gravity.END)
        selectionView.layoutParams = layoutParams
        dpadRecyclerView.setReverseLayout(true)
    }

    private fun setupSlowScrollingBehavior() {
        LimitedScrollBehavior().setup(
            recyclerView = dpadRecyclerView,
            extraLayoutSpaceStart = { dpadRecyclerView.width / 2 },
            extraLayoutSpaceEnd = { 0 }
        )
    }

}
