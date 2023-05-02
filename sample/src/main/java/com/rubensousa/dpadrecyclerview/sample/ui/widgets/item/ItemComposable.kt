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


import androidx.compose.animation.core.FastOutLinearInEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ItemComposable {
    const val TEST_TAG_TEXT_FOCUSED = "focused_text"
    const val TEST_TAG_TEXT_NOT_FOCUSED = "unfocused_text"
}

@Composable
fun ItemComposable(
    modifier: Modifier = Modifier, item: Int, isFocused: Boolean
) {
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
    val scale = animateFloatAsState(
        targetValue = if (isFocused) 1.1f else 1.0f,
        animationSpec = tween(
            durationMillis = if (isFocused) 350 else 200,
            easing = if (isFocused) FastOutSlowInEasing else FastOutLinearInEasing,
        )
    )

    Box(
        modifier = modifier
            .scale(scale.value)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            modifier = Modifier
                .scale(scale.value)
                .testTag(
                    if (isFocused) {
                        ItemComposable.TEST_TAG_TEXT_FOCUSED
                    } else {
                        ItemComposable.TEST_TAG_TEXT_NOT_FOCUSED
                    }
                ),
            text = item.toString(),
            color = textColor,
            fontSize = 35.sp
        )
    }
}

@Composable
fun GridItemComposable(item: Int, isFocused: Boolean) {
    ItemComposable(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        item = item,
        isFocused
    )
}

@Preview
@Composable
fun PreviewGridItemComposableFocused() {
    GridItemComposable(item = 0, isFocused = true)
}

@Preview
@Composable
fun PreviewGridItemComposableNotFocused() {
    GridItemComposable(item = 0, isFocused = false)
}