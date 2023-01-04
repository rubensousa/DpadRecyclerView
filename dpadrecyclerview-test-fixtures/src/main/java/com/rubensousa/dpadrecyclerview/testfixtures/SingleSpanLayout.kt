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

import kotlin.math.abs
import kotlin.math.max

abstract class SingleSpanLayout(config: LayoutConfig) : LayoutMatrix(config) {

    abstract fun append(position: Int, endOffset: Int): ViewItem
    abstract fun prepend(position: Int, startOffset: Int): ViewItem

    override fun getLayoutStartOffset(): Int {
        val firstView = getFirstView() ?: return 0
        return if (isVertical()) {
            firstView.getDecoratedTop()
        } else {
            firstView.getDecoratedLeft()
        }
    }

    override fun getLayoutEndOffset(): Int {
        val lastView = getLastView() ?: return 0
        return if (isVertical()) {
            lastView.getDecoratedBottom()
        } else {
            lastView.getDecoratedRight()
        }
    }

    override fun scrollBy(offset: Int) {
        val scrollDistance = abs(offset)
        if (offset < 0) {
            val firstView = getFirstView() ?: return
            layoutRequest.apply {
                setTowardsStart()
                checkpoint = getDecoratedStart(firstView)
                val availableScrollSpace = max(0, -checkpoint)
                space = max(0, scrollDistance + getExtraLayoutSpaceStart() - availableScrollSpace)
                position = firstView.position - 1
            }
            fill(layoutRequest)
        } else {
            val lastView = getLastView() ?: return
            layoutRequest.apply {
                setTowardsEnd()
                checkpoint = getDecoratedEnd(lastView)
                val availableScrollSpace = max(0, checkpoint - getSize())
                space = max(0, scrollDistance + getExtraLayoutSpaceEnd() - availableScrollSpace)
                position = lastView.position + 1
            }
            fill(layoutRequest)
        }
        offsetChildren(-offset)
        if (offset < 0) {
            recycleEnd()
        } else {
            recycleStart()
        }
    }

    override fun layoutBlock(request: LayoutRequest): LayoutResult {
        val view = if (request.isTowardsEnd()) {
            append(request.position, request.checkpoint)
        } else {
            prepend(request.position, request.checkpoint)
        }
        request.position += request.direction
        return LayoutResult(
            views = listOf(view),
            consumedSpace = if (isVertical()) {
                view.getDecoratedHeight()
            } else {
                view.getDecoratedWidth()
            },
            skipConsumption = false
        )
    }

}
