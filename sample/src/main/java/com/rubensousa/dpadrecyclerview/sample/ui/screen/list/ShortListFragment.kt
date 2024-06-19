package com.rubensousa.dpadrecyclerview.sample.ui.screen.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.FocusableDirection
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.sample.R
import com.rubensousa.dpadrecyclerview.sample.databinding.MainAdapterItemFeatureBinding
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenRecyclerviewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.dpToPx
import com.rubensousa.dpadrecyclerview.sample.ui.viewBinding
import com.rubensousa.dpadrecyclerview.sample.ui.widgets.common.ItemAnimator
import com.rubensousa.dpadrecyclerview.spacing.DpadLinearSpacingDecoration

class ShortListFragment : Fragment(R.layout.screen_recyclerview) {

    private val binding by viewBinding(ScreenRecyclerviewBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.apply {
            setParentAlignment(
                ParentAlignment(
                    edge = ParentAlignment.Edge.NONE,
                    fraction = 0.5f
                )
            )
            setFocusableDirection(FocusableDirection.CIRCULAR)
            addItemDecoration(
                DpadLinearSpacingDecoration.create(
                    itemSpacing = dpToPx(16.dp),
                    perpendicularEdgeSpacing = dpToPx(48.dp)
                )
            )
            adapter = Adapter(
                items = List(4) { i ->
                    "Item $i"
                }
            )
            requestFocus()
        }
    }

    private class Adapter(
        private val items: List<String>,
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                MainAdapterItemFeatureBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun getItemCount(): Int = items.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(items[position])
        }

        override fun onViewRecycled(holder: ViewHolder) {
            super.onViewRecycled(holder)
            holder.recycle()
        }

        class ViewHolder(
            private val binding: MainAdapterItemFeatureBinding,
        ) : RecyclerView.ViewHolder(binding.root), DpadViewHolder {

            private val animator = ItemAnimator(binding.root)

            init {
                itemView.setOnFocusChangeListener { v, hasFocus ->
                    if (hasFocus) {
                        animator.startFocusGainAnimation()
                    } else {
                        animator.startFocusLossAnimation()
                    }
                }
            }

            fun bind(item: String) {
                binding.textView.text = item
            }

            fun recycle() {
                animator.cancel()
            }

        }

    }

}
