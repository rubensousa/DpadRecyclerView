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

package com.rubensousa.dpadrecyclerview.layoutmanager.alignment


import android.view.View
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.DpadLayoutParams

internal class ChildScrollAlignment {

    private var alignment = ChildAlignment(offset = 0)

    fun setAlignment(alignmentConfig: ChildAlignment) {
        alignment = alignmentConfig
    }

    fun getAlignment() = alignment

    fun updateAlignments(
        view: View,
        layoutParams: DpadLayoutParams,
        isVertical: Boolean,
        reverseLayout: Boolean
    ) {
        val anchor = ViewAnchorHelper.calculateAnchor(
            itemView = view,
            alignmentView = view,
            alignment, isVertical, reverseLayout
        )
        layoutParams.setAlignmentAnchor(anchor)
    }

}
