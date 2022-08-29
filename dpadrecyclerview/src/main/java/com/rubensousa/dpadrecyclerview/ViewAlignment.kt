package com.rubensousa.dpadrecyclerview

import android.view.View

interface ViewAlignment {

    companion object {
        /**
         * See [offset]
         */
        const val DEFAULT_OFFSET = 0

        /**
         * See [offsetRatio]
         */
        const val DEFAULT_OFFSET_RATIO = 0.5f
    }

    /**
     * The distance to the [offsetRatio] of the view in pixels.
     *
     * E.g offsetRatio = 0.5f, offset = 100, View's height = 500
     *
     * Keyline position = 500 * 0.5f + 100 = 350
     */
    val offset: Int

    /**
     * The keyline position for the alignment. Default: 0.5f (center)
     *
     * Set [isOffsetRatioEnabled] to false in case you want to disable this
     */
    val offsetRatio: Float

    /**
     * When enabled, [offsetRatio] will be used for the alignment.
     * Otherwise, only [offset] will be used.
     */
    val isOffsetRatioEnabled: Boolean

    /**
     * True if padding should be included for the alignment.
     * Includes start/top padding if [offsetRatio] is 0.0.
     * Includes end/bottom padding if [offsetRatio] is 1.0.
     * If [offsetRatio] is not 0.0 or 1.0, padding isn't included
     */
    val includePadding: Boolean

    /**
     *  When true, aligns to [View.getBaseline] for the view used for the alignment
     */
    val alignToBaseline: Boolean
}
