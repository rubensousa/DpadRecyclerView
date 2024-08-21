package com.rubensousa.dpadrecyclerview

/**
 * Callback for receiving a notification when a [DpadRecyclerView] loses focus.
 *
 * To observe the focus gain event, use [DpadRecyclerView.addOnViewFocusedListener]
 */
interface OnFocusLostListener {

    fun onFocusLost(recyclerView: DpadRecyclerView)

}
