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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

internal class PreLayoutRequest {

    var firstPosition: Int = RecyclerView.NO_POSITION
        private set

    var lastPosition: Int = RecyclerView.NO_POSITION
        private set

    var startOffset = Int.MAX_VALUE
        private set

    var endOffset = Int.MIN_VALUE
        private set

    var extraLayoutSpace: Int = 0
        private set

    var firstView: View? = null
        private set

    var lastView: View? = null
        private set

    fun updateOffsets(decoratedStart: Int, decoratedEnd: Int, remainingScroll: Int) {
        startOffset = min(startOffset, decoratedStart)
        endOffset = max(endOffset, decoratedEnd)
        if (startOffset != Int.MAX_VALUE && endOffset != Int.MIN_VALUE && endOffset > startOffset) {
            extraLayoutSpace = max(0, endOffset - startOffset + remainingScroll)
        }
    }

    fun reset(
        firstPos: Int,
        firstView: View,
        lastPos: Int,
        lastView: View?
    ) {
        clear()
        firstPosition = firstPos
        lastPosition = lastPos
        this.firstView = firstView
        this.lastView = lastView
    }

    fun clear() {
        extraLayoutSpace = 0
        startOffset = Int.MAX_VALUE
        endOffset = Int.MIN_VALUE
    }

    override fun toString(): String {
        return "PreLayoutRequest(firstPosition=$firstPosition, " +
                "lastPosition=$lastPosition, " +
                "startOffset=$startOffset, " +
                "endOffset=$endOffset, " +
                "extraLayoutSpace=$extraLayoutSpace)"
    }


}
