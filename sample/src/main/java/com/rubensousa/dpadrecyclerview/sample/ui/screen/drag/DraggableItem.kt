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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rubensousa.dpadrecyclerview.compose.dpadClickable

@Composable
fun DraggableItem(
    item: Int,
    isDragging: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onClick: () -> Unit = {},
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
    Box(
        modifier = modifier
            .size(200.dp)
            .then(
                if (isDragging) {
                    Modifier.border(8.dp, Color.Blue, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .focusRequester(focusRequester)
            .onFocusChanged { focusState ->
                isFocused = focusState.hasFocus
            }
            .focusTarget()
            .dpadClickable {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = item.toString(),
            color = textColor,
            fontSize = 35.sp
        )
    }
}

@Preview
@Composable
fun PreviewItemNotFocused() {
    DraggableItem(item = 0, isDragging = false)
}

@Preview
@Composable
fun PreviewItemFocused() {
    val focusRequester = remember { FocusRequester() }

    DraggableItem(item = 0, isDragging = false, focusRequester = focusRequester)

    SideEffect {
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
fun PreviewItemDragging() {
    val focusRequester = remember { FocusRequester() }

    DraggableItem(item = 0, isDragging = true, focusRequester = focusRequester)

    SideEffect {
        focusRequester.requestFocus()
    }
}