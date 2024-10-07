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
import androidx.recyclerview.widget.ListAdapter

open class DelegateAdapter :
    ListAdapter<RecyclerViewItem, DelegateViewHolder<RecyclerViewItem>>(
        RecyclerViewItem.DIFF_CALLBACK
    ) {

    private val delegates = mutableMapOf<Int, ViewHolderDelegate<*, *>>()

    fun addDelegate(delegate: ViewHolderDelegate<*, *>) {
        delegates[delegates.size] = delegate
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DelegateViewHolder<RecyclerViewItem> {
        return findDelegateForItemViewType(viewType).onCreateViewHolder(parent)
    }

    override fun onBindViewHolder(holder: DelegateViewHolder<RecyclerViewItem>, position: Int) {
        findDelegateForItemViewType(holder.itemViewType)
            .onBindViewHolder(holder, getItem(position))
    }

    override fun onViewRecycled(holder: DelegateViewHolder<RecyclerViewItem>) {
        super.onViewRecycled(holder)
        findDelegateForItemViewType(holder.itemViewType).onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: DelegateViewHolder<RecyclerViewItem>) {
        super.onViewAttachedToWindow(holder)
        findDelegateForItemViewType(holder.itemViewType).onViewAttached(holder)
    }

    override fun onViewDetachedFromWindow(holder: DelegateViewHolder<RecyclerViewItem>) {
        findDelegateForItemViewType(holder.itemViewType).onViewDetached(holder)
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        delegates.keys.forEach { key ->
            val delegate = delegates[key]
            if (delegate != null && delegate.matches(item)) {
                return key
            }
        }
        throw IllegalStateException("No delegate found for item $item")
    }

    @Suppress("UNCHECKED_CAST")
    private fun findDelegateForItemViewType(viewType: Int): ViewHolderDelegate<RecyclerViewItem, DelegateViewHolder<RecyclerViewItem>> {
        return delegates[viewType] as ViewHolderDelegate<RecyclerViewItem, DelegateViewHolder<RecyclerViewItem>>
    }


}
