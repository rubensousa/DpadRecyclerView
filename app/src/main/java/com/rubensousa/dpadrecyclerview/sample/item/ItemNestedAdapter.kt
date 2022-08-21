package com.rubensousa.dpadrecyclerview.sample.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.databinding.AdapterNestedItemBinding
import com.rubensousa.tvfocus.recyclerview.item.ItemViewHolder

class ItemNestedAdapter : RecyclerView.Adapter<ItemViewHolder>() {

    private var list: List<Int> = emptyList()

    var clickListener: ItemViewHolder.ItemClickListener? = null

    @SuppressLint("NotifyDataSetChanged")
    fun replaceList(newList: List<Int>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding = AdapterNestedItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ItemViewHolder(binding.root, binding.textView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(list[position], clickListener)
    }

    override fun onViewRecycled(holder: ItemViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    override fun getItemCount(): Int = list.size


}
