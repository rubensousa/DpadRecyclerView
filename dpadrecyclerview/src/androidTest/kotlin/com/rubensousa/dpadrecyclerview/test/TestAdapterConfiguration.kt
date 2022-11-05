package com.rubensousa.dpadrecyclerview.test

import android.os.Parcel
import android.os.Parcelable
import com.rubensousa.dpadrecyclerview.testing.R

data class TestAdapterConfiguration(
    val itemLayoutId: Int = R.layout.dpadrecyclerview_test_item_grid,
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

    companion object CREATOR : Parcelable.Creator<TestAdapterConfiguration> {
        override fun createFromParcel(parcel: Parcel): TestAdapterConfiguration {
            return TestAdapterConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<TestAdapterConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}