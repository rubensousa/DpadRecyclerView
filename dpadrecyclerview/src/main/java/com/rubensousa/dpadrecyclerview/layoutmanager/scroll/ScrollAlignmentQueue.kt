package com.rubensousa.dpadrecyclerview.layoutmanager.scroll

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.FocusDirection
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import java.util.LinkedList
import kotlin.math.sign

internal class ScrollAlignmentQueue(
    private val configuration: LayoutConfiguration,
    private val layoutAlignment: LayoutAlignment,
    private val layoutInfo: LayoutInfo
) {

    private var pendingAlignments = LinkedList<PendingAlignment>()

    fun consumeAll() {
        if (!hasMaxPendingAlignments()) {
            return
        }
        pendingAlignments.clear()
    }

    fun hasReachedLimit(focusDirection: FocusDirection): Boolean {
        if (!hasMaxPendingAlignments()) {
            return false
        }
        consumeAlignedViews(focusDirection.getScrollSign(layoutInfo.shouldReverseLayout()))
        return pendingAlignments.size == configuration.maxPendingAlignments
    }

    private fun hasMaxPendingAlignments(): Boolean {
        // There's no pending alignments if we don't allow smooth scrolling
        if (!configuration.isSmoothFocusChangesEnabled) {
            return false
        }
        return configuration.maxPendingAlignments != Int.MAX_VALUE
    }

    fun push(focusedView: View, childView: View?, scrollOffset: Int): Boolean {
        if (!hasMaxPendingAlignments() || scrollOffset == 0) {
            return true
        }

        consumeAlignedViews(scrollOffset)

        if (pendingAlignments.size < configuration.maxPendingAlignments) {
            // Don't allow duplicate pending entries
            pendingAlignments.removeAll { entry ->
                entry.view === focusedView && entry.childView == childView
            }
            pendingAlignments.addLast(
                PendingAlignment(
                    focusedView, childView, scrollOffset.sign
                )
            )
            return true
        }
        return false
    }

    private fun consumeAlignedViews(targetScrollOffset: Int) {
        // Check if the last pending alignment is in the opposite direction. If so, remove it
        val lastAlignment = pendingAlignments.peekLast()
        if (lastAlignment != null && lastAlignment.sign != targetScrollOffset.sign) {
            pendingAlignments.removeLast()
        }

        val iterator = pendingAlignments.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val scrollOffset = layoutAlignment.calculateScrollOffset(entry.view, entry.childView)
            if (scrollOffset == 0
                || isPendingAlignmentInOppositeDirection(entry, scrollOffset)
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

    private fun isPendingAlignmentInOppositeDirection(
        alignment: PendingAlignment,
        scrollOffset: Int
    ): Boolean {
        return scrollOffset.sign != alignment.sign
    }

    data class PendingAlignment(val view: View, val childView: View?, val sign: Int)
}
