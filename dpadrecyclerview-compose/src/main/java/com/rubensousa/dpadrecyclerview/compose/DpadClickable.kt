/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import android.media.AudioManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import kotlinx.coroutines.launch

/**
 * Similar to [Modifier.clickable], but handles only [AcceptableKeys]
 * and triggers a sound effect on click.
 * Workaround for: https://issuetracker.google.com/issues/268268856
 */
@Suppress("UnnecessaryVariable")
@Composable
fun Modifier.dpadClickable(
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onLongClick: (() -> Unit)? = null,
    onClick: (() -> Unit)?,
): Modifier {
    val clickLambda = onClick
    val longClickLambda = onLongClick
    val context = LocalContext.current
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager }
    return handleDpadCenter(
        enabled = enabled,
        interactionSource = interactionSource,
        onClick = if (onClick != null) {
            {
                audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
                onClick()
            }
        } else {
            null
        },
        onLongClick = if (onLongClick != null) {
            {
                onLongClick()
            }
        } else {
            null
        }
    ).focusable(interactionSource = interactionSource)
        .semantics(mergeDescendants = true) {
            onClick(label = null) {
                clickLambda?.let { action ->
                    audioManager?.playSoundEffect(AudioManager.FX_KEY_CLICK)
                    action()
                    return@onClick true
                }
                false
            }
            onLongClick {
                longClickLambda?.let { action ->
                    action()
                    return@onLongClick true
                }
                false
            }
            if (!enabled) {
                disabled()
            }
        }
}

/**
 * This modifier is used to perform some actions when the user clicks the DPAD center button
 *
 * @param enabled if this is false, the DPAD center event is ignored
 * @param interactionSource used to emit [PressInteraction] events
 * @param onClick this lambda will be triggered on DPAD center event
 * @param onLongClick this lambda will be triggered when DPAD center is long pressed.
 */
private fun Modifier.handleDpadCenter(
    enabled: Boolean,
    interactionSource: MutableInteractionSource,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) = composed(
    inspectorInfo =
    debugInspectorInfo {
        name = "handleDpadCenter"
        properties["enabled"] = enabled
        properties["interactionSource"] = interactionSource
        properties["onClick"] = onClick
        properties["onLongClick"] = onLongClick
    }
) {
    if (!enabled) return@composed this

    val coroutineScope = rememberCoroutineScope()
    val pressInteraction = remember { PressInteraction.Press(Offset.Zero) }
    var isLongClick by remember { mutableStateOf(false) }
    val isPressed by interactionSource.collectIsPressedAsState()

    this.onFocusChanged {
        if (!it.isFocused && isPressed) {
            coroutineScope.launch {
                interactionSource.emit(PressInteraction.Release(pressInteraction))
            }
        }
    }.onKeyEvent { keyEvent ->
        if (AcceptableKeys.contains(keyEvent.nativeKeyEvent.keyCode)) {
            when (keyEvent.nativeKeyEvent.action) {
                NativeKeyEvent.ACTION_DOWN -> {
                    when (keyEvent.nativeKeyEvent.repeatCount) {
                        0 ->
                            coroutineScope.launch {
                                interactionSource.emit(pressInteraction)
                            }

                        1 ->
                            onLongClick?.let {
                                isLongClick = true
                                coroutineScope.launch {
                                    interactionSource.emit(
                                        PressInteraction.Release(pressInteraction)
                                    )
                                }
                                it.invoke()
                            }
                    }
                }

                NativeKeyEvent.ACTION_UP -> {
                    if (!isLongClick) {
                        coroutineScope.launch {
                            interactionSource.emit(
                                PressInteraction.Release(pressInteraction)
                            )
                        }
                        onClick?.invoke()
                    } else {
                        isLongClick = false
                    }
                }
            }
            return@onKeyEvent true
        }
        false
    }
}

private val AcceptableKeys = intArrayOf(
    NativeKeyEvent.KEYCODE_DPAD_CENTER,
    NativeKeyEvent.KEYCODE_ENTER,
    NativeKeyEvent.KEYCODE_NUMPAD_ENTER
)