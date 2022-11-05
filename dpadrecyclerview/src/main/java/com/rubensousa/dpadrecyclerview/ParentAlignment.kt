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
 * Alignment configuration for aligning views in relation to the RecyclerView bounds
 */
data class ParentAlignment(
    /**
     * The specific alignment to a given edge that overrides the keyline alignment. See [Edge]
     * Default: [Edge.MIN_MAX]
     */
    val edge: Edge = DEFAULT_EDGE,
    /**
     * The distance to the [offsetRatio] of the RecyclerView in pixels.
     *
     * E.g offsetRatio = 0.5f, offset = 100, RecyclerView's height = 500
     *
     * Keyline position = 500 * 0.5f + 100 = 350
     */
    val offset: Int = DEFAULT_OFFSET,
    /**
     * The keyline position for the alignment. Default: 0.5f (center)
     *
     * Set [isOffsetRatioEnabled] to false in case you want to disable this
     */
    val offsetRatio: Float = DEFAULT_OFFSET_RATIO,
    /**
     * true if [offsetRatio] should be used to position the item.
     *
     * If false, only [offset] will be used for the keyline position
     *
     * Default is true.
     */
    val isOffsetRatioEnabled: Boolean = true
) : Parcelable {

    companion object CREATOR : Parcelable.Creator<ParentAlignment> {

        val DEFAULT_EDGE = Edge.MIN_MAX
        const val DEFAULT_OFFSET = 0
        const val DEFAULT_OFFSET_RATIO = 0.5f

        override fun createFromParcel(parcel: Parcel): ParentAlignment {
            return ParentAlignment(parcel)
        }

        override fun newArray(size: Int): Array<ParentAlignment?> {
            return arrayOfNulls(size)
        }
    }

    init {
        require(offsetRatio in 0f..1f) {
            "offsetStartRatio must be a value between 0f and 1f"
        }
    }

    constructor(parcel: Parcel) : this(
        Edge.values()[parcel.readInt()],
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(edge.ordinal)
        parcel.writeInt(offset)
        parcel.writeFloat(offsetRatio)
        parcel.writeByte(if (isOffsetRatioEnabled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Overrides the keyline alignment and instead aligns to a certain edge
     */
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
