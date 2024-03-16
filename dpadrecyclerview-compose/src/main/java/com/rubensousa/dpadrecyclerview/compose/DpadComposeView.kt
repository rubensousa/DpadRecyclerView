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

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

/**
 * A wrapper for [ComposeView] to allow keeping focus inside the view system
 */
internal class DpadComposeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val composeView = ComposeView(context)
    private val internalFocusListener = OnFocusChangeListener { v, hasFocus ->
        focusListener?.onFocusChange(v, hasFocus)
    }
    private var focusListener: OnFocusChangeListener? = null

    init {
        addView(composeView)
        clipChildren = false
        super.setOnFocusChangeListener(internalFocusListener)
    }

    override fun setOnFocusChangeListener(listener: OnFocusChangeListener?) {
        focusListener = listener
    }

    fun setFocusConfiguration(
        isFocusable: Boolean,
        dispatchFocusToComposable: Boolean
    ) {
        if (dispatchFocusToComposable) {
            composeView.isFocusable = isFocusable
            composeView.isFocusableInTouchMode = isFocusable
            composeView.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            this.isFocusable = false
            this.isFocusableInTouchMode = false
        } else {
            this.isFocusable = isFocusable
            this.isFocusableInTouchMode = isFocusable
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
    }

    fun setViewCompositionStrategy(strategy: ViewCompositionStrategy) {
        composeView.setViewCompositionStrategy(strategy)
    }

    fun setContent(content: @Composable () -> Unit) {
        composeView.setContent(content)
    }

    internal fun hasComposition() = composeView.hasComposition
}
