package com.rubensousa.dpadrecyclerview

import android.os.Parcel
import android.os.Parcelable
import android.view.View

/**
 * Alignment configuration for aligning views in relation to its dimensions
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
     * The distance to the [offsetRatio] of the view in pixels.
     *
     * E.g offsetRatio = 0.5f, offset = 100, View's height = 500
     *
     * Keyline position = 500 * 0.5f + 100 = 350
     */
    val offset: Int = 0,
    /**
     * The keyline position for the alignment. Default: 0.5f (center)
     *
     * Set [isOffsetRatioEnabled] to false in case you want to disable this
     */
    val offsetRatio: Float = 0.5f,
    /**
     * When enabled, [offsetRatio] will be used for the alignment.
     * Otherwise, only [offset] will be used.
     */
    val isOffsetRatioEnabled: Boolean = true,
    /**
     * True if padding should be included for the alignment.
     * Includes start/top padding if [offsetRatio] is 0.0.
     * Includes end/bottom padding if [offsetRatio] is 1.0.
     * If [offsetRatio] is not 0.0 or 1.0, padding isn't included
     */
    val includePadding: Boolean = false,
    /**
     *  When true, aligns to [View.getBaseline] for the view with id equal to [alignmentViewId]
     */
    val alignToBaseline: Boolean = false
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
        require(offsetRatio in 0f..1f) {
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
        parcel.writeFloat(offsetRatio)
        parcel.writeByte(if (isOffsetRatioEnabled) 1 else 0)
        parcel.writeByte(if (includePadding) 1 else 0)
        parcel.writeByte(if (alignToBaseline) 1 else 0)
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
