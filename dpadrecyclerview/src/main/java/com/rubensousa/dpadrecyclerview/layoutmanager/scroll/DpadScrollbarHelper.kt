/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * A helper class to do scroll offset calculations.
 * Adapted from ScrollbarHelper of androidx.recyclerview
 */
internal object DpadScrollbarHelper {

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    fun computeScrollOffset(
        state: RecyclerView.State,
        orientationHelper: OrientationHelper,
        startChild: View?,
        endChild: View?,
        lm: RecyclerView.LayoutManager,
        smoothScrollbarEnabled: Boolean,
        reverseLayout: Boolean
    ): Int {
        if (lm.childCount == 0 || state.itemCount == 0 || startChild == null || endChild == null) {
            return 0
        }
        val minPosition = Math.min(lm.getPosition(startChild), lm.getPosition(endChild))
        val maxPosition = Math.max(lm.getPosition(startChild), lm.getPosition(endChild))
        val itemsBefore = if (reverseLayout) {
            Math.max(0, state.itemCount - maxPosition - 1)
        } else {
            Math.max(0, minPosition)
        }
        if (!smoothScrollbarEnabled) {
            return itemsBefore
        }
        val laidOutArea = Math.abs(
            orientationHelper.getDecoratedEnd(endChild)
                    - orientationHelper.getDecoratedStart(startChild)
        )
        val itemRange = Math.abs((lm.getPosition(startChild) - lm.getPosition(endChild))) + 1
        val avgSizePerRow = laidOutArea.toFloat() / itemRange
        return Math.round(
            itemsBefore * avgSizePerRow + ((orientationHelper.startAfterPadding
                    - orientationHelper.getDecoratedStart(startChild)))
        )
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    fun computeScrollExtent(
        state: RecyclerView.State,
        orientationHelper: OrientationHelper,
        startChild: View?,
        endChild: View?,
        lm: RecyclerView.LayoutManager,
        smoothScrollbarEnabled: Boolean
    ): Int {
        if ((lm.childCount == 0) || (state.itemCount == 0) || (startChild == null
                    ) || (endChild == null)
        ) {
            return 0
        }
        if (!smoothScrollbarEnabled) {
            return Math.abs(lm.getPosition(startChild) - lm.getPosition(endChild)) + 1
        }
        val extend = (orientationHelper.getDecoratedEnd(endChild)
                - orientationHelper.getDecoratedStart(startChild))
        return Math.min(orientationHelper.totalSpace, extend)
    }

    /**
     * @param startChild View closest to start of the list. (top or left)
     * @param endChild   View closest to end of the list (bottom or right)
     */
    fun computeScrollRange(
        state: RecyclerView.State,
        orientationHelper: OrientationHelper,
        startChild: View?,
        endChild: View?,
        lm: RecyclerView.LayoutManager,
        smoothScrollbarEnabled: Boolean
    ): Int {
        if ((lm.childCount == 0)
            || (state.itemCount == 0)
            || (startChild == null)
            || (endChild == null)
        ) {
            return 0
        }
        if (!smoothScrollbarEnabled) {
            return state.itemCount
        }
        // smooth scrollbar enabled. try to estimate better.
        val laidOutArea = (orientationHelper.getDecoratedEnd(endChild)
                - orientationHelper.getDecoratedStart(startChild))
        val laidOutRange = (Math.abs(lm.getPosition(startChild) - lm.getPosition(endChild)) + 1)
        // estimate a size for full list.
        return (laidOutArea.toFloat() / laidOutRange * state.itemCount).toInt()
    }

}
