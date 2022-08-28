package com.rubensousa.dpadrecyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * Task that's scheduled and executed when a ViewHolder is selected
 *
 * @param executeWhenAligned if this task should only be executed
 * when a ViewHolder is aligned to its final position,
 * or **false** if it should be executed immediately after the selection
 */
abstract class ViewHolderTask(val executeWhenAligned: Boolean = false) {
    abstract fun execute(viewHolder: RecyclerView.ViewHolder)
}
