package com.rubensousa.dpadrecyclerview.test

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder

abstract class TestViewHolder(view: View) : RecyclerView.ViewHolder(view), DpadViewHolder {

    private var isSelected = false

    override fun onViewHolderSelected() {
        super.onViewHolderSelected()
        isSelected = true
    }

    override fun onViewHolderDeselected() {
        super.onViewHolderDeselected()
        isSelected = false
    }

    fun isViewHolderSelected(): Boolean = isSelected

}
