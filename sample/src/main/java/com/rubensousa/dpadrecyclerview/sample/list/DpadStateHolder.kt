package com.rubensousa.dpadrecyclerview.sample.list

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener

class DpadStateHolder {

    private val positions = LinkedHashMap<String, Int>()
    private val listeners = LinkedHashMap<String, OnViewHolderSelectedListener>()

    fun register(recyclerView: DpadRecyclerView, key: String) {
        val restoredPosition = positions[key] ?: 0
        recyclerView.setSelectedPosition(restoredPosition, false)
        val listener = object : OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                super.onViewHolderSelected(parent, child, position, subPosition)
                positions[key] = position
            }
        }
        recyclerView.addOnViewHolderSelectedListener(listener)
        listeners[key] = listener
    }

    fun unregister(recyclerView: DpadRecyclerView, key: String) {
        listeners.remove(key)?.let { listener ->
            recyclerView.removeOnViewHolderSelectedListener(listener)
            recyclerView.setSelectedPosition(position = 0, smooth = false)
        }
    }


}
