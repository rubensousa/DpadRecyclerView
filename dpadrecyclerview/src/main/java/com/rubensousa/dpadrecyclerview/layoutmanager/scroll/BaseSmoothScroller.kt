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

package com.rubensousa.dpadrecyclerview.layoutmanager.scroll

import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import kotlin.math.max

internal open class BaseSmoothScroller(
    recyclerView: RecyclerView,
    protected val layoutInfo: LayoutInfo
) : LinearSmoothScroller(recyclerView.context) {

    companion object {
        const val MIN_SMOOTH_SCROLL_DURATION_MS = 30
    }

    private var isCanceled = false

    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
        val smoothScrollSpeedFactor = layoutInfo.getConfiguration().smoothScrollSpeedFactor
        return super.calculateSpeedPerPixel(displayMetrics) * smoothScrollSpeedFactor
    }

    override fun calculateTimeForScrolling(dx: Int): Int {
        var ms = super.calculateTimeForScrolling(dx)
        val totalSpace = layoutInfo.getTotalSpace()
        if (totalSpace > 0) {
            ms = max(ms, MIN_SMOOTH_SCROLL_DURATION_MS * dx / totalSpace)
        }
        return ms
    }

    fun cancel() {
        isCanceled = true
    }

    fun isCanceled(): Boolean = isCanceled

}
