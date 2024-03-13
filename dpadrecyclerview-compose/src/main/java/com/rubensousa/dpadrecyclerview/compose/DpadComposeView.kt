package com.rubensousa.dpadrecyclerview.compose

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy

/**
 * A wrapper for [ComposeView] to allow keeping focus inside the view system
 */
class DpadComposeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val composeView = ComposeView(context)
    private val focusState = mutableStateOf(false)
    private val internalFocusListener = OnFocusChangeListener { v, hasFocus ->
        focusState.value = hasFocus
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

}
