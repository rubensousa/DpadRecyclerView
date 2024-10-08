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

package com.rubensousa.dpadrecyclerview.sample.ui.model

import android.view.ViewGroup

interface ViewHolderDelegate<T : RecyclerViewItem, VH : DelegateViewHolder<T>> {

    fun onCreateViewHolder(parent: ViewGroup): VH

    fun onBindViewHolder(holder: VH, item: T) {
        holder.bind(item)
    }

    fun onViewRecycled(holder: VH) {}

    fun onViewAttached(holder: VH) {}

    fun onViewDetached(holder: VH) {}

    fun matches(item: RecyclerViewItem): Boolean

}
