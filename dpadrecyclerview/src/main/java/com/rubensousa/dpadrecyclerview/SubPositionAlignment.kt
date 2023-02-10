/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview

import android.os.Parcel
import android.os.Parcelable
import android.view.View

/**
 * Alignment configuration specific to a certain sub position.
 *
 * This will override the default alignment set from [ChildAlignment].
 */
data class SubPositionAlignment(
    override val offset: Int = ViewAlignment.DEFAULT_OFFSET,
    override val offsetRatio: Float = ViewAlignment.DEFAULT_OFFSET_RATIO,
    override val isOffsetRatioEnabled: Boolean = true,
    override val includePadding: Boolean = false,
    override val alignToBaseline: Boolean = false,
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
) : ViewAlignment, Parcelable {

    companion object CREATOR : Parcelable.Creator<SubPositionAlignment> {
        override fun createFromParcel(parcel: Parcel): SubPositionAlignment {
            return SubPositionAlignment(parcel)
        }

        override fun newArray(size: Int): Array<SubPositionAlignment?> {
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
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(offset)
        parcel.writeFloat(offsetRatio)
        parcel.writeByte(if (isOffsetRatioEnabled) 1 else 0)
        parcel.writeByte(if (includePadding) 1 else 0)
        parcel.writeByte(if (alignToBaseline) 1 else 0)
        parcel.writeInt(alignmentViewId)
        parcel.writeInt(focusViewId)
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
