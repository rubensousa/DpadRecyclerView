package com.rubensousa.dpadrecyclerview.test.assertions

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

abstract class DpadRvAssertion : ViewAssertion {

    override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
        if (noViewFoundException != null) {
            throw noViewFoundException
        }
        assertThat(view).isInstanceOf(DpadRecyclerView::class.java)
        check(view as DpadRecyclerView)
    }

    abstract fun check(view: DpadRecyclerView)

}
