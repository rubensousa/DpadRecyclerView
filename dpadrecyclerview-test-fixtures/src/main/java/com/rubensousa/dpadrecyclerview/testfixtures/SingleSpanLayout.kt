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

    protected abstract fun append(position: Int, endOffset: Int): ViewItem
    protected abstract fun prepend(position: Int, startOffset: Int): ViewItem

    override fun initializeLayout(pivotPosition: Int) {
        // Start by laying out the the pivot since all other items are laid out around it
        val view = layoutPivot(pivotPosition)

        // Layout from the pivot until the start limit of the matrix
        layoutFromPivotToStart(view)

        // Layout from the pivot until the end limit of the matrix
        layoutFromPivotToEnd(view)
    }

    private fun layoutPivot(pivotPosition: Int): ViewItem {
        layoutRequest.append(pivotPosition) {
            position = pivotPosition
            checkpoint = (config.parentKeyline - config.childKeyline * getChildSize()).toInt()
            space = 1
        }
        val view = layoutBlock(layoutRequest).views.first()
        append(view)

        // Align the pivot using the alignment configuration
        val scrollOffset = getViewCenter(view) - config.parentKeyline
        offsetChildren(-scrollOffset)

        return view
    }

    private fun layoutFromPivotToStart(pivotView: ViewItem) {
        layoutRequest.prepend(pivotView.position) {
            checkpoint = getDecoratedStart(pivotView)
            space = max(0, checkpoint + getExtraLayoutSpaceStart())
        }
        fill(layoutRequest)
    }

    private fun layoutFromPivotToEnd(pivotView: ViewItem) {
        layoutRequest.append(pivotView.position) {
            checkpoint = getDecoratedEnd(pivotView)
            space = max(0, getVisibleSpace() - checkpoint + getExtraLayoutSpaceEnd())
        }
        fill(layoutRequest)
    }

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

    override fun layoutExtraStart() {
        val firstView = getFirstView() ?: return
        layoutRequest.prepend(firstView.position) {
            checkpoint = getLayoutStartOffset()
            space = max(0, checkpoint + getExtraLayoutSpaceStart())
        }
        fill(layoutRequest)
    }

    override fun layoutExtraEnd() {
        val lastView = getLastView() ?: return
        layoutRequest.append(lastView.position) {
            checkpoint = getLayoutEndOffset()
            space = max(0, getVisibleSpace() - checkpoint + getExtraLayoutSpaceEnd())
        }
        fill(layoutRequest)
    }

    override fun scrollBy(offset: Int) {
        val scrollDistance = abs(offset)
        if (offset < 0) {
            val firstView = getFirstView() ?: return
            layoutRequest.prepend(firstView.position) {
                checkpoint = getDecoratedStart(firstView)
                val availableScrollSpace = max(0, -checkpoint)
                space = max(0, scrollDistance + getExtraLayoutSpaceStart() - availableScrollSpace)
            }
            fill(layoutRequest)
        } else {
            val lastView = getLastView() ?: return
            layoutRequest.append(lastView.position) {
                checkpoint = getDecoratedEnd(lastView)
                val availableScrollSpace = max(0, checkpoint - getSize())
                space = max(0, scrollDistance + getExtraLayoutSpaceEnd() - availableScrollSpace)
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

    override fun layoutBlock(request: LayoutBlockRequest): LayoutBlockResult {
        val view = if (request.isTowardsEnd()) {
            append(request.position, request.checkpoint)
        } else {
            prepend(request.position, request.checkpoint)
        }
        request.position += request.currentItemDirection
        return LayoutBlockResult(
            views = listOf(view),
            consumedSpace = if (isVertical()) {
                view.getDecoratedHeight()
            } else {
                view.getDecoratedWidth()
            },
            skipConsumption = false
        )
    }

    private fun getChildSize(): Int {
        return if (isVertical()) {
            config.viewHeight
        } else {
            config.viewWidth
        }
    }

}
