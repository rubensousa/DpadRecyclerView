package com.rubensousa.dpadrecyclerview.layoutmanager.scroll

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import java.util.*
import kotlin.math.sign

internal class ScrollAlignmentQueue(
    private val configuration: LayoutConfiguration,
    private val alignment: LayoutAlignment,
    private val layoutInfo: LayoutInfo
) {

    private var pendingAlignments = LinkedList<PendingAlignment>()

    fun consumeAll() {
        if (!hasMaxPendingAlignments()) {
            return
        }
        pendingAlignments.clear()
    }

    fun hasReachedLimit(): Boolean {
        if (!hasMaxPendingAlignments()) {
            return false
        }
        consumeAlignedViews()
        return pendingAlignments.size == configuration.maxPendingAlignments
    }

    private fun hasMaxPendingAlignments(): Boolean {
        // There's no pending alignments if we don't allow smooth scrolling
        if (!configuration.isSmoothFocusChangesEnabled) {
            return false
        }
        return configuration.maxPendingAlignments != Int.MAX_VALUE
    }

    fun add(focusedView: View, childView: View?, scrollOffset: Int) {
        if (!hasMaxPendingAlignments()) {
            return
        }
        consumeAlignedViews()
        if (scrollOffset == 0) {
            return
        }
        if (pendingAlignments.size < configuration.maxPendingAlignments) {
            // Don't allow duplicate pending entries
            pendingAlignments.removeAll { entry ->
                entry.view === focusedView && entry.childView == childView
            }
            pendingAlignments.addLast(
                PendingAlignment(
                    focusedView, childView, sign(scrollOffset.toFloat())
                )
            )
        }
    }

    private fun consumeAlignedViews() {
        val iterator = pendingAlignments.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val scrollOffset = alignment.calculateScrollOffset(entry.view, entry.childView)
            if (scrollOffset == 0
                || isScrollingInOppositeDirection(scrollOffset, entry.sign)
                || isRemoved(entry.view)
            ) {
                iterator.remove()
            }
        }
    }

    private fun isRemoved(view: View): Boolean {
        val viewHolder = layoutInfo.getChildViewHolder(view) ?: return true
        val layoutParams = viewHolder.itemView.layoutParams as RecyclerView.LayoutParams
        return layoutParams.isItemRemoved
    }

    private fun isScrollingInOppositeDirection(offset: Int, originalSign: Float): Boolean {
        val offsetSign = sign(offset.toFloat())
        return offsetSign != originalSign
    }

    data class PendingAlignment(val view: View, val childView: View?, val sign: Float)
}
