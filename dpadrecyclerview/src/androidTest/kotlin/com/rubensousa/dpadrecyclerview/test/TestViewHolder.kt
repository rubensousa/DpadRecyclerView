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

package com.rubensousa.dpadrecyclerview.test

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder

abstract class TestViewHolder(view: View) : RecyclerView.ViewHolder(view), DpadViewHolder {

    var isSelected = false
        private set

    var selectionCount = 0
        private set

    var deselectionCount = 0
        private set

    var alignmentCount = 0
        private set

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        isSelected = true
        selectionCount++
    }

    override fun onViewHolderSelectedAndAligned() {
        super.onViewHolderSelectedAndAligned()
        alignmentCount++
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        isSelected = false
        deselectionCount++
    }

}
