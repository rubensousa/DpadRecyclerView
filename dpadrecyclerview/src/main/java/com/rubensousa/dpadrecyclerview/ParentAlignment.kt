package com.rubensousa.dpadrecyclerview

import android.os.Parcel
import android.os.Parcelable
import android.view.Gravity

/**
 * Alignment configuration for aligning views in relation to the RecyclerView bounds
 */
// TODO: Add more comments
data class ParentAlignment(
    val edge: Edge,
    val offset: Int = 0,
    val offsetPercent: Float = 50f,
    /**
     * true if [offsetPercent] should be used to position the item. Default is true
     */
    val offsetPercentEnabled: Boolean = true,
    /**
     * When true, if there are very few items between min edge and keyline,
     * align the items to keyline instead of aligning them to the min edge
     */
    val preferKeylineOverMinEdge: Boolean = false,
    /**
     * When true, if there are very few items between max edge and keyline,
     * align the items to keyline instead of aligning them to the max edge
     */
    val preferKeylineOverMaxEdge: Boolean = true
) : Parcelable {

    companion object CREATOR : Parcelable.Creator<ParentAlignment> {
        override fun createFromParcel(parcel: Parcel): ParentAlignment {
            return ParentAlignment(parcel)
        }

        override fun newArray(size: Int): Array<ParentAlignment?> {
            return arrayOfNulls(size)
        }
    }

    constructor(parcel: Parcel) : this(
        Edge.values()[parcel.readInt()],
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    init {
        require(offsetPercent in 0f..100.0f) {
            "offsetPercent must be a value between 0f and 100f"
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(edge.ordinal)
        parcel.writeInt(offset)
        parcel.writeFloat(offsetPercent)
        parcel.writeByte(if (offsetPercentEnabled) 1 else 0)
        parcel.writeByte(if (preferKeylineOverMinEdge) 1 else 0)
        parcel.writeByte(if (preferKeylineOverMaxEdge) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    enum class Edge {
        /**
         * Items will always be aligned to the keyline position by default
         */
        NONE,

        /**
         * All items will be always aligned to the keyline position except the item at the start.
         * This will be the top of Vertical orientation, or start for the horizontal orientation
         */
        MIN,

        /**
         * All items will be always aligned to the keyline position except the item at the end.
         * This will be the bottom for Vertical orientation, or end for Horizontal orientation
         */
        MAX,

        /**
         * All items will be always aligned to the keyline position except the items
         * at the start and end edges
         */
        MIN_MAX
    }

}
