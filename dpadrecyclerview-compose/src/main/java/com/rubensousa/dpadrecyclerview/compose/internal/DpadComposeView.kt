package com.rubensousa.dpadrecyclerview.compose.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

/**
 * A wrapper for [androidx.compose.ui.platform.ComposeView] to allow keeping focus inside the view system
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
        composeView.isFocusable = false
        composeView.isFocusableInTouchMode = false
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
            composeView.descendantFocusability = FOCUS_AFTER_DESCENDANTS
            this.isFocusable = false
            this.isFocusableInTouchMode = false
        } else {
            this.isFocusable = isFocusable
            this.isFocusableInTouchMode = isFocusable
            descendantFocusability = FOCUS_BLOCK_DESCENDANTS
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