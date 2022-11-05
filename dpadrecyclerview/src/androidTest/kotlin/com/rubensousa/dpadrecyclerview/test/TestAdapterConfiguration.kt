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

package com.rubensousa.dpadrecyclerview.test

import android.os.Parcel
import android.os.Parcelable
import com.rubensousa.dpadrecyclerview.testing.R

data class TestAdapterConfiguration(
    val itemLayoutId: Int = R.layout.dpadrecyclerview_test_item_grid,
    val numberOfItems: Int = 200,
    val alternateFocus: Boolean = false
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(itemLayoutId)
        parcel.writeInt(numberOfItems)
        parcel.writeByte(if (alternateFocus) 1 else 0)
    }

    companion object CREATOR : Parcelable.Creator<TestAdapterConfiguration> {
        override fun createFromParcel(parcel: Parcel): TestAdapterConfiguration {
            return TestAdapterConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<TestAdapterConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}