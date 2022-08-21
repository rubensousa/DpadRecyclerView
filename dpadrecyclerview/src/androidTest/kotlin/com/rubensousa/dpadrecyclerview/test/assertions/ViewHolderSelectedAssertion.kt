package com.rubensousa.dpadrecyclerview.test.assertions

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.test.TestViewHolder

class ViewHolderSelectedAssertion(
    private val position: Int,
    private val expected: Boolean = true
) : ViewAssertion {

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (view !is DpadRecyclerView) {
            return
        }
        val viewHolder = view.findViewHolderForAdapterPosition(position)
        val isSelected = (viewHolder as TestViewHolder).isViewHolderSelected()
        if (expected) {
            assertThat(isSelected).isTrue()
        } else {
            assertThat(isSelected).isFalse()
        }
    }
}