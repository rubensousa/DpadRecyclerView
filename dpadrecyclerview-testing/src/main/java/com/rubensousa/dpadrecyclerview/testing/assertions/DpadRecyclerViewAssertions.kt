/*
 * Copyright 2022 Rúben Sousa
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rubensousa.dpadrecyclerview.testing.assertions

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.util.HumanReadables
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import junit.framework.AssertionFailedError

object DpadRecyclerViewAssertions {

    @JvmStatic
    fun isNotFocused(): ViewAssertion {
        return isFocused(position = RecyclerView.NO_POSITION)
    }

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
                if (focusedView == null) {
                    throw AssertionFailedError(
                        "DpadRecyclerView didn't have focus: ${HumanReadables.describe(view)}"
                    )
                }
                assertThat(view.findContainingViewHolder(focusedView)!!.absoluteAdapterPosition)
                    .isEqualTo(focusedPosition)
            }
        }

    }

}
