package com.rubensousa.dpadrecyclerview.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.rubensousa.dpadrecyclerview.DpadViewHolder

class TestAdapter(
    private val adapterLayoutId: Int,
    private val alternateFocus: Boolean
) : ListAdapter<Int, TestAdapter.ItemViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Int>() {
            override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(adapterLayoutId, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
        val isFocusable = if (alternateFocus) {
            position % 2 == 0
        } else {
            true
        }
        holder.itemView.isFocusable = isFocusable
        holder.itemView.isFocusableInTouchMode = isFocusable
    }

    class ItemViewHolder(view: View) : TestViewHolder(view), DpadViewHolder {

        private val textView = view.findViewById<TextView>(R.id.textView)

        fun bind(index: Int) {
            textView.text = index.toString()
        }

    }

}
