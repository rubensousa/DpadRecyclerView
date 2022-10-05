package com.rubensousa.dpadrecyclerview.sample.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.sample.R

class ListPlaceholderAdapter(
    private val items: Int = 1,
    private val layoutId: Int = R.layout.adapter_list_placeholder,
    private val focusPlaceholders: Boolean = false
) : RecyclerView.Adapter<ListPlaceholderAdapter.VH>() {

    private var show = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(
            layoutId, parent, false
        )
        view.isFocusableInTouchMode = focusPlaceholders
        view.isFocusable = focusPlaceholders
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

    }

    fun show(enabled: Boolean) {
        val wasShowing = show
        show = enabled
        if (!wasShowing && show) {
            notifyItemRangeInserted(0, items)
        } else if (wasShowing && !show) {
            notifyItemRangeRemoved(0, items)
        }
    }

    fun isShowing() = show

    override fun getItemCount(): Int {
        return if (show) {
            items
        } else {
            0
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view)

}
