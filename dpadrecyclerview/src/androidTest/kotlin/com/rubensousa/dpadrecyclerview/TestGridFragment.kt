package com.rubensousa.dpadrecyclerview

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.test.R

class TestGridFragment : Fragment(R.layout.test_container) {

    companion object {

        private const val ARG_LAYOUT_CONFIG = "layout_config"
        private const val ARG_ADAPTER_CONFIG = "adapter_config"

        fun getArgs(
            layoutConfig: LayoutConfiguration,
            adapterConfig: AdapterConfiguration = AdapterConfiguration()
        ): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(ARG_LAYOUT_CONFIG, layoutConfig)
            bundle.putParcelable(ARG_ADAPTER_CONFIG, adapterConfig)
            return bundle
        }
    }

    private val selectionEvents = ArrayList<SelectionEvent>()
    private val alignedEvents = ArrayList<SelectionEvent>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = requireArguments()
        val layoutConfig = args.getParcelable<LayoutConfiguration>(ARG_LAYOUT_CONFIG)!!
        val adapterConfig = args.getParcelable<AdapterConfiguration>(ARG_ADAPTER_CONFIG)!!
        val recyclerView = view.findViewById<DpadRecyclerView>(R.id.recyclerView)
        val adapter = TestAdapter(adapterConfig.itemLayoutId, adapterConfig.alternateFocus)
        adapter.submitList(ArrayList<Int>().apply {
            repeat(adapterConfig.numberOfItems) {
                add(it)
            }
        })
        recyclerView.addOnViewHolderSelectedListener(object : OnViewHolderSelectedListener {
            override fun onViewHolderSelected(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                super.onViewHolderSelected(parent, child, position, subPosition)
                selectionEvents.add(SelectionEvent(position, subPosition))
            }

            override fun onViewHolderSelectedAndAligned(
                parent: RecyclerView,
                child: RecyclerView.ViewHolder?,
                position: Int,
                subPosition: Int
            ) {
                super.onViewHolderSelectedAndAligned(parent, child, position, subPosition)
                alignedEvents.add(SelectionEvent(position, subPosition))
            }
        })
        recyclerView.setSpanCount(layoutConfig.spans)
        recyclerView.setOrientation(layoutConfig.orientation)
        recyclerView.adapter = adapter
        recyclerView.setParentAlignment(layoutConfig.parentAlignment)
        recyclerView.setChildAlignment(layoutConfig.childAlignment)
        recyclerView.requestFocus()
    }

    fun getSelectionEvents(): List<SelectionEvent> {
        return selectionEvents
    }

    fun getSelectedAndPositionedEvents(): List<SelectionEvent> {
        return alignedEvents
    }

    data class SelectionEvent(val position: Int, val subPosition: Int)

    data class AdapterConfiguration(
        val itemLayoutId: Int = R.layout.test_item_grid,
        val numberOfItems: Int = 200,
        val alternateFocus: Boolean = false
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte()
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(itemLayoutId)
            parcel.writeInt(numberOfItems)
            parcel.writeByte(if (alternateFocus) 1 else 0)
        }

        companion object CREATOR : Parcelable.Creator<AdapterConfiguration> {
            override fun createFromParcel(parcel: Parcel): AdapterConfiguration {
                return AdapterConfiguration(parcel)
            }

            override fun newArray(size: Int): Array<AdapterConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class LayoutConfiguration(
        val spans: Int,
        val orientation: Int,
        val parentAlignment: ParentAlignment,
        val childAlignment: ChildAlignment,
        val reverseLayout: Boolean = false,
    ) : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readParcelable(ParentAlignment::class.java.classLoader)!!,
            parcel.readParcelable(ChildAlignment::class.java.classLoader)!!,
            parcel.readByte() != 0.toByte()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(spans)
            parcel.writeInt(orientation)
            parcel.writeParcelable(parentAlignment, flags)
            parcel.writeParcelable(childAlignment, flags)
            parcel.writeByte(if (reverseLayout) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<LayoutConfiguration> {
            override fun createFromParcel(parcel: Parcel): LayoutConfiguration {
                return LayoutConfiguration(parcel)
            }

            override fun newArray(size: Int): Array<LayoutConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }

}
