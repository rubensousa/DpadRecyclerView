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

import android.view.View
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class ItemAnimator(private val itemView: View) {

    companion object {
        private const val selectDuration = 350L
        private const val deselectDuration = 200L
        private val selectInterpolator = FastOutSlowInInterpolator()
        private val deselectInterpolator = FastOutLinearInInterpolator()
    }

    fun startFocusAnimation() {
        itemView.animate().cancel()
        itemView.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setInterpolator(selectInterpolator)
            .setDuration(selectDuration)
    }

    fun startUnfocusAnimation() {
        itemView.animate().cancel()
        itemView.scaleX = 1.05f
        itemView.scaleY = 1.05f
        itemView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(deselectInterpolator)
            .setDuration(deselectDuration)
    }

    fun cancel() {
        itemView.animate().cancel()
        itemView.scaleX = 1f
        itemView.scaleY = 1f
    }

}