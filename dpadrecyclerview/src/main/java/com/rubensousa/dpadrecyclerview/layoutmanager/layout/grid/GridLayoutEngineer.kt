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

package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.LayoutAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutArchitect
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.OnChildLayoutListener
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.StructureEngineer
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewBounds
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.ViewRecycler
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutArchitect

// TODO Add a second pass to adjust view heights based on row size
internal class GridLayoutEngineer(
    layoutManager: LayoutManager,
    layoutInfo: LayoutInfo,
    layoutAlignment: LayoutAlignment,
    onChildLayoutListener: OnChildLayoutListener
) : StructureEngineer(layoutManager, layoutInfo, layoutAlignment, onChildLayoutListener) {

    companion object {
        const val TAG = "GridArchitect"
    }

    private val architect = LinearLayoutArchitect(layoutInfo)
    private val recycler = GridRecycler(layoutManager, layoutInfo)
    private val grid = Grid(numberOfSpans = layoutInfo.getSpanCount())

    override fun getArchitect(): LayoutArchitect = architect

    override fun getViewRecycler(): ViewRecycler = recycler

    override fun init(
        layoutRequest: LayoutRequest,
        recyclerViewState: RecyclerView.State
    ) {
        grid.init(
            width = layoutInfo.getSecondaryTotalSpace(),
            start = layoutInfo.getSecondaryStartAfterPadding(),
            end = layoutInfo.getSecondaryEndAfterPadding()
        )
    }

    override fun offsetChildren(offset: Int, layoutRequest: LayoutRequest) {
        super.offsetChildren(offset, layoutRequest)
        grid.offsetBy(offset)
    }

    override fun placePivot(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    ) {
        val size = layoutInfo.getMeasuredSize(view)
        val viewCenter = layoutAlignment.calculateViewCenterForLayout(view)
        val head = viewCenter - size / 2 - layoutInfo.getStartDecorationSize(view)
        val tail = viewCenter + size / 2 + layoutInfo.getEndDecorationSize(view)
        val decoratedSize = tail - head
        val spanSize = layoutInfo.getSpanSize(position)

        grid.placePivot(
            newTop = head,
            viewSize = decoratedSize,
            spanIndex = layoutInfo.getStartColumnIndex(position),
            spanSize = spanSize
        )

        if (layoutInfo.isVertical()) {
            bounds.top = head
            bounds.bottom = tail
            bounds.left = grid.getTopRowStartOffset()
            bounds.right = grid.getTopRowEndOffset()
        } else {
            bounds.left = head
            bounds.right = tail
            bounds.top = grid.getTopRowStartOffset()
            bounds.bottom = grid.getTopRowEndOffset()
        }

        layoutRequest.updateWindow(head, head)
    }

    override fun appendView(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            grid.appendHorizontally(
                decoratedSize, layoutInfo.getSpanSize(position), bounds, layoutRequest.checkpoint
            )
        } else {
            // TODO
            0
        }
        layoutRequest.appendWindow(consumedSpace)
        return consumedSpace
    }

    override fun prependView(
        view: View,
        position: Int,
        bounds: ViewBounds,
        layoutRequest: LayoutRequest
    ): Int {
        val decoratedSize = layoutInfo.getDecoratedSize(view)
        val consumedSpace = if (layoutInfo.isVertical()) {
            grid.prependHorizontally(
                decoratedSize, layoutInfo.getSpanSize(position), bounds, layoutRequest.checkpoint
            )
        } else {
            // TODO
            0
        }
        layoutRequest.prependWindow(consumedSpace)
        return consumedSpace
    }

}
