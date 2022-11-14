package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerViewHelper
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterNestedItemBinding

class CustomLayoutActivity : AppCompatActivity() {

    companion object {
        init {
            DpadRecyclerViewHelper.enableNewPivotLayoutManager(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_layout)
        val recyclerView = findViewById<DpadRecyclerView>(R.id.recyclerView)
        recyclerView.addItemDecoration(
            LinearMarginDecoration.createVertical(
                verticalMargin = resources.getDimensionPixelOffset(R.dimen.item_spacing) / 2
            )
        )
        recyclerView.requestFocus()
        recyclerView.adapter = Adapter()
    }


    class Adapter : RecyclerView.Adapter<VH>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                AdapterNestedItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = 100

    }


    class VH(private val adapterItemBinding: AdapterNestedItemBinding) :
        RecyclerView.ViewHolder(adapterItemBinding.root) {

        init {
            adapterItemBinding.root.apply {
                isFocusable = true
                isFocusableInTouchMode = true
            }
        }

        fun bind(item: Int) {
            adapterItemBinding.textView.text = item.toString()
        }
    }
}