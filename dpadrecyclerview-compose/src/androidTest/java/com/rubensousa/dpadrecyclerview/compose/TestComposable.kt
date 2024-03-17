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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview

object TestComposable {
    val focusedKey = SemanticsPropertyKey<Boolean>("Focused")
}

@Composable
fun TestComposable(
    modifier: Modifier = Modifier,
    item: Int,
    isFocused: Boolean,
    onDispose: () -> Unit = {},
) {
    val backgroundColor = if (isFocused) {
        Color.White
    } else {
        Color.Black
    }
    Box(
        modifier = modifier
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.semantics {
                set(TestComposable.focusedKey, isFocused)
            },
            text = item.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = if (isFocused) {
                Color.Black
            } else {
                Color.White
            }
        )
    }
    DisposableEffect(key1 = item) {
        onDispose {
            onDispose()
        }
    }
}

@Composable
fun TestComposableFocus(
    modifier: Modifier = Modifier,
    item: Int,
    onClick: () -> Unit,
    onDispose: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor = if (isFocused) {
        Color.White
    } else {
        Color.Black
    }
    Box(
        modifier = modifier
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .focusTarget()
            .background(backgroundColor)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier.semantics {
                set(TestComposable.focusedKey, isFocused)
            },
            text = item.toString(),
            style = MaterialTheme.typography.headlineLarge,
            color = if (isFocused) {
                Color.Black
            } else {
                Color.White
            }
        )
    }
    DisposableEffect(key1 = item) {
        onDispose {
            onDispose()
        }
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun TestComposablePreviewNormal() {
    TestComposableFocus(
        item = 0,
        onClick = {}
    )
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun TestComposablePreviewFocused() {
    val focusRequester = remember { FocusRequester() }
    TestComposableFocus(
        item = 0,
        modifier = Modifier.focusRequester(focusRequester),
        onClick = {}
    )
    SideEffect {
        focusRequester.requestFocus()
    }
}
