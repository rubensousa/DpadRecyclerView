package com.rubensousa.dpadrecyclerview

import androidx.recyclerview.widget.RecyclerView

/**
 * Task that's scheduled and executed when a ViewHolder is selected
 */
interface ViewHolderTask {
    fun run(viewHolder: RecyclerView.ViewHolder)
}
