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

package com.rubensousa.dpadrecyclerview.test.tests

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusPosition
import com.rubensousa.dpadrecyclerview.test.helpers.assertSelectedPosition
import com.rubensousa.dpadrecyclerview.testing.KeyEvents.pressDown
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Rule
import org.junit.Test

class FocusTest : GridTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.MIN_MAX
            ),
            childAlignment = ChildAlignment(offset = 0)
        )
    }

    @Test
    fun testViewHoldersReceiveFocus() {
        launchFragment()

        repeat(5) { index ->
            assertFocusPosition(position = index)
            assertSelectedPosition(position = index)
            pressDown()
        }
    }

    @Test
    fun testViewHoldersNotFocusableDoNotReceiveFocus() {
        launchFragment(getDefaultAdapterConfiguration().copy(alternateFocus = true))

        repeat(5) { index ->
            assertFocusPosition(position = 2 * index)
            assertSelectedPosition(position = 2 * index)
            pressDown()
        }
    }
}
