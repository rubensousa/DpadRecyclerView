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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview

object TestComposable {
    val focusedKey = SemanticsPropertyKey<Boolean>("Focused")
    val selectedKey = SemanticsPropertyKey<Boolean>("Selected")
}

@Composable
fun TestComposable(
    modifier: Modifier = Modifier,
    item: Int,
    isFocused: Boolean,
    isSelected: Boolean
) {
    val backgroundColor = if (isFocused) {
        Color.White
    } else if (isSelected) {
        Color.Blue
    } else {
        Color.Black
    }
    Box(
        modifier = modifier
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(modifier = Modifier.semantics {
            set(TestComposable.focusedKey, isFocused)
            set(TestComposable.selectedKey, isSelected)
        }, text = item.toString())
    }
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun TestComposablePreviewNormal() {
    TestComposable(item = 0, isFocused = false, isSelected = false)
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun TestComposablePreviewFocused() {
    TestComposable(item = 0, isFocused = true, isSelected = false)
}

@Preview(widthDp = 300, heightDp = 300)
@Composable
fun TestComposablePreviewSelected() {
    TestComposable(item = 0, isFocused = false, isSelected = true)
}