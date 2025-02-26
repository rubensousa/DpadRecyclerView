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

package com.rubensousa.dpadrecyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * Listener for receiving layout events of children of this [RecyclerView]
 */
interface OnChildLaidOutListener {
    /**
     * Called after a ViewHolder's view has been added to the view hierarchy
     * and has been laid out
     * @param parent the [RecyclerView] that contains this child
     * @param child the [RecyclerView.ViewHolder] that was laid out
     */
    fun onChildLaidOut(parent: DpadRecyclerView, child: RecyclerView.ViewHolder)
}