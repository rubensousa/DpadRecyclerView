package com.rubensousa.dpadrecyclerview.layoutmanager.layout.grid

import android.view.View
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutInfo
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutRequest
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.linear.LinearLayoutArchitect
import kotlin.math.max

internal class GridLayoutArchitect(
    layoutInfo: LayoutInfo,
    private val startRow: GridRow,
    private val endRow: GridRow
) : LinearLayoutArchitect(layoutInfo) {

    override fun updateLayoutStateBeforePivot(
        layoutRequest: LayoutRequest,
        pivotView: View,
        pivotPosition: Int
    ) {
        layoutRequest.apply {
            setStartDirection()
            setCurrentPosition(pivotPosition - 1)
            setCheckpoint(getLayoutStart())
            setAvailableScrollSpace(0)
            val startFillSpace = max(
                0, checkpoint - layoutInfo.getStartAfterPadding()
            )
            setFillSpace(startFillSpace + layoutRequest.extraLayoutSpaceStart)
        }
    }

    override fun updateLayoutStateAfterPivot(
        layoutRequest: LayoutRequest,
        pivotView: View,
        pivotPosition: Int
    ) {
        layoutRequest.apply {
            setEndDirection()
            setCurrentPosition(pivotPosition + 1)
            setCheckpoint(getLayoutEnd())
            setAvailableScrollSpace(0)
            val endFillSpace = max(
                0, layoutInfo.getEndAfterPadding() - checkpoint
            )
            setFillSpace(endFillSpace + layoutRequest.extraLayoutSpaceEnd)
        }
    }


    override fun getLayoutStart(): Int {
        return startRow.startOffset
    }

    override fun getLayoutEnd(): Int {
        return endRow.startOffset
    }

}
