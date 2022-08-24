package com.rubensousa.dpadrecyclerview

import androidx.recyclerview.widget.RecyclerView


/**
 * Interface for receiving a notification when a ViewHolder has been selected.
 * There are two methods:
 *
 *  * [onViewHolderSelected] is called when the ViewHolder has been selected
 *
 *  * [onViewHolderSelectedAndAligned] is called when the ViewHolder has been selected
 *  and aligned out to its final position
 */
interface OnViewHolderSelectedListener {

    /**
     * @param parent      The RecyclerView where the selection happened.
     * @param child       The ViewHolder within the RecyclerView that is selected, or null if no
     * view is selected.
     * @param position    The position of the view in the adapter, or NO_POSITION
     * if no view is selected.
     * @param subPosition The index of the alignment from [DpadViewHolder.getAlignments],
     * or 0 if there is no custom alignment
     */
    fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
    }

    /**
     * Called when [child] has scrolled to its final position
     *
     * @param parent      The RecyclerView where the selection happened.
     * @param child       The ViewHolder within the RecyclerView that is selected, or null if no
     * view is selected.
     * @param position    The position of the view in the adapter, or NO_POSITION
     * if no view is selected.
     * @param subPosition The index of the alignment from [DpadViewHolder.getAlignments],
     * or 0 if there is no custom alignment
     */
    fun onViewHolderSelectedAndAligned(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
    }

}
