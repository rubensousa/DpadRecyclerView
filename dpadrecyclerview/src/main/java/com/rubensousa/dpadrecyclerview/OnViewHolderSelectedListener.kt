package com.rubensousa.dpadrecyclerview

import androidx.recyclerview.widget.RecyclerView


/**
 * Interface for receiving notification when a child of this ViewGroup has been selected.
 * There are two methods:
 *
 *  * [onViewHolderSelected] is called when the view holder is about to be selected.
 *  The listener could change size of the view holder in this callback.
 *
 *  * [onViewHolderSelectedAndAligned] is called when view holder has been selected
 *  and laid out in RecyclerView.
 */
// TODO Clear docs
interface OnViewHolderSelectedListener {

    /**
     * Callback method to be invoked when a child of this ViewGroup has been selected. Listener
     * might change the size of the child and the position of the child is not finalized. To get
     * the final layout position of child, override [.onChildViewHolderSelectedAndPositioned].
     *
     * @param parent      The RecyclerView where the selection happened.
     * @param child       The ViewHolder within the RecyclerView that is selected, or null if no
     * view is selected.
     * @param position    The position of the view in the adapter, or NO_POSITION
     * if no view is selected.
     * @param subPosition The index of which [ItemAlignmentDef] being used,
     * 0 if there is no ItemAlignmentDef defined for the item.
     */
    fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
    }

    /**
     * Callback method to be invoked when a child of this ViewGroup has been selected and
     * aligned to its final position.
     *
     * @param parent      The RecyclerView where the selection happened.
     * @param child       The ViewHolder within the RecyclerView that is selected, or null if no
     * view is selected.
     * @param position    The position of the view in the adapter, or NO_POSITION
     * if no view is selected.
     * @param subPosition The index of which [ItemAlignmentDef] being used,
     * 0 if there is no ItemAlignmentDef defined for the item.
     */
    fun onViewHolderSelectedAndAligned(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
    }
}