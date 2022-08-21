package com.rubensousa.dpadrecyclerview

import android.view.View
import androidx.annotation.VisibleForTesting
import java.util.*

/**
 * A ViewHolder managed by [DpadGridLayoutManager].
 *
 * Implement this in case you're interested in receiving selection changes or customising alignment
 *
 * For receiving focus changes, use the standard [View.setOnFocusChangeListener] instead
 */
interface DpadViewHolder {

    /**
     * Will be called whenever this ViewHolder is the current one selected.
     *
     * This is NOT the same as gaining focus.
     *
     * To observe focus changes,
     * you need to use the focus listener set via [View.setOnFocusChangeListener]
     *
     * This is called automatically by [DpadGridLayoutManager] on selection changes.
     */
    fun onViewHolderSelected() {}

    /**
     * Will be called whenever this ViewHolder is no longer the current one selected.
     *
     * This is NOT the same as losing focus.
     *
     * To observe focus changes,
     * you need to use the focus listener set via [View.setOnFocusChangeListener]
     *
     * This is called automatically by [DpadGridLayoutManager] on selection changes.
     */
    fun onViewHolderDeselected() {}

    /**
     * @return the alignment configurations to use for this ViewHolder,
     * or empty if it should be aligned using the configuration of the [DpadRecyclerView]
     */
    fun getAlignments(): List<ChildAlignment> {
        return Collections.emptyList()
    }

    /**
     * @return true if this ViewHolder received a [onViewHolderSelected] and is now selected
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun isViewHolderSelected(): Boolean = false

}
