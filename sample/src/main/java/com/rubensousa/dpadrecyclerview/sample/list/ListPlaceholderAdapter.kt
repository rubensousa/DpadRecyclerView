package com.rubensousa.dpadrecyclerview.sample.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListPlaceholderBinding

class ListPlaceholderAdapter : RecyclerView.Adapter<ListPlaceholderAdapter.VH>() {

    private var show = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            AdapterListPlaceholderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ).root
        )
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

    }

    fun show(enabled: Boolean) {
        show = enabled
        notifyItemChanged(0)
    }

    override fun getItemCount(): Int {
        return if (show) {
            1
        } else {
            0
        }
    }

    class VH(view: View) : RecyclerView.ViewHolder(view)

}
