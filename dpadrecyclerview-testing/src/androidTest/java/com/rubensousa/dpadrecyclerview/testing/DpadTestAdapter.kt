package com.rubensousa.dpadrecyclerview.testing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.ViewHolderAlignment
import java.util.Collections

class DpadTestAdapter(private val showSubPositions: Boolean = false) :
    RecyclerView.Adapter<DpadTestAdapter.VH>() {

    private var items = ArrayList<Item>()

    init {
        repeat(1000) { value ->
            items.add(Item(value))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return if (showSubPositions) {
            SubPositionVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.dpadrecyclerview_test_item_subposition, parent, false
                )
            )
        } else {
            SimpleVH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.dpadrecyclerview_test_item_grid, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun addItem() {
        items.add(Item(value = items.size))
        notifyItemInserted(items.size - 1)
    }

    fun removeItem() {
        items.removeLast()
        notifyItemRemoved(items.size)
    }

    fun clearItems() {
        items.clear()
        notifyDataSetChanged()
    }

    fun moveLastItem() {
        Collections.swap(items, items.size - 1, 0)
        notifyItemMoved(items.size - 1, 0)
    }

    fun changeLastItem() {
        val lastItem = items.last()
        items[items.size - 1] = lastItem.copy(value = -lastItem.value)
        notifyItemChanged(items.size - 1)
    }

    data class Item(val value: Int)

    abstract class VH(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.isFocusable = true
            view.isFocusableInTouchMode = true
        }

        @CallSuper
        open fun bind(item: Item) {
            itemView.tag = item.value
        }
    }

    class SimpleVH(view: View) : VH(view) {

        private val textView: TextView = view.findViewById(R.id.textView)

        override fun bind(item: Item) {
            super.bind(item)
            textView.text = item.value.toString()
        }

    }

    class SubPositionVH(view: View) : VH(view), DpadViewHolder {

        private val alignments = ArrayList<ViewHolderAlignment>()

        init {
            alignments.apply {
                add(
                    ViewHolderAlignment(
                        offset = 0,
                        offsetRatio = 0.5f,
                        alignmentViewId = R.id.subPosition0TextView,
                        focusViewId = R.id.subPosition0TextView
                    )
                )
                add(
                    ViewHolderAlignment(
                        offset = 0,
                        offsetRatio = 0.5f,
                        alignmentViewId = R.id.subPosition1TextView,
                        focusViewId = R.id.subPosition1TextView
                    )
                )
                add(
                    ViewHolderAlignment(
                        offset = 0,
                        offsetRatio = 0.5f,
                        alignmentViewId = R.id.subPosition2TextView,
                        focusViewId = R.id.subPosition2TextView
                    )
                )
            }
        }

        override fun getAlignments(): List<ViewHolderAlignment> {
            return alignments
        }

    }


}