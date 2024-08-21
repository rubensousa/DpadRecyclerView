package com.rubensousa.dpadrecyclerview

/**
 * Callback for receiving a notification when a [DpadRecyclerView] loses focus.
 *
 * To observe the focus gain event, use [DpadRecyclerView.addOnViewFocusedListener]
 */
interface OnFocusLostListener {

    /**
     * @param recyclerView the [DpadRecyclerView] that lost focus
     */
    fun onFocusLost(recyclerView: DpadRecyclerView)

}
