/*
 * Copyright 2024 Rúben Sousa
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

import androidx.recyclerview.widget.RecyclerView.ViewHolder

/**
 * Allows [DpadRecyclerView] to align differently for each ViewHolder.
 * When this is used, the [ParentAlignment.Edge] preference has no effect
 * and you're fully responsible to pick an anchor for all ViewHolders
 */
interface AlignmentLookup {

    /**
     * @return the [ParentAlignment] configuration to be used for [viewHolder]
     * or null to fallback to the default one set via [DpadRecyclerView.setParentAlignment]
     */
    fun getParentAlignment(viewHolder: ViewHolder): ParentAlignment? = null

    /**
     * @return the [ChildAlignment] configuration to be used for [viewHolder]
     * or null to fallback to the default one set via [DpadRecyclerView.setChildAlignment]
     */
    fun getChildAlignment(viewHolder: ViewHolder): ChildAlignment? = null

}
