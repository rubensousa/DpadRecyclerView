package com.rubensousa.dpadrecyclerview.sample.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.R
import com.rubensousa.dpadrecyclerview.databinding.AdapterListHeaderBinding

class ListHeaderAdapter : ListAdapter<String, ListHeaderAdapter.VH>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val viewHolder = VH(
            AdapterListHeaderBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
        viewHolder.itemView.isFocusableInTouchMode = true
        viewHolder.itemView.isFocusable = true
        return viewHolder
    }

    override fun onBindViewHolder(holder: VH, position: Int) {

    }

    class VH(
        binding: AdapterListHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

        private val childAlignments = ArrayList<ChildAlignment>()

        init {
            childAlignments.apply {
                add(
                    ChildAlignment(
                        offset = 0,
                        offsetPercent = 50f,
                        alignmentViewId = R.id.subPosition0TextView,
                        focusViewId = R.id.subPosition0TextView
                    )
                )
                add(
                    ChildAlignment(
                        offset = 0,
                        offsetPercent = 50f,
                        alignmentViewId = R.id.subPosition1TextView,
                        focusViewId = R.id.subPosition1TextView
                    )
                )
                add(
                    ChildAlignment(
                        offset = 0,
                        offsetPercent = 50f,
                        alignmentViewId = R.id.subPosition2TextView,
                        focusViewId = R.id.subPosition2TextView
                    )
                )
            }
        }

        override fun getAlignments(): List<ChildAlignment> {
            return childAlignments
        }

    }


}
