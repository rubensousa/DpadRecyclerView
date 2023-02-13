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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.item

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder

/**
 * Currently open issues on official issue tracker:
 * 1. Focus is not sent correctly from Views to Composables: https://issuetracker.google.com/issues/268248352
 * 2. Clicking on a focused Composable does not trigger the standard audio feedback: https://issuetracker.google.com/issues/268268856
 *
 * - Issue 1 is solved by keeping the focus on [composeView] instead of passing it down to composables
 *
 * - Issue 2 is solved by handling the click on [composeView] instead of doing it inside the composables
 */
open class DpadComposeViewHolder<T>(
    val composeView: ComposeView,
    composable: @Composable (item: T, isFocused: Boolean, isSelected: Boolean) -> Unit,
    onClick: (item: T) -> Unit
) : RecyclerView.ViewHolder(composeView), DpadViewHolder {

    private val itemState = mutableStateOf<T?>(null)
    private val focusState = mutableStateOf(false)
    private val selectionState = mutableStateOf(false)

    init {
        composeView.isFocusable = true
        composeView.isFocusableInTouchMode = true
        composeView.descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        composeView.setOnFocusChangeListener { _, hasFocus ->
            focusState.value = hasFocus
        }
        composeView.setOnClickListener {
            itemState.value?.let(onClick)
        }
        composeView.setContent {
            itemState.value?.let { item ->
                composable(item, isFocused = focusState.value, isSelected = selectionState.value)
            }
        }
    }

    override fun onViewHolderSelected() {
        selectionState.value = true
    }

    override fun onViewHolderDeselected() {
        selectionState.value = false
    }

    fun bind(item: T?) {
        itemState.value = item
    }

}
