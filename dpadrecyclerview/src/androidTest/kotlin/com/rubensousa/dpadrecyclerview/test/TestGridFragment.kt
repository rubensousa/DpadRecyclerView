package com.rubensousa.dpadrecyclerview.test

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.OnViewHolderSelectedListener

open class TestGridFragment : Fragment(R.layout.test_container) {

    companion object {

        private const val ARG_LAYOUT_CONFIG = "layout_config"
        private const val ARG_ADAPTER_CONFIG = "adapter_config"

        fun getArgs(
            layoutConfig: TestLayoutConfiguration,
            adapterConfig: TestAdapterConfiguration = TestAdapterConfiguration()
        ): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(ARG_LAYOUT_CONFIG, layoutConfig)
            bundle.putParcelable(ARG_ADAPTER_CONFIG, adapterConfig)
            return bundle
        }
    }

    private val selectionEvents = ArrayList<TestSelectionEvent>()
    private val alignedEvents = ArrayList<TestSelectionEvent>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val layoutConfig = args.getParcelable<TestLayoutConfiguration>(ARG_LAYOUT_CONFIG)!!
        val adapterConfig = args.getParcelable<TestAdapterConfiguration>(ARG_ADAPTER_CONFIG)!!
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        recyclerView.addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                super.onViewHolderSelected(parent, child, position, subPosition)
                selectionEvents.add(TestSelectionEvent(position, subPosition))
            }

            override fun onViewHolderSelectedAndAligned(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                super.onViewHolderSelectedAndAligned(parent, child, position, subPosition)
                alignedEvents.add(TestSelectionEvent(position, subPosition))
            }
        })
        recyclerView.setGravity(layoutConfig.gravity)
        recyclerView.setSpanCount(layoutConfig.spans)
        recyclerView.setOrientation(layoutConfig.orientation)
        recyclerView.setParentAlignment(layoutConfig.parentAlignment)
        recyclerView.setChildAlignment(layoutConfig.childAlignment)
        recyclerView.adapter = createAdapter(recyclerView, adapterConfig)
        recyclerView.requestFocus()
    }


    open fun createAdapter(
        recyclerView: DpadRecyclerView,
        adapterConfig: TestAdapterConfiguration
    ): RecyclerView.Adapter<*> {
        val adapter = TestAdapter(adapterConfig.itemLayoutId, adapterConfig.alternateFocus)
        adapter.submitList(ArrayList<Int>().apply {
            repeat(adapterConfig.numberOfItems) {
                add(it)
            }
        })
        return adapter
    }

    fun getSelectionEvents(): List<TestSelectionEvent> {
        return selectionEvents
    }

    fun getSelectedAndPositionedEvents(): List<TestSelectionEvent> {
        return alignedEvents
    }

}
