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

package com.rubensousa.dpadrecyclerview.internal.layoutmanager

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

internal class ViewSelector {

    var position: Int = 0
        private set

    var subPosition: Int = 0
        private set

    // TODO
    fun onItemsAdded(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {

    }

    // TODO
    fun onItemsChanged(recyclerView: RecyclerView) {

    }

    // TODO
    fun onItemsRemoved(recyclerView: RecyclerView, positionStart: Int, itemCount: Int) {

    }

    // TODO
    fun onItemsMoved(recyclerView: RecyclerView, from: Int, to: Int, itemCount: Int) {

    }

    // TODO
    fun onAdapterChanged(
        oldAdapter: RecyclerView.Adapter<*>?,
        newAdapter: RecyclerView.Adapter<*>?
    ) {

    }

    fun update(position: Int, subPosition: Int) {
        this.position = position
        this.subPosition = subPosition
        Log.i(DpadRecyclerView.TAG, "Selection state update: $position, $subPosition")
    }

    fun onSaveInstanceState(): Parcelable {
        return SavedState(position)
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            position = state.selectedPosition
        }
    }

    data class SavedState(val selectedPosition: Int) : Parcelable {

        companion object CREATOR : Parcelable.Creator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }

        constructor(parcel: Parcel) : this(parcel.readInt())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(selectedPosition)
        }

        override fun describeContents(): Int {
            return 0
        }
    }

}