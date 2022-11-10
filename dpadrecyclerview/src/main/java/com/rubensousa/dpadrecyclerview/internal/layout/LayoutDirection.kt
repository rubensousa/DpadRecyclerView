package com.rubensousa.dpadrecyclerview.internal.layout

/**
 * Direction in which the layout is being filled.
 * These are absolute directions, so it doesn't consider RTL at all
 */
internal enum class LayoutDirection {
    /**
     * Either left in horizontal or top in vertical
     */
    START,

    /**
     * Either right in horizontal or bottom in vertical
     */
    END
}
