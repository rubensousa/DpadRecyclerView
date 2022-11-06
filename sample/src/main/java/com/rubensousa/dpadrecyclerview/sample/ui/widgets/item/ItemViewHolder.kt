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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.item

import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder

class ItemViewHolder(
    root: View,
    private val textView: TextView
) : RecyclerView.ViewHolder(root), DpadViewHolder {

    private var clickListener: ItemClickListener? = null
    private val interpolator = AccelerateDecelerateInterpolator()

    init {
        itemView.setOnClickListener {
            clickListener?.onViewHolderClicked()
        }
        root.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                grow()
            } else {
                shrink()
            }
        }
    }

    fun bind(item: Int, listener: ItemClickListener?) {
        textView.text = item.toString()
        clickListener = listener
    }

    fun recycle() {
        clickListener = null
    }

    fun grow() {
        itemView.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setInterpolator(interpolator)
            .duration = 500
    }

    fun shrink() {
        itemView.animate().cancel()
        itemView.scaleX = 1.0f
        itemView.scaleY = 1.0f
    }

    interface ItemClickListener {
        fun onViewHolderClicked()
    }

}
