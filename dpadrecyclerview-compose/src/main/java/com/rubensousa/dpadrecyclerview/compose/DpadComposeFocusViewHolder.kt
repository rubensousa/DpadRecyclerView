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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder

/**
 * Similar to [DpadComposeViewHolder], but sends the focus down to composables
 *
 * This allows inline definition of ViewHolders in `onCreateViewHolder`:
 *
 * ```kotlin
 * override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DpadComposeFocusViewHolder<Int> {
 *     return DpadComposeFocusViewHolder(parent) { item, isSelected ->
 *          ItemComposable(item, isSelected)
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
    compositionStrategy: ViewCompositionStrategy = RecyclerViewCompositionStrategy.DisposeOnRecycled,
    private val content: @Composable (item: T, isSelected: Boolean) -> Unit
) : RecyclerView.ViewHolder(DpadComposeView(parent.context)), DpadViewHolder {

    private val itemState = mutableStateOf<T?>(null)
    private val selectionState = mutableStateOf(false)

    init {
        val composeView = itemView as DpadComposeView
        composeView.apply {
            setFocusConfiguration(
                isFocusable = true,
                dispatchFocusToComposable = true
            )
            setViewCompositionStrategy(compositionStrategy)
            setContent {
                itemState.value?.let { item ->
                    content(item, selectionState.value)
                }
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

    fun getItem(): T? = itemState.value
}
