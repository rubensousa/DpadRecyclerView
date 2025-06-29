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
package com.rubensousa.dpadrecyclerview.compose

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.compose.internal.DpadComposeView

/**
 * A basic ViewHolder that forwards [content] to a [ComposeView]
 * and handles focus and clicks inside the View system.
 *
 * Focus is kept inside the internal [ComposeView] to ensure that it behaves correctly
 * and to workaround the following issues:
 *
 * 1. Focus is not sent correctly from Views to Composables:
 * [b/268248352](https://issuetracker.google.com/issues/268248352)
 * This is solved by just holding the focus in [ComposeView]
 *
 * 2. Clicking on a focused Composable does not trigger the standard audio feedback:
 * [b/268268856](https://issuetracker.google.com/issues/268268856)
 * This is solved by just handling the click on [ComposeView] directly
 *
 * This allows inline definition of ViewHolders in `onCreateViewHolder`:
 *
 * ```kotlin
 * override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DpadComposeViewHolder<Int> {
 *     return DpadComposeViewHolder(parent) { item, isFocused ->
 *          ItemComposable(item, isFocused)
 *     }
 * }
 * ```
 * To update the current item, override `onBindViewHolder` and call [setItemState]:
 *
 * ```kotlin
 * override fun onBindViewHolder(holder: DpadComposeViewHolder<Int>, position: Int) {
 *      holder.setItemState(getItem(position))
 * }
 * ```
 */
@Deprecated("Use DpadComposeFocusViewHolder instead")
class DpadComposeViewHolder<T>(
    parent: ViewGroup,
    onClick: ((item: T) -> Unit)? = null,
    onLongClick: ((item: T) -> Boolean)? = null,
    isFocusable: Boolean = true,
    private val content: @Composable (item: T, isFocused: Boolean) -> Unit = { _, _ -> }
) : RecyclerView.ViewHolder(DpadComposeView(parent.context)) {

    private val focusState = mutableStateOf(false)
    private val itemState = mutableStateOf<T?>(null)
    private val composeView = itemView as DpadComposeView

    init {
        composeView.apply {
            setFocusConfiguration(
                isFocusable = isFocusable,
                dispatchFocusToComposable = false
            )
            setOnFocusChangeListener { _, hasFocus ->
                focusState.value = hasFocus
            }
            setContent {
                itemState.value?.let { item ->
                    content(item, focusState.value)
                }
            }
        }
        if (onClick != null) {
            itemView.setOnClickListener {
                getItem()?.let(onClick)
            }
        }
        if (onLongClick != null) {
            itemView.setOnLongClickListener {
                val value = getItem() ?: return@setOnLongClickListener false
                onLongClick(value)
            }
        }
    }

    fun setContent(content: @Composable (item: T) -> Unit) {
        composeView.setContent {
            itemState.value?.let { item ->
                content(item)
            }
        }
    }

    fun setItemState(item: T?) {
        itemState.value = item
    }

    fun getItem(): T? = itemState.value
}
