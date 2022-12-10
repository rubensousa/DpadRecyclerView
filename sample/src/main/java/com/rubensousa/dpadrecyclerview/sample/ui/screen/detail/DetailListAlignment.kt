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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.detail

import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import timber.log.Timber

class DetailListAlignment {

    private val topParentAlignment = ParentAlignment(
        edge = ParentAlignment.Edge.NONE,
        offset = 0,
        offsetRatio = 0.05f
    )
    private val topChildAlignment = ChildAlignment(offset = 0, offsetRatio = 0f)
    private val centerParentAlignment = ParentAlignment(
        edge = ParentAlignment.Edge.NONE,
        offset = 0,
        offsetRatio = 0.5f
    )
    private val centerChildAlignment = ChildAlignment(offset = 0, offsetRatio = 0.5f)
    private var isAlignedToTop = false

    fun alignToCenter(recyclerView: DpadRecyclerView) {
        if (!isAlignedToTop){
            return
        }
        recyclerView.setAlignments(centerParentAlignment, centerChildAlignment, smooth = true)
        Timber.i("Aligning to center")
        isAlignedToTop = false
    }

    fun alignToTop(recyclerView: DpadRecyclerView) {
        if (isAlignedToTop) {
            return
        }
        recyclerView.setAlignments(topParentAlignment, topChildAlignment, smooth = true)
        Timber.i("Aligning to top")
        isAlignedToTop = true
    }

}
