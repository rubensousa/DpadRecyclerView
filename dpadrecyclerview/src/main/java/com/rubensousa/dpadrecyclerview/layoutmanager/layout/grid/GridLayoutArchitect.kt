package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutArchitect
import kotlin.math.abs
import kotlin.math.max

internal class GridLayoutArchitect(layoutInfo: LayoutInfo) : LinearLayoutArchitect(layoutInfo) {

    override fun updateLayoutStateForScroll(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State,
        offset: Int
    ) {
        layoutRequest.setRecyclingEnabled(true)

        val scrollDistance = abs(offset)

        if (offset < 0) {
            val view = layoutInfo.getChildClosestToStart() ?: return
            layoutRequest.prepend(layoutInfo.getLayoutPositionOf(view)) {
                if (defaultItemDirection == LayoutRequest.ItemDirection.TAIL) {
                    setCurrentItemDirectionStart()
                } else {
                    setCurrentItemDirectionEnd()
                }
                setCheckpoint(layoutInfo.getDecoratedStart(view))
                updateExtraLayoutSpace(layoutRequest, state)
                setAvailableScrollSpace(max(0, layoutInfo.getStartAfterPadding() - checkpoint))
                setFillSpace(scrollDistance + extraLayoutSpaceStart - availableScrollSpace)
            }
        } else {
            val view = layoutInfo.getChildClosestToEnd() ?: return
            layoutRequest.append(layoutInfo.getLayoutPositionOf(view)) {
                if (defaultItemDirection == LayoutRequest.ItemDirection.TAIL) {
                    setCurrentItemDirectionEnd()
                } else {
                    setCurrentItemDirectionStart()
                }
                setCheckpoint(layoutInfo.getDecoratedEnd(view))
                updateExtraLayoutSpace(layoutRequest, state)
                setAvailableScrollSpace(max(0, checkpoint - layoutInfo.getEndAfterPadding()))
                setFillSpace(scrollDistance + extraLayoutSpaceEnd - availableScrollSpace)
            }
        }
    }

}
