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

/**
 * Alignment configuration for aligning views in relation to its dimensions
 */
data class ChildAlignment(
    override val offset: Int = ViewAlignment.DEFAULT_OFFSET,
    override val fraction: Float = ViewAlignment.DEFAULT_FRACTION,
    override val isFractionEnabled: Boolean = true,
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
        require(fraction in 0f..1f) {
            "fraction must be a value between 0f and 1f"
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
        parcel.writeFloat(fraction)
        parcel.writeByte(if (isFractionEnabled) 1 else 0)
        parcel.writeByte(if (includePadding) 1 else 0)
        parcel.writeByte(if (alignToBaseline) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

}
