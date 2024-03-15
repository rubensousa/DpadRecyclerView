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

package com.rubensousa.dpadrecyclerview.state

import android.os.Parcelable
import android.util.SparseArray
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class DpadStateRegistry {

    // TODO: Restore on configuration changes
    private val scrollStates = mutableMapOf<String, Parcelable?>()
    private val viewStates = mutableMapOf<String, SparseArray<Parcelable>>()
    private val scrollState = DpadScrollState(scrollStates)
    private val viewHolderState = DpadViewHolderState(viewStates)

    /**
     * @return [DpadScrollState] to save and restore scroll states of [DpadRecyclerView]
     */
    fun getScrollState(): DpadScrollState {
        return scrollState
    }

    /**
     * @return [DpadViewHolderState] to save and restore view states
     * of ViewHolders part of a  [DpadRecyclerView]
     */
    fun getViewHolderState(): DpadViewHolderState {
        return viewHolderState
    }

    /**
     * Clears all ViewHolder states to prevent them from being restored later
     */
    fun clearViewHolderState() {
        viewStates.clear()
    }

    /**
     * Clears all scroll states to prevent them from being restored later
     */
    fun clearScrollState() {
        scrollStates.clear()
    }

}
