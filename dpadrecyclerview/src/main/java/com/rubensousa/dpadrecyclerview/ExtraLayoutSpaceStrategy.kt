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

package com.rubensousa.dpadrecyclerview

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Overrides the default mechanism for laying out extra views at the borders of the RecyclerView.
 * Check [LinearLayoutManager.calculateExtraLayoutSpace] for more details.
 */
interface ExtraLayoutSpaceStrategy {
    /**
     * Calculates the extra space that should be laid out (in pixels).
     * `extraLayoutSpace[0]` should contain the extra space for top/left
     * and `extraLayoutSpace[1]` the extra space for bottom/right depending on the orientation.
     *
     * By default, [DpadRecyclerView] will layout half of an extra page in the scrolling direction
     */
    fun calculateExtraLayoutSpace(state: RecyclerView.State, extraLayoutSpace: IntArray)
}