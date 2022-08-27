package com.rubensousa.dpadrecyclerview.test

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class TestPaginationFragment : TestGridFragment() {

    private val concatAdapter = ConcatAdapter(
        ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()
    )
    private val viewModel by viewModels<PaginationViewModel>()

    override fun createAdapter(
        recyclerView: DpadRecyclerView,
        adapterConfig: TestAdapterConfiguration
    ): RecyclerView.Adapter<*> {
        val testAdapter = TestAdapter(adapterConfig.itemLayoutId, alternateFocus = false)
        val loadingAdapter = PlaceholderAdapter()
        viewModel.initialLoad(adapterConfig.numberOfItems)
        viewModel.listState.observe(viewLifecycleOwner) { list ->
            testAdapter.submitList(list)
        }
        viewModel.loadingState.observe(viewLifecycleOwner) { isLoading ->
            loadingAdapter.show(isLoading)
        }
        concatAdapter.addAdapter(testAdapter)
        return concatAdapter
    }

    override fun onViewHolderSelected(
        parent: RecyclerView,
        child: RecyclerView.ViewHolder?,
        position: Int,
        subPosition: Int
    ) {
        super.onViewHolderSelected(parent, child, position, subPosition)
        viewModel.loadMore(position)
    }

    class PlaceholderAdapter : RecyclerView.Adapter<PlaceholderAdapter.VH>() {

        private var show = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            return VH(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.test_placeholder, parent, false)
            )
        }

        override fun onBindViewHolder(holder: VH, position: Int) {

        }

        fun show(enabled: Boolean) {
            show = enabled
            notifyItemChanged(0)
        }

        override fun getItemCount(): Int {
            return if (show) {
                1
            } else {
                0
            }
        }

        class VH(view: View) : RecyclerView.ViewHolder(view)

    }


}