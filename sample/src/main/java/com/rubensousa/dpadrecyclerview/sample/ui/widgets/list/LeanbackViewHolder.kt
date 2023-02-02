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

import android.view.View
import androidx.leanback.widget.HorizontalGridView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.sample.R

class LeanbackViewHolder(
    view: View,
    val horizontalGridView: HorizontalGridView,
    itemLayoutId: Int
) : AbstractListViewHolder(view, horizontalGridView, itemLayoutId) {

    init {
        horizontalGridView.itemAlignmentOffset = 0
        horizontalGridView.itemAlignmentOffsetPercent = 0.0f
        horizontalGridView.windowAlignment = HorizontalGridView.WINDOW_ALIGN_HIGH_EDGE
        horizontalGridView.windowAlignmentOffset = view.resources.getDimensionPixelOffset(
            R.dimen.list_margin_start
        )
        horizontalGridView.windowAlignmentOffsetPercent = 0.0f
        horizontalGridView.apply {
            addItemDecoration(
                LinearMarginDecoration.createHorizontal(
                    horizontalMargin = itemView.resources.getDimensionPixelOffset(
                        R.dimen.horizontal_item_spacing
                    )
                )
            )
        }
        onViewHolderDeselected()
    }

}
