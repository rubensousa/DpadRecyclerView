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

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.util.HumanReadables
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.DpadViewHolder
import com.rubensousa.dpadrecyclerview.SubPositionAlignment
import junit.framework.AssertionFailedError
import java.util.Collections

object DpadRecyclerViewAssertions {

    @JvmStatic
    fun isFocused(position: Int, subPosition: Int = 0): ViewAssertion {
        return FocusAssertion(position, subPosition)
    }

    @JvmStatic
    fun isSelected(position: Int, subPosition: Int = 0): ViewAssertion {
        return SelectionAssertion(position, subPosition)
    }

    private class SelectionAssertion(
        private val position: Int,
        private val subPosition: Int = 0
    ) : DpadRecyclerViewAssertion() {

        override fun check(view: DpadRecyclerView) {
            assertThat(view.getSelectedPosition()).isEqualTo(position)
            assertThat(view.getSelectedSubPosition()).isEqualTo(subPosition)
        }
    }

    private class FocusAssertion(
        private val focusedPosition: Int,
        private val focusedSubPosition: Int = 0
    ) : DpadRecyclerViewAssertion() {

        override fun check(view: DpadRecyclerView) {
            val focusedView = view.findFocus() ?: throw AssertionFailedError(
                "DpadRecyclerView didn't have focus: ${HumanReadables.describe(view)}"
            )

            val viewHolder = view.findContainingViewHolder(focusedView)
                ?: throw AssertionFailedError(
                    "ViewHolder not found for position " +
                            "$focusedPosition and sub position $focusedSubPosition"
                )

            assertThat(viewHolder.absoluteAdapterPosition).isEqualTo(focusedPosition)

            val alignments = getAlignments(viewHolder)
            if (alignments.isEmpty() && focusedSubPosition > 0) {
                throw AssertionFailedError(
                    "ViewHolder doesn't have any sub position. " +
                            "View: ${HumanReadables.describe(view)}"
                )
            } else if (alignments.isNotEmpty()) {
                val alignment = alignments[focusedSubPosition]
                val expectedView = viewHolder.itemView.findViewById<View>(
                    alignment.getFocusViewId()
                )
                assertThat(focusedView).isEqualTo(expectedView)
            }
        }

        private fun getAlignments(viewHolder: RecyclerView.ViewHolder): List<SubPositionAlignment> {
            if (viewHolder !is DpadViewHolder) {
                return Collections.emptyList()
            }
            return viewHolder.getSubPositionAlignments()
        }

    }

}
