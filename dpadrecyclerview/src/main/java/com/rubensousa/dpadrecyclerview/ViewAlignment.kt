/*
 * Copyright 2022 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview

import android.view.View

interface ViewAlignment {

    companion object {
        /**
         * See [offset]
         */
        const val DEFAULT_OFFSET = 0

        /**
         * See [fraction]
         */
        const val DEFAULT_FRACTION = 0.5f
    }

    /**
     * The distance to the [fraction] of the view in pixels.
     *
     * E.g fraction = 0.5f, offset = 100, View's height = 500
     *
     * Keyline position = 500 * 0.5f + 100 = 350
     */
    val offset: Int

    /**
     * The keyline position for the alignment. Default: 0.5f (center)
     *
     * Set [isFractionEnabled] to false in case you want to disable this
     */
    val fraction: Float

    /**
     * When enabled, [fraction] will be used for the alignment.
     * Otherwise, only [offset] will be used.
     */
    val isFractionEnabled: Boolean

    /**
     * True if padding should be included for the alignment.
     * Includes start/top padding if [fraction] is 0f.
     * Includes end/bottom padding if [fraction] is 1f.
     * If [fraction] is not 0f or 1f, padding isn't included
     */
    val includePadding: Boolean

    /**
     *  When true, aligns to [View.getBaseline] for the view used for the alignment
     */
    val alignToBaseline: Boolean
}
