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

package com.rubensousa.dpadrecyclerview.sample.ui.widgets.common

import android.widget.TextView
import androidx.compose.animation.core.EaseIn
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class ListAnimator(
    private val recyclerView: DpadRecyclerView,
    private val title: TextView
) {

    companion object {
        private const val inactiveAlpha = 0.4f
        private const val selectDuration = 500L
        private const val deselectDuration = 200L
        private val selectInterpolator = FastOutSlowInInterpolator()
        private val deselectInterpolator = FastOutLinearInInterpolator()
    }

    init {
        recyclerView.alpha = inactiveAlpha
        title.alpha = inactiveAlpha
    }

    fun startSelectionAnimation() {
        title.pivotX = 0f
        title.pivotY = title.height.toFloat()
        EaseIn
        title.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(1.0f)
            .setInterpolator(selectInterpolator)
            .setDuration(selectDuration)

        recyclerView.animate()
            .alpha(1.0f)
            .setInterpolator(selectInterpolator)
            .setDuration(selectDuration)
    }

    fun startDeselectionAnimation() {
        title.animate()
            .alpha(0.4f)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(deselectInterpolator)
            .setDuration(deselectDuration)

        recyclerView.animate().alpha(0.4f)
            .setInterpolator(deselectInterpolator)
            .setDuration(deselectDuration)
    }

    fun cancel() {
        recyclerView.animate().cancel()
        title.animate().cancel()
    }

}