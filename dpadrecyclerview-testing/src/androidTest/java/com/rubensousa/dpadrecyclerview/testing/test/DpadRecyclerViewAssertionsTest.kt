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

package com.rubensousa.dpadrecyclerview.testing.test

import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadViewAssertions
import org.junit.Test

class DpadRecyclerViewAssertionsTest : RecyclerViewTest() {

    @Test
    fun testCurrentFocusPosition() {
        launchGridFragment()

        // First item is focused by default
        assert(DpadRecyclerViewAssertions.isFocused(position = 0))
        assert(DpadViewAssertions.hasFocus())

        performActions(DpadViewActions.clearFocus())

        assert(DpadViewAssertions.isNotFocused())
        assert(DpadViewAssertions.doesNotHaveFocus())
    }

    @Test
    fun testCurrentPosition() {
        launchSubPositionFragment()

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 3),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 3))

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 3, subPosition = 1, smooth = false),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 3, subPosition = 1))

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 3, subPosition = 2),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 3, subPosition = 2))

        performActions(
            DpadRecyclerViewActions.selectPosition(position = 6, subPosition = 1),
            DpadRecyclerViewActions.waitForIdleScroll()
        )
        assert(DpadRecyclerViewAssertions.isSelected(position = 6, subPosition = 1))
    }

}
