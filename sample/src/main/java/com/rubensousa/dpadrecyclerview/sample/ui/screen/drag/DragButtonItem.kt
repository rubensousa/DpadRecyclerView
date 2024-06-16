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

package com.rubensousa.dpadrecyclerview.sample.ui.screen.drag

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rubensousa.dpadrecyclerview.compose.dpadClickable
import com.rubensousa.dpadrecyclerview.sample.R

@Composable
fun DragButtonItem(
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onStartDragClick: () -> Unit,
    onStopDragClick: () -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor = if (isFocused) {
        Color.White
    } else {
        Color.Black
    }
    val textColor = if (isFocused) {
        Color.Black
    } else {
        Color.White
    }
    Row(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.hasFocus
            }
            .focusable()
            .dpadClickable {
                if (isDragging) {
                    onStopDragClick()
                } else {
                    onStartDragClick()
                }
            }
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(64.dp),
            painter = if (!isDragging) {
                painterResource(id = R.drawable.ic_drag_handle)
            } else {
                painterResource(id = R.drawable.ic_done)
            },
            contentDescription = null,
            tint = textColor
        )
        Text(
            style = MaterialTheme.typography.titleLarge,
            text = if (isDragging) {
                "Stop dragging"
            } else {
                "Start dragging"
            },
            color = textColor
        )
    }
}

@Preview
@Composable
fun PreviewDragButtonNotFocused() {
    DragButtonItem(
        isDragging = false,
        onStartDragClick = {},
        onStopDragClick = {}
    )
}

@Preview
@Composable
fun PreviewDragButtonFocused() {
    val focusRequester = remember { FocusRequester() }

    DragButtonItem(
        isDragging = true,
        focusRequester = focusRequester,
        onStartDragClick = {},
        onStopDragClick = {}
    )

    SideEffect {
        focusRequester.requestFocus()
    }
}