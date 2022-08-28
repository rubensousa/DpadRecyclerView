package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.DecorationLookup
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.*
import com.rubensousa.dpadrecyclerview.sample.databinding.ScreenTvNestedListsBinding
import com.rubensousa.dpadrecyclerview.sample.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.list.DpadStateHolder
import com.rubensousa.dpadrecyclerview.sample.list.NestedListAdapter
import com.rubensousa.dpadrecyclerview.sample.list.ListPlaceholderAdapter
import timber.log.Timber

class MainFragment : Fragment(R.layout.screen_tv_nested_lists) {

    private var _binding: ScreenTvNestedListsBinding? = null
    private val binding: ScreenTvNestedListsBinding get() = _binding!!
    private var selectedPosition = RecyclerView.NO_POSITION
    private val scrollStateHolder = DpadStateHolder()
    private val viewModel by viewModels<MainViewModel>()
    private val concatAdapter = ConcatAdapter(
        ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()
    )
    private val loadingAdapter = ListPlaceholderAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ScreenTvNestedListsBinding.bind(view)
        val nestedListAdapter = setupAdapter()
        setupAlignment(binding.recyclerView)
        setupPagination(binding.recyclerView)
        viewModel.listState.observe(viewLifecycleOwner) { list ->
            nestedListAdapter.submitList(list)
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            loadingAdapter.show(isLoading)
        }
        binding.recyclerView.requestFocus()
        if (selectedPosition != RecyclerView.NO_POSITION) {
            binding.recyclerView.setSelectedPosition(
                selectedPosition,
                smooth = false,
                object : ViewHolderTask() {
                    override fun execute(viewHolder: RecyclerView.ViewHolder) {
                        Timber.d("Selection state restored")
                    }
                })
        }
    }

    private fun setupAdapter(): NestedListAdapter {
        val nestedListAdapter = NestedListAdapter(scrollStateHolder,
            object : ItemViewHolder.ItemClickListener {
                override fun onViewHolderClicked() {
                    findNavController().navigate(R.id.open_detail)
                }
            })
        concatAdapter.addAdapter(nestedListAdapter)
        concatAdapter.addAdapter(loadingAdapter)
        nestedListAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerView.adapter = concatAdapter
        return nestedListAdapter
    }

    private fun setupPagination(recyclerView: DpadRecyclerView) {
        recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                selectedPosition = position
                viewModel.loadMore(selectedPosition)
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

    private fun setupAlignment(recyclerView: DpadRecyclerView) {
        recyclerView.setParentAlignment(
            ParentAlignment(
                edge = ParentAlignment.Edge.MIN,
                offset = 0,
                offsetRatio = 0.5f
            )
        )

        recyclerView.setChildAlignment(
            ChildAlignment(offset = 0, offsetRatio = 0.5f)
        )
        recyclerView.addItemDecoration(
            LinearMarginDecoration.createVertical(
                verticalMargin = resources.getDimensionPixelOffset(
                    R.dimen.item_spacing
                ),
                decorationLookup = object : DecorationLookup {
                    override fun shouldApplyDecoration(position: Int, itemCount: Int): Boolean {
                        return position != itemCount - 1 || !loadingAdapter.isShowing()
                    }
                }
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerView.adapter = null
        _binding = null
    }

}