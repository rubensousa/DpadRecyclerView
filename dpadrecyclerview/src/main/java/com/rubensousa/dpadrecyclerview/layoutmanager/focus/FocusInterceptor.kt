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

package com.rubensousa.dpadrecyclerview.layoutmanager.focus

import android.view.View
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

internal interface FocusInterceptor {

    /**
     * @param recyclerView the RecyclerView bound to the LayoutManager
     * @param focusedView the currently focused View
     * @param position the current pivot position
     * @param direction One of [View.FOCUS_LEFT], [View.FOCUS_RIGHT], [View.FOCUS_UP],
     * [View.FOCUS_DOWN], [View.FOCUS_FORWARD], [View.FOCUS_BACKWARD]
     */
    fun findFocus(
        recyclerView: DpadRecyclerView,
        focusedView: View,
        position: Int,
        direction: Int
    ): View?

}
