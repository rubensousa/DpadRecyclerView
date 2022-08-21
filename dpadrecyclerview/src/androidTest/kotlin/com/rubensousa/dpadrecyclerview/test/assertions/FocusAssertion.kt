package com.rubensousa.dpadrecyclerview.test.assertions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class FocusAssertion(private val focusedPosition: Int) : ViewAssertion {

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (view !is DpadRecyclerView) {
            return
        }
        val focusedView = view.findFocus()
        if (focusedPosition == RecyclerView.NO_POSITION) {
            if (focusedView !== view) {
                assertThat(focusedView).isNull()
            }
        } else {
            assertThat(view.findContainingViewHolder(focusedView)!!.absoluteAdapterPosition)
                .isEqualTo(focusedPosition)
        }
    }
}