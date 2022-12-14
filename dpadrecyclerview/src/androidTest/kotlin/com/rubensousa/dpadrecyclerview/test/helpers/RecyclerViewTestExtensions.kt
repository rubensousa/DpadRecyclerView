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

package com.rubensousa.dpadrecyclerview.test.helpers

import android.graphics.Rect
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.matcher.ViewMatchers
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.assertions.ViewHolderSelectedAssertion
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions

fun selectLastPosition(smooth: Boolean = false, id: Int = R.id.recyclerView): Int {
    var selectedPosition: Int = RecyclerView.NO_POSITION
    Espresso.onView(ViewMatchers.withId(id))
        .perform(DpadRecyclerViewActions.selectLastPosition(smooth) { position ->
            selectedPosition = position
        })
    if (smooth) {
        Espresso.onView(ViewMatchers.withId(id)).perform(DpadRecyclerViewActions.waitForIdleScroll())
    }
    return selectedPosition
}

fun selectPosition(
    position: Int,
    subPosition: Int,
    smooth: Boolean = false,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(ViewMatchers.withId(id))
        .perform(DpadRecyclerViewActions.selectPosition(position, subPosition, smooth))
    if (smooth) {
        Espresso.onView(ViewMatchers.withId(id)).perform(DpadRecyclerViewActions.waitForIdleScroll())
    }
}

fun selectSubPosition(
    subPosition: Int,
    smooth: Boolean = false,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(ViewMatchers.withId(id))
        .perform(DpadRecyclerViewActions.selectSubPosition(subPosition, smooth))
    if (smooth) {
        Espresso.onView(ViewMatchers.withId(id)).perform(DpadRecyclerViewActions.waitForIdleScroll())
    }
}

fun assertSelectedPosition(position: Int, subPosition: Int = 0, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id))
        .check(DpadRecyclerViewAssertions.isSelected(position, subPosition))
}

fun assertViewHolderSelected(
    position: Int,
    isSelected: Boolean = true,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(ViewMatchers.withId(id))
        .check(ViewHolderSelectedAssertion(position, isSelected))
}

fun assertFocusPosition(position: Int, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id))
        .check(DpadRecyclerViewAssertions.isFocused(position))
}

fun getItemViewBounds(position: Int, id: Int = R.id.recyclerView): Rect {
    val rect = Rect()
    Espresso.onView(ViewMatchers.withId(id))
        .perform(
            DpadRecyclerViewActions.waitForIdleScroll(),
            DpadRecyclerViewActions.getItemViewBounds(position, rect)
        )
    return rect
}

fun getRecyclerViewBounds(id: Int = R.id.recyclerView): Rect {
    val rect = Rect()
    Espresso.onView(ViewMatchers.withId(id)).perform(
        DpadRecyclerViewActions.waitForIdleScroll(),
        DpadViewActions.getViewBounds(rect)
    )
    return rect
}

fun updateParentAlignment(alignment: ParentAlignment, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(
        DpadRecyclerViewActions.waitForIdleScroll(),
        DpadRecyclerViewActions.updateParentAlignment(alignment),
        DpadRecyclerViewActions.waitForIdleScroll()
    )
}

fun updateChildAlignment(alignment: ChildAlignment, id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(
        DpadRecyclerViewActions.waitForIdleScroll(),
        DpadRecyclerViewActions.updateChildAlignment(alignment),
        DpadRecyclerViewActions.waitForIdleScroll()
    )
}

fun waitForIdleScrollState(id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(DpadRecyclerViewActions.waitForIdleScroll())
}

fun waitForAdapterUpdate(id: Int = R.id.recyclerView) {
    Espresso.onView(ViewMatchers.withId(id)).perform(DpadRecyclerViewActions.waitForAdapterUpdate())
}

fun onRecyclerView(
    label: String,
    id: Int = R.id.recyclerView,
    action: (recyclerView: DpadRecyclerView) -> Unit
) {
    Espresso.onView(ViewMatchers.withId(id)).perform(DpadRecyclerViewActions.execute(label, action))
}
