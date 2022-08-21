package com.rubensousa.dpadrecyclerview.test.assertions

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

class SelectionAssertion(
    private val position: Int,
    private val subPosition: Int
) : ViewAssertion {

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (view !is DpadRecyclerView) {
            return
        }
        assertThat(view.getSelectedPosition()).isEqualTo(position)
        assertThat(view.getSelectedSubPosition()).isEqualTo(subPosition)

    }
}