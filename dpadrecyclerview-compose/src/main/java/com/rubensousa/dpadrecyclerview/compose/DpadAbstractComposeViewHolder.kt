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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder

/**
 * A ViewHolder that will render a [Composable] in [Content].
 *
 * Check the default implementation at [DpadComposeViewHolder]
 */
abstract class DpadAbstractComposeViewHolder<T>(
    parent: ViewGroup,
    isFocusable: Boolean = true,
    dispatchFocusToComposable: Boolean = true,
    compositionStrategy: ViewCompositionStrategy = RecyclerViewCompositionStrategy.DisposeOnRecycled
) : RecyclerView.ViewHolder(DpadComposeView(parent.context)), DpadViewHolder {

    private val itemState = mutableStateOf<T?>(null)
    private val selectionState = mutableStateOf(false)

    init {
        val composeView = itemView as DpadComposeView
        composeView.apply {
            setFocusConfiguration(
                isFocusable = isFocusable,
                dispatchFocusToComposable = dispatchFocusToComposable
            )
            setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(hasFocus)
            }
            setViewCompositionStrategy(compositionStrategy)
            setContent {
                itemState.value?.let { item ->
                    Content(item, composeView.hasFocus(), selectionState.value)
                }
            }
        }
    }

    @Composable
    abstract fun Content(item: T, isFocused: Boolean, isSelected: Boolean)

    override fun onViewHolderSelected() {
        selectionState.value = true
    }

    override fun onViewHolderDeselected() {
        selectionState.value = false
    }

    open fun onFocusChanged(hasFocus: Boolean) {

    }

    fun setItemState(item: T?) {
        itemState.value = item
    }

    fun getItem(): T? = itemState.value

}
