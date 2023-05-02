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

package com.rubensousa.dpadrecyclerview.testing

import androidx.test.espresso.Espresso
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.assertions.DpadRecyclerViewAssertions
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class KeyEventsTest : RecyclerViewTest() {

    @get:Rule
    val disableIdleTimeoutRule = DisableIdleTimeoutRule()

    @Test
    fun testFastHorizontalScroll() {
        launchGridFragment()
        Espresso.onIdle()

        KeyEvents.pressRight(times = 10)
        assert(DpadRecyclerViewAssertions.isFocused(position = 10))

        performActions(DpadRecyclerViewActions.waitForIdleScroll())
        KeyEvents.pressLeft(times = 10)
        assert(DpadRecyclerViewAssertions.isFocused(position = 0))
    }

    @Test
    fun testFastVerticalScroll() {
        launchGridFragment()
        Espresso.onIdle()

        KeyEvents.pressDown(times = 5)
        assert(DpadRecyclerViewAssertions.isFocused(position = 5 * 5))

        performActions(DpadRecyclerViewActions.waitForIdleScroll())
        KeyEvents.pressUp(times = 5)
        assert(DpadRecyclerViewAssertions.isFocused(position = 0))
    }
}
