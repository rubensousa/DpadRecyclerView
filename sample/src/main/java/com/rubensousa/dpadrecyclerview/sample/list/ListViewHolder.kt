package com.rubensousa.dpadrecyclerview.sample.list

import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterListBinding
import com.rubensousa.dpadrecyclerview.sample.item.ItemNestedAdapter
import com.rubensousa.dpadrecyclerview.sample.item.ItemViewHolder

class ListViewHolder(private val binding: AdapterListBinding) :
    RecyclerView.ViewHolder(binding.root), DpadViewHolder {

    private val adapter = ItemNestedAdapter()
    private var key: String? = null

    init {
        itemView.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                binding.recyclerView.requestFocus()
            }
        }
        setupRecyclerView(binding.recyclerView)
        onViewHolderDeselected()
    }

    fun bind(
        list: ListModel, stateHolder: DpadStateHolder,
        clickListener: ItemViewHolder.ItemClickListener
    ) {
        adapter.clickListener = clickListener
        key = list.title
        binding.textView.text = list.title
        adapter.replaceList(list.items)
        binding.recyclerView.adapter = adapter
        stateHolder.register(binding.recyclerView, list.title)
    }

    fun onRecycled(stateHolder: DpadStateHolder) {
        adapter.clickListener = null
        key?.let { scrollKey ->
            stateHolder.unregister(binding.recyclerView, scrollKey)
        }
        binding.recyclerView.adapter = null
    }

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        binding.recyclerView.alpha = 1.0f
        binding.textView.alpha = 1.0f
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        binding.recyclerView.alpha = 0.5f
        binding.textView.alpha = 0.5f
    }

    fun onAttachedToWindow() {}

    fun onDetachedFromWindow() {}

    private fun setupRecyclerView(recyclerView: DpadRecyclerView) {
        recyclerView.apply {
            setGravity(Gravity.CENTER)
            addItemDecoration(
                LinearMarginDecoration.createHorizontal(
                    horizontalMargin = binding.root.context.resources.getDimensionPixelOffset(
                        R.dimen.item_spacing
                    ) / 2
                )
            )
            setParentAlignment(
                ParentAlignment(
                    edge = Edge.MAX,
                    offset = binding.root.resources.getDimensionPixelOffset(
                        R.dimen.list_margin_start
                    ),
                    isOffsetRatioEnabled = false
                )
            )
            setChildAlignment(
                ChildAlignment(offset = 0, offsetRatio = 0f)
            )
        }
    }

}
