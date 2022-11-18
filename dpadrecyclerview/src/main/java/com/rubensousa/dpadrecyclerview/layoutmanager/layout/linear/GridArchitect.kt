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

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo

// TODO
internal class GridArchitect(
    private val layout: RecyclerView.LayoutManager,
    private val layoutInfo: LayoutInfo,
    private val configuration: LayoutConfiguration,
) {

    fun layoutChunk(
        recycler: RecyclerView.Recycler,
        recyclerViewState: RecyclerView.State,
        layoutState: LinearLayoutState,
        layoutResult: LayoutResult
    ) {

    }

}
