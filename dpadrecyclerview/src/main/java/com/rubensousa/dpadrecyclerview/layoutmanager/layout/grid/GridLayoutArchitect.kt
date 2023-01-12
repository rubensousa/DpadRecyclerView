package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutArchitect
import kotlin.math.abs
import kotlin.math.max

internal class GridLayoutArchitect(
    layoutInfo: LayoutInfo,
    private val startRow: GridRow,
    private val endRow: GridRow
) : LinearLayoutArchitect(layoutInfo) {

    override fun updateLayoutStateForScroll(
        layoutRequest: LayoutRequest,
        state: RecyclerView.State,
        offset: Int
    ) {
        updateExtraLayoutSpace(layoutRequest, state)
        layoutRequest.setRecyclingEnabled(true)

        val scrollDistance = abs(offset)

        if (offset < 0) {
            val checkpoint = if (startRow.isStartComplete()) {
                startRow.startOffset
            } else {
                startRow.endOffset
            }
            layoutRequest.prepend(startRow.getFirstPosition()) {
                setCheckpoint(checkpoint)
                setAvailableScrollSpace(max(0, layoutInfo.getStartAfterPadding() - checkpoint))
                setFillSpace(scrollDistance + extraLayoutSpaceStart - availableScrollSpace)
            }
        } else {
            val checkpoint = if (endRow.isEndComplete()) {
                endRow.endOffset
            } else {
                endRow.startOffset
            }
            layoutRequest.append(endRow.getLastPosition()) {
                setCheckpoint(checkpoint)
                setAvailableScrollSpace(max(0, checkpoint - layoutInfo.getEndAfterPadding()))
                setFillSpace(scrollDistance + extraLayoutSpaceEnd - availableScrollSpace)
            }
        }
    }

    override fun getLayoutStart(): Int = startRow.startOffset

    override fun getLayoutEnd(): Int = endRow.endOffset

}
