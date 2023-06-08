package com.rubensousa.dpadrecyclerview

/**
 * Defines how items are looped around in [DpadRecyclerView].
 *
 * Looping is only supported for single span layouts.
 *
 * If the layout doesn't fill the entire viewport, then looping is disabled
 */
enum class DpadLoopDirection {

    /**
     * Disable looping the adapter contents
     */
    NONE,

    /**
     * Only allow infinite scrolling from the max edge.
     * When returning to the min edge, stop looping
     */
    MAX,

    /**
     * Mirrors both the min and max edge, so that infinite scroll is supported in both directions
     */
    MIN_MAX;
}