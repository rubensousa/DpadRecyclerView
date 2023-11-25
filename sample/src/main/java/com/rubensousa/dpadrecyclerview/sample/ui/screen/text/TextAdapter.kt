package com.rubensousa.dpadrecyclerview.sample.ui.screen.text

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterItemTextBinding


class TextAdapter : ListAdapter<String, TextAdapter.TextItemViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextItemViewHolder {
        return TextItemViewHolder(
            AdapterItemTextBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TextItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TextItemViewHolder(
        val binding: AdapterItemTextBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(text: String) {
            binding.textView.text = text
        }

    }


}

