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

package com.rubensousa.dpadrecyclerview.compose

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView

/**
 * This allows inline definition of ViewHolders in `onCreateViewHolder`:
 *
 * ```kotlin
 * override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DpadComposeFocusViewHolder<Int> {
 *     return DpadComposeFocusViewHolder(parent) { item ->
 *          ItemComposable(item)
 *     }
 * }
 * ```
 * To update the current item, override `onBindViewHolder` and call [setItemState]:
 *
 * ```kotlin
 * override fun onBindViewHolder(holder: DpadComposeFocusViewHolder<Int>, position: Int) {
 *      holder.setItemState(getItem(position))
 * }
 * ```
 */
class DpadComposeFocusViewHolder<T>(
    parent: ViewGroup,
    isFocusable: Boolean = true,
    private val content: (@Composable (item: T) -> Unit)? = null,
) : RecyclerView.ViewHolder(ComposeView(parent.context)) {

    private val itemState = mutableStateOf<T?>(null)
    private val composeView = itemView as ComposeView

    init {
        composeView.apply {
            this@DpadComposeFocusViewHolder.setFocusable(isFocusable)
            content?.let {
                setContent(content)
            }
        }
    }

    /**
     * Sets the content of the internal [ComposeView]
     */
    fun setContent(content: @Composable (item: T) -> Unit) {
        composeView.setContent {
            itemState.value?.let { item ->
                content(item)
            }
        }
    }

    /**
     * Marks the internal [ComposeView] has [focusable] or not.
     * If false, this will prevent child nodes from being focusable
     */
    fun setFocusable(focusable: Boolean) {
        composeView.apply {
            if (!isFocusable) {
                isFocusable = false
                isFocusableInTouchMode = false
            }
            descendantFocusability = if (focusable) {
                ViewGroup.FOCUS_AFTER_DESCENDANTS
            } else {
                ViewGroup.FOCUS_BLOCK_DESCENDANTS
            }
        }
    }

    /**
     * Updates the current item so that the composition is updated with the new value
     * in [content]
     */
    fun setItemState(item: T?) {
        itemState.value = item
    }

    /**
     * @return the current item used for [content]
     */
    fun getItem(): T? = itemState.value

}
