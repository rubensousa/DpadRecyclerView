package com.rubensousa.dpadrecyclerview.test.assertions

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView

object DpadRecyclerViewAssertions {

    @JvmStatic
    fun isFocused(position: Int): ViewAssertion {
        return FocusAssertion(position)
    }

    @JvmStatic
    fun isSelected(position: Int, subPosition: Int = 0): ViewAssertion {
        return SelectionAssertion(position, subPosition)
    }

    private class SelectionAssertion(
        private val position: Int,
        private val subPosition: Int = 0
    ) : DpadRvAssertion() {

        override fun check(view: DpadRecyclerView) {
            assertThat(view.getSelectedPosition()).isEqualTo(position)
            assertThat(view.getSelectedSubPosition()).isEqualTo(subPosition)
        }
    }


    private class FocusAssertion(private val focusedPosition: Int) : DpadRvAssertion() {

        override fun check(view: DpadRecyclerView) {
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

}
