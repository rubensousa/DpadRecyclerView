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
import android.widget.TextView
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class ListAnimator(
    private val recyclerView: DpadRecyclerView,
    private val title: TextView
) {

    companion object {
        private const val inactiveAlpha = 0.4f
        private const val selectDuration = 350L
        private const val deselectDuration = 200L
        private val selectInterpolator = FastOutSlowInInterpolator()
        private val deselectInterpolator = FastOutLinearInInterpolator()
        private val handler: Handler by lazy {
            Handler(Looper.getMainLooper())
        }
    }

    private val selectionRunnable = Runnable {
        title.pivotX = 0f
        title.pivotY = title.height.toFloat()
        title.animate()
            .scaleX(1.5f)
            .scaleY(1.5f)
            .alpha(1f)
            .setInterpolator(selectInterpolator)
            .duration = selectDuration

        recyclerView.animate()
            .alpha(1f)
            .setInterpolator(selectInterpolator)
            .duration = selectDuration
    }
    private val deselectionRunnable = Runnable {
        title.animate()
            .alpha(inactiveAlpha)
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(deselectInterpolator)
            .duration = deselectDuration

        recyclerView.animate()
            .alpha(inactiveAlpha)
            .setInterpolator(deselectInterpolator)
            .duration = deselectDuration
    }

    init {
        recyclerView.alpha = inactiveAlpha
        title.alpha = inactiveAlpha
    }

    fun startSelectionAnimation() {
        cancelPendingAnimations()
        handler.post(selectionRunnable)
    }

    fun startDeselectionAnimation() {
        cancelPendingAnimations()
        handler.post(deselectionRunnable)
    }

    fun cancel() {
        cancelPendingAnimations()
        recyclerView.alpha = inactiveAlpha
        title.alpha = inactiveAlpha
        title.scaleX = 1.0f
        title.scaleY = 1.0f
    }

    private fun cancelPendingAnimations() {
        handler.removeCallbacks(selectionRunnable)
        handler.removeCallbacks(deselectionRunnable)
        title.animate().cancel()
        recyclerView.animate().cancel()
    }

}