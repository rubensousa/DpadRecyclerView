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

package com.rubensousa.dpadrecyclerview.internal.layoutmanager.layout

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Holds information for the current pivot layout point.
 * Views will be laid out from [headOffset] or [tailOffset] depending on the layout direction
 */
internal class PivotInfo {
    var position: Int = 0

    var headOffset: Int = 0

    var tailOffset: Int = 0

    fun isViewValidAsPivot(pivot: View, state: RecyclerView.State): Boolean {
        val layoutParams = pivot.layoutParams as RecyclerView.LayoutParams
        return !layoutParams.isItemRemoved
                && layoutParams.viewLayoutPosition >= 0
                && layoutParams.viewLayoutPosition < state.itemCount
    }

    override fun toString(): String {
        return "PivotInfo(position=$position, headOffset=$headOffset, tailOffset=$tailOffset)"
    }


}