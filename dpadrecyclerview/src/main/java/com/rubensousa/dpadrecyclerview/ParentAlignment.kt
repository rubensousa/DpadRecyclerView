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
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge.MAX
import com.rubensousa.dpadrecyclerview.ParentAlignment.Edge.MIN

/**
 * Alignment configuration for aligning views in relation to the RecyclerView bounds
 */
data class ParentAlignment(
    /**
     * The specific alignment to a given edge that overrides the keyline alignment. See [Edge]
     * Default: [Edge.MIN_MAX]
     */
    val edge: Edge = Edge.MIN_MAX,
    /**
     * The distance added to the [fraction] of the RecyclerView in pixels.
     *
     * E.g fraction = 0.5f, offset = 100, RecyclerView's height = 500
     *
     * Keyline position = 500 * 0.5f + 100 = 350
     */
    val offset: Int = ViewAlignment.DEFAULT_OFFSET,
    /**
     * The keyline position for the alignment. Default: 0.5f (center)
     *
     * Set [isFractionEnabled] to false in case you want to disable this
     */
    val fraction: Float = ViewAlignment.DEFAULT_FRACTION,
    /**
     * true if [fraction] should be used to position the item.
     *
     * If false, only [offset] will be used for the keyline position
     *
     * Default is true.
     */
    val isFractionEnabled: Boolean = true,
    /**
     * When [Edge.MAX] or [Edge.MIN] are used,
     * this flag decides if the Views should be aligned to the keyline
     * when there are few items, overriding the edge preference.
     *
     * Default is:
     * True for [Edge.MAX], which means we prefer aligning to the keyline
     * False for [Edge.MIN], which means we prefer aligning to the min edge.
     */
    val preferKeylineOverEdge: Boolean = edge == MAX,
) : Parcelable {

    companion object CREATOR : Parcelable.Creator<ParentAlignment> {

        override fun createFromParcel(parcel: Parcel): ParentAlignment {
            return ParentAlignment(parcel)
        }

        override fun newArray(size: Int): Array<ParentAlignment?> {
            return arrayOfNulls(size)
        }
    }

    init {
        require(fraction in 0f..1f) {
            "fraction must be a value between 0f and 1f"
        }
    }

    constructor(parcel: Parcel) : this(
        Edge.entries[parcel.readInt()],
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(edge.ordinal)
        parcel.writeInt(offset)
        parcel.writeFloat(fraction)
        parcel.writeByte(if (isFractionEnabled) 1 else 0)
        parcel.writeByte(if (preferKeylineOverEdge) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Overrides the keyline alignment and instead aligns to a certain edge.
     *
     * [MIN] and [MAX] are considered from the layout direction,
     * so in case reverse layout is enabled, [MIN] and [MAX] will point instead
     * to bottom/top for vertical layouts and left/right for horizontal layouts.
     */
    enum class Edge {
        /**
         * Items will always be aligned to the keyline position by default
         */
        NONE,

        /**
         * All items will be always aligned to the keyline position except the item at the start.
         * This will be the top for Vertical orientation, or start for the horizontal orientation
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
