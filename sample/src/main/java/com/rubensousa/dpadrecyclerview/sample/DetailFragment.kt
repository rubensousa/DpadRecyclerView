package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.GridSpanMarginDecoration
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenTvDetailBinding
import com.rubensousa.dpadrecyclerview.sample.item.ItemGridAdapter
import com.rubensousa.dpadrecyclerview.sample.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.list.ListHeaderAdapter
import com.rubensousa.dpadrecyclerview.sample.list.ListPlaceholderAdapter
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
    private val viewModel by viewModels<DetailViewModel>()
    private val loadingAdapter = ListPlaceholderAdapter(
        items = 5,
        focusPlaceholders = true
    )
    private val itemAdapter = ItemGridAdapter(object : ItemViewHolder.ItemClickListener {
        override fun onViewHolderClicked() {
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ScreenTvDetailBinding.bind(view)
        val recyclerView = binding.recyclerView
        setupAdapter(recyclerView)
        binding.up.setOnClickListener {
            val subPosition = recyclerView.getSelectedSubPosition()
            if (subPosition > 0) {
                recyclerView.setSelectedSubPositionSmooth(subPosition - 1)
            } else {
                recyclerView.setSelectedPositionSmooth(
                    recyclerView.getSelectedPosition() - 1
                )
            }
        }
        binding.down.setOnClickListener {
            val subPosition = recyclerView.getSelectedSubPosition()
            val subPositionCount = recyclerView.getCurrentSubPositions()
            if (subPosition < subPositionCount - 1) {
                recyclerView.setSelectedSubPositionSmooth(subPosition + 1)
            } else {
                recyclerView.setSelectedPositionSmooth(
                    recyclerView.getSelectedPosition() + 1
                )
            }
        }
        viewModel.listState.observe(viewLifecycleOwner) { list ->
            itemAdapter.submitList(list)
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            loadingAdapter.show(isLoading)
        }
        recyclerView.requestFocus()
    }

    private fun setupAdapter(recyclerView: DpadRecyclerView) {
        val concatAdapter = ConcatAdapter(
            ConcatAdapter.Config.Builder()
                .setIsolateViewTypes(true)
                .build()
        )
        val headerAdapter = ListHeaderAdapter()
        headerAdapter.submitList(listOf("Header", "Header"))
        concatAdapter.addAdapter(headerAdapter)
        concatAdapter.addAdapter(itemAdapter)
        concatAdapter.addAdapter(loadingAdapter)
        recyclerView.setSpanSizeLookup(object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (position < headerAdapter.itemCount) {
                    recyclerView.getSpanCount()
                } else {
                    1
                }
            }
        })
        recyclerView.setChildAlignment(topChildAlignment)
        recyclerView.setParentAlignment(topParentAlignment)
        recyclerView.addItemDecoration(
            GridSpanMarginDecoration.create(
                margin = binding.root.context.resources.getDimensionPixelOffset(
                    R.dimen.item_spacing
                ),
                recyclerView.getDpadLayoutManager()
            )
        )
        recyclerView.adapter = concatAdapter
        recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                if (position > 6) {
                    recyclerView.setAlignments(
                        centerParentAlignment,
                        centerChildAlignment,
                        smooth = true
                    )
                } else {
                    recyclerView.setAlignments(
                        topParentAlignment,
                        topChildAlignment,
                        smooth = true
                    )
                }
                Timber.d("Selected: $position, $subPosition")
                viewModel.loadMore(position)
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }


}
