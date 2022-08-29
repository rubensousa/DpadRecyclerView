package com.rubensousa.dpadrecyclerview

import android.os.Parcel
import android.os.Parcelable

/**
 * Alignment configuration for aligning views in relation to its dimensions
 */
data class ChildAlignment(
    override val offset: Int = ViewAlignment.DEFAULT_OFFSET,
    override val offsetRatio: Float = ViewAlignment.DEFAULT_OFFSET_RATIO,
    override val isOffsetRatioEnabled: Boolean = true,
    override val includePadding: Boolean = false,
    override val alignToBaseline: Boolean = false
) : ViewAlignment, Parcelable {

    companion object CREATOR : Parcelable.Creator<ChildAlignment> {

        override fun createFromParcel(parcel: Parcel): ChildAlignment {
            return ChildAlignment(parcel)
        }

        override fun newArray(size: Int): Array<ChildAlignment?> {
            return arrayOfNulls(size)
        }
    }

    init {
        require(offsetRatio in 0f..1f) {
            "offsetRatio must be a value between 0f and 1f"
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(offset)
        parcel.writeFloat(offsetRatio)
        parcel.writeByte(if (isOffsetRatioEnabled) 1 else 0)
        parcel.writeByte(if (includePadding) 1 else 0)
        parcel.writeByte(if (alignToBaseline) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

}
