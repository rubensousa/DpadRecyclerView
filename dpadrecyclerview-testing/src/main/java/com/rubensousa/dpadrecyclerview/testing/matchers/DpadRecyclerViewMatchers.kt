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

package com.rubensousa.dpadrecyclerview.testing.matchers

import android.view.View
import android.view.ViewParent
import androidx.recyclerview.widget.RecyclerView
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeDiagnosingMatcher

object DpadRecyclerViewMatchers {

    /**
     * @return a Matcher for a view that's inside a RecyclerView.ViewHolder at position [position]
     */
    fun withDescendantOfItemViewAt(position: Int): Matcher<View> {
        return object : TypeSafeDiagnosingMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("view that's a child of an itemView at position $position")
            }

            override fun matchesSafely(
                view: View,
                mismatchDescription: Description
            ): Boolean {
                val itemView = searchForItemView(view.parent)
                if (itemView == null ){
                    mismatchDescription.appendText("view is not a child of a RecyclerView")
                    return false
                }

                val layoutParams = itemView.layoutParams as RecyclerView.LayoutParams
                if (layoutParams.absoluteAdapterPosition == position) {
                    return true
                }

                mismatchDescription.appendText("view is at position $position instead")
                return false
            }

            private fun searchForItemView(parent: ViewParent?): View? {
                if (parent !is View) {
                    return null
                }
                val view = parent as View
                val layoutParams = view.layoutParams
                if (layoutParams is RecyclerView.LayoutParams) {
                    return parent
                }
                return searchForItemView(view.parent)
            }
        }
    }
}
