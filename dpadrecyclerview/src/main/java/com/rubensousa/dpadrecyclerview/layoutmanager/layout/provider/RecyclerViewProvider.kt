/*
 * Copyright 2023 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.provider

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest

/**
 * A [ViewProvider] that gets Views from the [Recycler].
 *
 * This is used for the main layout stages and scrolling.
 */
internal class RecyclerViewProvider : ViewProvider {

    private var recycler: Recycler? = null

    override fun next(layoutRequest: LayoutRequest, state: RecyclerView.State): View? {
        if (!hasNext(layoutRequest.currentPosition, state)) {
            return null
        }
        val view = recycler?.getViewForPosition(layoutRequest.currentPosition)
        if (view != null) {
            layoutRequest.moveToNextPosition()
        }
        return view
    }

    fun updateRecycler(recycler: Recycler) {
        this.recycler = recycler
    }

    fun clearRecycler() {
        recycler = null
    }

}
