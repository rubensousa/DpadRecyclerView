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
import androidx.test.espresso.ViewAssertion
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.assertions.ViewHolderSelectedAssertion
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadViewAssertions
import com.rubensousa.dpadrecyclerview.testing.matchers.DpadRecyclerViewMatchers
import org.hamcrest.Matchers.allOf


fun selectLastPosition(smooth: Boolean = false, id: Int = R.id.recyclerView): Int {
    var selectedPosition: Int = RecyclerView.NO_POSITION
    Espresso.onView(withId(id))
        .perform(DpadRecyclerViewActions.selectLastPosition(smooth) { position ->
            selectedPosition = position
        })
    if (smooth) {
        Espresso.onView(withId(id))
            .perform(DpadRecyclerViewActions.waitForIdleScroll())
    }
    return selectedPosition
}

fun selectPosition(
    position: Int,
    subPosition: Int = 0,
    smooth: Boolean = false,
    waitForIdle: Boolean = smooth,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(withId(id))
        .perform(DpadRecyclerViewActions.selectPosition(position, subPosition, smooth))
    if (waitForIdle) {
        Espresso.onView(withId(id))
            .perform(DpadRecyclerViewActions.waitForIdleScroll())
    }
}

fun selectSubPosition(
    subPosition: Int,
    smooth: Boolean = false,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(withId(id))
        .perform(DpadRecyclerViewActions.selectSubPosition(subPosition, smooth))
    if (smooth) {
        Espresso.onView(withId(id))
            .perform(DpadRecyclerViewActions.waitForIdleScroll())
    }
}

fun assertItemAtPosition(position: Int, item: Int) {
    Espresso.onView(
        allOf(
            ViewMatchers.withText(item.toString()),
            DpadRecyclerViewMatchers.withDescendantOfItemViewAt(position)
        )
    ).check(ViewAssertions.matches(isDisplayed()))
}

fun assertIsNotFocused() {
    waitForIdleScrollState()
    waitForAnimation()
    Espresso.onView(withId(R.id.recyclerView))
        .check(DpadViewAssertions.isNotFocused())
}

fun assertIsFocused() {
    waitForIdleScrollState()
    waitForAnimation()
    Espresso.onView(withId(R.id.recyclerView))
        .check(DpadViewAssertions.isFocused())
}

fun assertFocusAndSelection(position: Int, subPosition: Int = 0, id: Int = R.id.recyclerView) {
    waitForIdleScrollState()
    waitForAnimation()
    assertSelectedPosition(position, subPosition, id)
    assertFocusPosition(position, subPosition, id)
}

fun assertSelectedPosition(position: Int, subPosition: Int = 0, id: Int = R.id.recyclerView) {
    Espresso.onView(withId(id))
        .check(DpadRecyclerViewAssertions.isSelected(position, subPosition))
}

fun assertViewHolderSelected(
    position: Int,
    isSelected: Boolean = true,
    id: Int = R.id.recyclerView
) {
    Espresso.onView(withId(id))
        .check(ViewHolderSelectedAssertion(position, isSelected))
}

fun assertFocusPosition(position: Int, subPosition: Int = 0, id: Int = R.id.recyclerView) {
    Espresso.onView(withId(id))
        .check(DpadRecyclerViewAssertions.isFocused(position, subPosition))
}

fun assertOnRecyclerView(assertion: ViewAssertion, id: Int = R.id.recyclerView) {
    Espresso.onView(withId(id))
        .check(assertion)
}


fun getItemViewBounds(position: Int, id: Int = R.id.recyclerView): Rect {
    val rect = Rect()
    Espresso.onView(withId(id))
        .perform(
            DpadRecyclerViewActions.waitForIdleScroll(),
            DpadRecyclerViewActions.getItemViewBounds(position, rect)
        )
    return rect
}

fun getRelativeItemViewBounds(position: Int, id: Int = R.id.recyclerView): Rect {
    val rect = Rect()
    Espresso.onView(withId(id))
        .perform(
            DpadRecyclerViewActions.waitForIdleScroll(),
            DpadRecyclerViewActions.getRelativeItemViewBounds(position, rect)
        )
    return rect
}

fun getRecyclerViewBounds(id: Int = R.id.recyclerView): Rect {
    val rect = Rect()
    Espresso.onView(withId(id)).perform(
        DpadRecyclerViewActions.waitForIdleScroll(),
        DpadViewActions.getViewBounds(rect)
    )
    return rect
}

fun updateParentAlignment(alignment: ParentAlignment, id: Int = R.id.recyclerView) {
    Espresso.onView(withId(id)).perform(
        DpadRecyclerViewActions.waitForIdleScroll(),
        DpadRecyclerViewActions.updateParentAlignment(alignment),
        DpadRecyclerViewActions.waitForIdleScroll()
    )
}

fun updateChildAlignment(alignment: ChildAlignment, id: Int = R.id.recyclerView) {
    Espresso.onView(withId(id)).perform(
        DpadRecyclerViewActions.waitForIdleScroll(),
        DpadRecyclerViewActions.updateChildAlignment(alignment),
        DpadRecyclerViewActions.waitForIdleScroll()
    )
}

fun waitForIdleScrollState(id: Int = R.id.recyclerView) {
    Espresso.onView(withId(id)).perform(DpadRecyclerViewActions.waitForIdleScroll())
}

fun waitForAdapterUpdate(id: Int = R.id.recyclerView) {
    Espresso.onView(withId(id)).perform(DpadRecyclerViewActions.waitForAdapterUpdate())
}

fun waitForAnimation() {
    waitForCondition("Waiting for Animations") { recyclerView ->
        val hasPendingAdapterUpdates = recyclerView.hasPendingAdapterUpdates()
                && recyclerView.adapter != null
        val isAnimationRunning = recyclerView.itemAnimator?.isRunning ?: false
        return@waitForCondition !isAnimationRunning && !hasPendingAdapterUpdates
    }
}

fun waitForCondition(
    description: String,
    condition: (recyclerView: DpadRecyclerView) -> Boolean
) {
    Espresso.onView(withId(R.id.recyclerView))
        .perform(DpadViewActions.waitForCondition(description, condition))
}

fun onRecyclerView(
    description: String,
    id: Int = R.id.recyclerView,
    action: (recyclerView: DpadRecyclerView) -> Unit
) {
    Espresso.onView(withId(id))
        .perform(DpadRecyclerViewActions.execute(description, action))
}

fun swipeVerticallyBy(dy: Int) {
    val centerCoordinatesProvider = CoordinatesProvider { view ->
        val xy = IntArray(2)
        view.getLocationOnScreen(xy)
        xy[0] = xy[0] + view.width / 2
        xy[1] = xy[1] + view.height / 2
        floatArrayOf(xy[0].toFloat(), xy[1].toFloat())
    }
    Espresso.onView(withId(R.id.recyclerView))
        .perform(
            GeneralSwipeAction(
                Swipe.FAST,
                centerCoordinatesProvider,
                { view ->
                    val location = centerCoordinatesProvider.calculateCoordinates(view)
                    location[1] = location[1] + dy
                    location
                }, Press.FINGER
            )
        )
}

