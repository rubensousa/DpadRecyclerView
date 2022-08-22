package com.rubensousa.dpadrecyclerview

import android.os.Parcel
import android.os.Parcelable
import android.view.View

/**
 * Alignment configuration for aligning views in relation
 * to other sibling views inside the same RecyclerView
 */
data class ChildAlignment(
    /**
     * The id of the child view that should be used for the alignment.
     * If it is [View.NO_ID], then the root view will be used instead.
     * This is not necessarily the same view that will receive focus.
     */
    val alignmentViewId: Int = View.NO_ID,
    /**
     * The id of the child view that will receive focus during alignment.
     * Otherwise, [alignmentViewId] will be the one to receive focus.
     */
    private val focusViewId: Int = View.NO_ID,
    /**
     * Returns number of pixels to the end of the start edge.
     * In both LTR or vertical cases, it's the offset added to left/top edge.
     * In the RTL case, it's the offset subtracted from right edge.
     */
    val offset: Int = 0,
    /**
     * Sets the offset percent for item alignment in addition to the [offset].
     * Example: 40f means 40% of the width/height from the start edge.
     * In RTL it's 40% of the width from the right edge
     * Set [isOffsetRatioEnabled] to false in case you want to disable this
     */
    val offsetStartRatio: Float = 0.5f,
    /**
     * True if padding should be included for the alignment.
     * Includes start/top padding if [offsetStartRatio] is 0.0.
     * Includes end/bottom padding if [offsetStartRatio] is 1.0.
     * If [offsetStartRatio] is not 0.0 or 1.0, padding isn't included
     */
    val includePadding: Boolean = false,
    /**
     *  When true, aligns to [View.getBaseline] for the view of with id equals [alignmentViewId]
     */
    val alignToBaseline: Boolean = false,
    /**
     * When enabled, [offsetStartRatio] will be used for the item alignment
     */
    val isOffsetRatioEnabled: Boolean = true
) : Parcelable {

    companion object CREATOR : Parcelable.Creator<ChildAlignment> {
        override fun createFromParcel(parcel: Parcel): ChildAlignment {
            return ChildAlignment(parcel)
        }

        override fun newArray(size: Int): Array<ChildAlignment?> {
            return arrayOfNulls(size)
        }
    }

    init {
        require(offsetStartRatio in 0f..1f) {
            "offsetStartRatio must be a value between 0f and 100f"
        }
    }

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(alignmentViewId)
        parcel.writeInt(focusViewId)
        parcel.writeInt(offset)
        parcel.writeFloat(offsetStartRatio)
        parcel.writeByte(if (includePadding) 1 else 0)
        parcel.writeByte(if (alignToBaseline) 1 else 0)
        parcel.writeByte(if (isOffsetRatioEnabled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getFocusViewId(): Int {
        if (focusViewId != View.NO_ID) {
            return focusViewId
        }
        return alignmentViewId
    }

}
