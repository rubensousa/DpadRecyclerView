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

package com.rubensousa.dpadrecyclerview.testfixtures

import androidx.recyclerview.widget.RecyclerView

class LayoutBlockRequest {
    /**
     * Coordinate from where to start the layout
     */
    var checkpoint: Int = 0

    /**
     * Position from the adapter to get the next view to be laid out
     */
    var position: Int = RecyclerView.NO_POSITION

    /**
     * Layout direction: 1 -> towards end, -1 -> towards start
     */
    var direction = 1
        private set

    /**
     * Adapter item direction: 1 -> towards end, -1 -> towards start
     */
    var itemDirection = 1
        private set

    /**
     * Total space to be laid out
     */
    var space: Int = 0

    fun isItemTowardsEnd() = itemDirection == 1

    fun setTowardsEnd() {
        direction = 1
    }

    fun setTowardsStart() {
        direction = -1
    }

    fun isTowardsEnd() = direction > 0

    fun isTowardsStart() = direction < 0

    fun reset() {
        setTowardsEnd()
        checkpoint = 0
        position = RecyclerView.NO_POSITION
        space = 0
    }

    override fun toString(): String {
        return "LayoutBlockRequest(checkpoint=$checkpoint, " +
                "position=$position, " +
                "direction=$direction, " +
                "space=$space)"
    }


}
