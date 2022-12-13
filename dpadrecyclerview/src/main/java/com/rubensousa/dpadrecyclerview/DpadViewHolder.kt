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
import java.util.Collections

/**
 * A ViewHolder managed by [DpadRecyclerView].
 *
 * Implement this in case you're interested in receiving selection changes or customising alignment
 *
 * For receiving focus changes, use the standard [View.setOnFocusChangeListener] instead
 */
interface DpadViewHolder {

    /**
     * Will be called whenever this ViewHolder is the current one selected.
     *
     * This is NOT the same as gaining focus.
     *
     * To observe focus changes,
     * you need to use the focus listener set via [View.setOnFocusChangeListener]
     *
     * This is called automatically by [DpadRecyclerView] on selection changes.
     */
    fun onViewHolderSelected() {}

    /**
     * Will be called whenever this ViewHolder is no longer the current one selected.
     *
     * This is NOT the same as losing focus.
     *
     * To observe focus changes,
     * you need to use the focus listener set via [View.setOnFocusChangeListener]
     *
     * This is called automatically by [DpadRecyclerView] on selection changes.
     */
    fun onViewHolderDeselected() {}

    /**
     * @return the alignment configurations to use for this ViewHolder,
     * or empty if it should be aligned using the configuration of the [DpadRecyclerView]
     */
    fun getAlignments(): List<ViewHolderAlignment> {
        return Collections.emptyList()
    }

}
