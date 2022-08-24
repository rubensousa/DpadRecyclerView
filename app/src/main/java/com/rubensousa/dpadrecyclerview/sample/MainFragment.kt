package com.rubensousa.dpadrecyclerview.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.decorator.LinearMarginDecoration
import com.rubensousa.dpadrecyclerview.*
import com.rubensousa.dpadrecyclerview.databinding.ScreenTvNestedListsBinding
import com.rubensousa.dpadrecyclerview.sample.list.DpadStateHolder
import com.rubensousa.dpadrecyclerview.sample.item.ItemViewHolder
import com.rubensousa.dpadrecyclerview.sample.list.ListModel
import com.rubensousa.dpadrecyclerview.sample.list.NestedListAdapter
import timber.log.Timber

class MainFragment : Fragment(R.layout.screen_tv_nested_lists) {

    private var _binding: ScreenTvNestedListsBinding? = null
    private val binding: ScreenTvNestedListsBinding get() = _binding!!
    private var selectedPosition = RecyclerView.NO_POSITION
    private val scrollStateHolder = DpadStateHolder()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = ScreenTvNestedListsBinding.bind(view)
        setupAdapter()
        setupAlignment(binding.recyclerView)
        binding.recyclerView.requestFocus()
        if (selectedPosition != RecyclerView.NO_POSITION) {
            binding.recyclerView.setSelectedPosition(
                selectedPosition,
                smooth = false,
                object : ViewHolderTask {
                    override fun run(viewHolder: RecyclerView.ViewHolder) {
                        Timber.d("Selection state restored")
                    }
                })
        }
    }

    private fun setupAdapter() {
        val nestedListAdapter = NestedListAdapter(scrollStateHolder,
            object : ItemViewHolder.ItemClickListener {
                override fun onViewHolderClicked() {
                    findNavController().navigate(R.id.open_detail)
                }
            })
        val list = ArrayList<ListModel>()
        for (i in 0 until 100) {
            list.add(generateList("List $i"))
        }
        nestedListAdapter.submitList(list)
        nestedListAdapter.stateRestorationPolicy =
            RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        binding.recyclerView.adapter = nestedListAdapter
    }

    private fun setupAlignment(recyclerView: DpadRecyclerView) {
        recyclerView.setParentAlignment(
            ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX,
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
                )
            )
        )
        recyclerView.addOnViewHolderSelectedListener(object :
            OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                selectedPosition = position
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