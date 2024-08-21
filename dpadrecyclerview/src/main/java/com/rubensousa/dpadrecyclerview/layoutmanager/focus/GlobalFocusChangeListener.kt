package com.rubensousa.dpadrecyclerview.layoutmanager.focus

import android.view.View
import android.view.ViewParent
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

internal class GlobalFocusChangeListener(
    private val recyclerView: DpadRecyclerView,
    private val onFocusLost: () -> Unit,
) : OnGlobalFocusChangeListener {

    private var hadFocus = recyclerView.hasFocus()

    override fun onGlobalFocusChanged(oldFocus: View?, newFocus: View?) {
        if (hadFocus) {
            val oldFocusRecyclerView = oldFocus?.let { findParentRecyclerView(it) }
            if (oldFocusRecyclerView !== recyclerView) {
                return
            }
            val newFocusRecyclerView = newFocus?.let { findParentRecyclerView(it) }
            if (newFocusRecyclerView !== recyclerView) {
                onFocusLost()
                hadFocus = false
            }
        } else {
            hadFocus = recyclerView.hasFocus()
        }
    }

    private fun findParentRecyclerView(view: View): RecyclerView? {
        var parent: ViewParent? = view.parent
        while (parent != null) {
            if (parent is RecyclerView) {
                return parent
            }
            parent = parent.parent
        }
        return null
    }

}
