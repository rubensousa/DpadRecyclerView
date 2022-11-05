package com.rubensousa.dpadrecyclerview.testing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import java.util.Collections

class DpadTestFragment : Fragment(R.layout.dpadrecyclerview_test_container),
    OnViewHolderSelectedListener {

    private val selectionEvents = ArrayList<DpadSelectionEvent>()
    private val adapter = Adapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        recyclerView.requestFocus()
        recyclerView.setParentAlignment(
            ParentAlignment(
                edge = ParentAlignment.Edge.NONE
            )
        )
        recyclerView.setFocusableDirection(FocusableDirection.CONTINUOUS)
        recyclerView.setSpanCount(5)
        recyclerView.addOnViewHolderSelectedListener(this)
        recyclerView.adapter = adapter
    }

    override fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        super.onViewHolderSelected(parent, child, position, subPosition)
        selectionEvents.add(DpadSelectionEvent(position, subPosition))
    }

    fun getSelectionEvents(): List<DpadSelectionEvent> {
        return ArrayList(selectionEvents)
    }

    fun getAdapterSize() = adapter.itemCount

    fun insertItem() {
        postAction { adapter.addItem() }
    }

    fun removeItem() {
        postAction { adapter.removeItem() }
    }

    fun clearItems() {
        postAction { adapter.clearItems() }
    }

    fun changeLastItem() {
        postAction { adapter.changeLastItem() }
    }

    fun moveLastItem() {
        postAction { adapter.moveLastItem() }
    }

    private fun postAction(action: () -> Unit) {
        view?.postDelayed(action, 1000L)
    }

    data class Item(val value: Int)

    class Adapter : RecyclerView.Adapter<VH>() {

        private var items = ArrayList<Item>()

        init {
            repeat(1000) { value ->
                items.add(Item(value))
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.dpadrecyclerview_test_item_grid,
                    parent, false
                )
            )
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

    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {

        private val textView: TextView = view.findViewById(R.id.textView)

        init {
            view.isFocusable = true
            view.isFocusableInTouchMode = true
        }

        fun bind(item: Item) {
            itemView.tag = item.value
            textView.text = item.value.toString()
        }

    }


}
