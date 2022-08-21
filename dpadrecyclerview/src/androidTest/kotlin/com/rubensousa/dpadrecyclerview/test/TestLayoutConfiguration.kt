package com.rubensousa.dpadrecyclerview.test

import android.os.Parcel
import android.os.Parcelable
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment

data class TestLayoutConfiguration(
    val spans: Int,
    val orientation: Int,
    val parentAlignment: ParentAlignment,
    val childAlignment: ChildAlignment,
    val reverseLayout: Boolean = false,
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        requireNotNull(
            parcel.readParcelable(
                ParentAlignment::class.java.classLoader, ParentAlignment::class.java
            )
        ),
        requireNotNull(
            parcel.readParcelable(
                ChildAlignment::class.java.classLoader, ChildAlignment::class.java
            )
        ),
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

    companion object CREATOR : Parcelable.Creator<TestLayoutConfiguration> {
        override fun createFromParcel(parcel: Parcel): TestLayoutConfiguration {
            return TestLayoutConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<TestLayoutConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}