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

import android.view.View
import androidx.recyclerview.widget.RecyclerView


/**
 * Callback for receiving a notification when a [View] of a [RecyclerView.ViewHolder]
 * has been focused.
 *
 * Be aware that a [RecyclerView.ViewHolder] selection can change without focus and, in that case,
 * this callback is not invoked.
 * Instead, use [OnViewHolderSelectedListener] for observing selections
 */
interface OnViewFocusedListener {

    /**
     * @param parent The [RecyclerView.ViewHolder] within the [RecyclerView] that is focused
     * @param child  The actual child [View] that received focus.
     * Can be a child of a nested RecyclerView.
     */
    fun onViewFocused(
        parent: RecyclerView.ViewHolder,
        child: View,
    )

}
