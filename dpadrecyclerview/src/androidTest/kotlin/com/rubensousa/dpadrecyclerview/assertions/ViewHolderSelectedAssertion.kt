package com.rubensousa.dpadrecyclerview.assertions

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.TestAdapter

class ViewHolderSelectedAssertion(
    private val position: Int,
    private val expected: Boolean = true
) : ViewAssertion {

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (view !is DpadRecyclerView) {
            return
        }
        val viewHolder = view.findViewHolderForAdapterPosition(position)
        val isSelected = (viewHolder as DpadViewHolder).isViewHolderSelected()
        if (expected) {
            assertThat(isSelected).isTrue()
        } else {
            assertThat(isSelected).isFalse()
        }
    }
}