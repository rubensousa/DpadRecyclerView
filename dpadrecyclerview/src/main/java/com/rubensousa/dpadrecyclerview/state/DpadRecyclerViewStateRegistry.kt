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

class DpadRecyclerViewStateRegistry {

    // TODO: Restore on configuration changes
    private val layoutManagerStates = mutableMapOf<String, Parcelable?>()
    private val scrollState = DpadRecyclerViewScrollState(layoutManagerStates)

    fun getScrollState(): DpadRecyclerViewScrollState {
        return scrollState
    }

    fun clearScrollStates() {
        layoutManagerStates.clear()
    }

    fun clearScrollState(key: String) {
        layoutManagerStates.remove(key)
    }

}
