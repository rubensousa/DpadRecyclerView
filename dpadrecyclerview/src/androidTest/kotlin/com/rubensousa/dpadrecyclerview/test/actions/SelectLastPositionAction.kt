package com.rubensousa.dpadrecyclerview.test.actions

import androidx.test.espresso.UiController
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class SelectLastPositionAction(
    private val smooth: Boolean,
    private val onPositionSelected : (position: Int) -> Unit
) : DpadRecyclerViewAction("Selecting last position") {

    override fun perform(uiController: UiController, recyclerView: DpadRecyclerView) {
        val itemCount = recyclerView.adapter?.itemCount ?: return
        val lastPosition = itemCount - 1
        recyclerView.setSelectedPosition(lastPosition, smooth)
        onPositionSelected(lastPosition)
    }
}