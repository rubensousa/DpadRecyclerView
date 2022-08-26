package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.GridSpanMarginDecoration
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.R
import com.rubensousa.dpadrecyclerview.databinding.ScreenTvDetailBinding
import com.rubensousa.dpadrecyclerview.sample.item.ItemGridAdapter
import com.rubensousa.dpadrecyclerview.sample.list.ListHeaderAdapter
import com.rubensousa.dpadrecyclerview.sample.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.list.ListModel
import timber.log.Timber

class DetailFragment : Fragment(R.layout.screen_tv_detail) {

    private var _binding: ScreenTvDetailBinding? = null
    private val binding: ScreenTvDetailBinding get() = _binding!!
    private val topParentAlignment = ParentAlignment(
        edge = Edge.NONE,
        offset = 0,
        offsetRatio = 0.05f
    )
    private val topChildAlignment = ChildAlignment(offset = 0, offsetRatio = 0f)
    private val centerParentAlignment = ParentAlignment(
        edge = Edge.NONE,
        offset = 0,
        offsetRatio = 0.5f
    )
    private val centerChildAlignment = ChildAlignment(offset = 0, offsetRatio = 0.5f)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ScreenTvDetailBinding.bind(view)
        setupAdapter()
        binding.up.setOnClickListener {
            val subPosition = binding.recyclerView.getSelectedSubPosition()
            if (subPosition > 0) {
                binding.recyclerView.setSelectedSubPosition(subPosition - 1, smooth = true)
            } else {
                binding.recyclerView.setSelectedPosition(
                    binding.recyclerView.getSelectedPosition() - 1,
                    smooth = true
                )
            }
        }
        binding.down.setOnClickListener {
            val subPosition = binding.recyclerView.getSelectedSubPosition()
            val subPositionCount = binding.recyclerView.getCurrentSubPositions()
            if (subPosition < subPositionCount - 1) {
                binding.recyclerView.setSelectedSubPosition(subPosition + 1, smooth = true)
            } else {
                binding.recyclerView.setSelectedPosition(
                    binding.recyclerView.getSelectedPosition() + 1,
                    smooth = true
                )
            }
        }
        binding.recyclerView.requestFocus()
    }

    private fun setupAdapter() {
        val concatAdapter = ConcatAdapter()
        val headerAdapter = ListHeaderAdapter()
        val itemAdapter = ItemGridAdapter(object : ItemViewHolder.ItemClickListener {
            override fun onViewHolderClicked() {
            }
        })
        val items = ArrayList<Int>()
        for (i in 0 until 10000) {
            items.add(i)
        }
        itemAdapter.submitList(items)
        headerAdapter.submitList(listOf("Header", "Header"))
        val list = ArrayList<ListModel>()
        for (i in 0 until 100) {
            list.add(generateList("List $i"))
        }
        concatAdapter.addAdapter(headerAdapter)
        concatAdapter.addAdapter(itemAdapter)
        binding.recyclerView.setSpanSizeLookup(object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position < headerAdapter.itemCount) {
                    return binding.recyclerView.getSpanCount()
                } else {
                    return 1
                }
            }
        })
        binding.recyclerView.setChildAlignment(topChildAlignment)
        binding.recyclerView.setParentAlignment(topParentAlignment)
        binding.recyclerView.addItemDecoration(
            GridSpanMarginDecoration.create(
                margin = binding.root.context.resources.getDimensionPixelOffset(
                    R.dimen.item_spacing
                ),
                binding.recyclerView.requireDpadLayoutManager()
            )
        )
        binding.recyclerView.adapter = concatAdapter
        binding.recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                if (position > 6) {
                    binding.recyclerView.setAlignments(
                        centerParentAlignment,
                        centerChildAlignment,
                        smooth = true
                    )
                } else {
                    binding.recyclerView.setAlignments(
                        topParentAlignment,
                        topChildAlignment,
                        smooth = true
                    )
                }
                Timber.d("Selected: $position, $subPosition")
            }

            override fun onViewHolderSelectedAndAligned(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                Timber.d("Aligned: $position, $subPosition")
            }
        })
    }

    private fun generateList(title: String): ListModel {
        val items = ArrayList<Int>()
        repeat(100) {
            items.add(it)
        }
        return ListModel(title, items)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
