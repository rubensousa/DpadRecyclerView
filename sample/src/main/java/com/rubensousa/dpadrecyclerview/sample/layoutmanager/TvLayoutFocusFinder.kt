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

package com.rubensousa.dpadrecyclerview.sample.layoutmanager

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class TvLayoutFocusFinder(
    private val configuration: TvLayoutConfiguration,
    private val layoutInfo: TvLayoutInfo
) {

    private var dpadRecyclerView: DpadRecyclerView? = null

    fun setRecyclerView(recyclerView: DpadRecyclerView?) {
        dpadRecyclerView = recyclerView
    }

    // TODO
    fun onRequestChildFocus(
        parent: RecyclerView,
        state: RecyclerView.State,
        child: View,
        focused: View?
    ): Boolean {
        return true
    }

    // TODO
    fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        if (dpadRecyclerView == null) {
            return focused
        }
        return null
    }

    // TODO
    fun onAddFocusables(
        recyclerView: RecyclerView,
        views: ArrayList<View>,
        direction: Int,
        focusableMode: Int
    ): Boolean {
        return true
    }

}
