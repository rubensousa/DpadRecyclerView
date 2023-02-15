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
import com.rubensousa.dpadrecyclerview.DpadViewHolder

/**
 * A ViewHolder that will render a [Composable].
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
 */
open class DpadComposeViewHolder<T>(
    parent: ViewGroup,
    composable: DpadComposable<T>,
    onClick: ((item: T) -> Unit)? = null,
    onLongClick: ((item: T) -> Boolean)? = null,
    isFocusable: Boolean = true
) : RecyclerView.ViewHolder(ComposeView(parent.context)), DpadViewHolder {

    private val itemState = mutableStateOf<T?>(null)
    private val focusState = mutableStateOf(false)
    private val selectionState = mutableStateOf(false)

    init {
        val composeView = itemView as ComposeView
        composeView.isFocusable = isFocusable
        composeView.isFocusableInTouchMode = isFocusable
        composeView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        composeView.setOnFocusChangeListener { _, hasFocus ->
            focusState.value = hasFocus
        }
        if (onClick != null) {
            composeView.setOnClickListener {
                itemState.value?.let(onClick)
            }
        }
        if (onLongClick != null) {
            composeView.setOnLongClickListener {
                val value = itemState.value ?: return@setOnLongClickListener false
                onLongClick(value)
            }
        }
        composeView.setContent {
            itemState.value?.let { item ->
                composable(item, focusState.value, selectionState.value)
            }
        }
    }

    override fun onViewHolderSelected() {
        selectionState.value = true
    }

    override fun onViewHolderDeselected() {
        selectionState.value = false
    }

    fun setItemState(item: T?) {
        itemState.value = item
    }

}

typealias DpadComposable<T> = @Composable (T, Boolean, Boolean) -> Unit
