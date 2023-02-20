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

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class ItemAnimator(private val itemView: View) {

    companion object {
        private const val focusGainDuration = 350L
        private const val focusLossDuration = 200L
        private val focusGainInterpolator = FastOutSlowInInterpolator()
        private val focusLossInterpolator = FastOutLinearInInterpolator()
        private val handler: Handler by lazy {
            Handler(Looper.getMainLooper())
        }
    }

    private val focusGainRunnable = Runnable {
        itemView.animate()
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setInterpolator(focusGainInterpolator)
            .duration = focusGainDuration
    }
    private val focusLossRunnable = Runnable {
        itemView.scaleX = 1.05f
        itemView.scaleY = 1.05f
        itemView.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(focusLossInterpolator)
            .duration = focusLossDuration
    }

    fun startFocusGainAnimation() {
        cancelPendingAnimations()
        handler.post(focusGainRunnable)
    }

    fun startFocusLossAnimation() {
        cancelPendingAnimations()
        handler.post(focusLossRunnable)
    }

    fun cancel() {
        cancelPendingAnimations()
        itemView.scaleX = 1f
        itemView.scaleY = 1f
    }

    private fun cancelPendingAnimations() {
        handler.removeCallbacks(focusGainRunnable)
        handler.removeCallbacks(focusLossRunnable)
        itemView.animate().cancel()
    }

}
