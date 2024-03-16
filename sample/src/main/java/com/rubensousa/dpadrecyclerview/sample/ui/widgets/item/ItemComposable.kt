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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.item


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rubensousa.dpadrecyclerview.compose.dpadClickable

object ItemComposable {
    val focusedKey = SemanticsPropertyKey<Boolean>("Focused")
}

@Composable
fun ItemComposable(
    item: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    var isFocused by remember { mutableStateOf(false) }
    val scaleState = animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        label = "scale",
        animationSpec = tween(
            durationMillis = if (isFocused) 350 else 0,
            easing = FastOutSlowInEasing
        )
    )
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
            .scale(scaleState.value)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
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
            modifier = Modifier
                .semantics {
                    set(ItemComposable.focusedKey, isFocused)
                },
            text = item.toString(),
            color = textColor,
            fontSize = 35.sp
        )
    }
}

@Composable
fun GridItemComposable(
    item: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    ItemComposable(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        item = item,
        onClick = onClick
    )
}

@Preview
@Composable
fun PreviewGridItemComposableFocused() {
    val focusRequester = remember {
        FocusRequester()
    }
    GridItemComposable(
        modifier = Modifier.focusRequester(focusRequester),
        item = 0,
    )
    SideEffect {
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
fun PreviewGridItemComposableNotFocused() {
    GridItemComposable(item = 0)
}