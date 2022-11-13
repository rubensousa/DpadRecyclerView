package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerViewHelper
import com.rubensousa.dpadrecyclerview.sample.databinding.AdapterItemGridBinding

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
                AdapterItemGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(position)
        }

        override fun getItemCount(): Int = 200

    }


    class VH(private val adapterItemBinding: AdapterItemGridBinding) :
        RecyclerView.ViewHolder(adapterItemBinding.root) {

        init {
            adapterItemBinding.root.updateLayoutParams<ViewGroup.LayoutParams> {
                height =
                    itemView.resources.getDimensionPixelOffset(R.dimen.adapter_small_item_height)
            }
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