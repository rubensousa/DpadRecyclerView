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

package com.rubensousa.dpadrecyclerview.test.tests.focus

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.OnViewFocusedListener
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.selectPosition
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FocusListenerTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE
            ),
            childAlignment = ChildAlignment(offset = 0)
        )
    }

    @Before
    fun setup() {
        launchFragment()
    }

    @Test
    fun testFocusListenerIsInvokedByDefault() {
        val events = getFocusEvents()
        assertThat(events).hasSize(1)
    }

    @Test
    fun testFocusListenerPassesCorrectView() {
        val event = getFocusEvents().first()
        assertThat(event.parent.absoluteAdapterPosition).isEqualTo(0)
        assertThat(event.child).isEqualTo(event.parent.itemView)
    }

    @Test
    fun testFocusListenerIsInvokedOnFocusGain() {
        // given
        executeOnFragment { fragment -> fragment.clearFocus() }

        // when
        executeOnFragment { fragment -> fragment.requestFocus() }

        // then
        assertThat(getFocusEvents()).hasSize(2)
        assertThat(getFocusEvents().last().parent.absoluteAdapterPosition).isEqualTo(0)
    }

    @Test
    fun testFocusListenerIsInvokedOnSmoothScroll() {
        // when
        selectPosition(position = 1, smooth = true)

        // then
        assertThat(getFocusEvents()).hasSize(2)
        assertThat(getFocusEvents().last().parent.absoluteAdapterPosition).isEqualTo(1)
    }

    @Test
    fun testFocusListenerIsInvokedOnKeyPress() {
        // when
        KeyEvents.pressDown()

        // then
        assertThat(getFocusEvents()).hasSize(2)
        assertThat(getFocusEvents().last().parent.absoluteAdapterPosition).isEqualTo(1)
    }

    @Test
    fun testClearFocusListeners() {
        // given
        onRecyclerView("Clear listeners") { recyclerView ->
            recyclerView.clearOnViewFocusedListeners()
        }

        // when
        KeyEvents.pressDown()

        // then
        assertThat(getFocusEvents()).hasSize(1)
        assertThat(getFocusEvents().last().parent.absoluteAdapterPosition).isEqualTo(0)
    }

    @Test
    fun testRemoveFocusListener() {
        // given
        var called = false
        val listener = object : OnViewFocusedListener {
            override fun onViewFocused(
                parent: RecyclerView.ViewHolder,
                child: View,
            ) {
                called = true
            }
        }

        onRecyclerView("Add and remove listener") { recyclerView ->
            recyclerView.addOnViewFocusedListener(listener)
            recyclerView.removeOnViewFocusedListener(listener)
        }

        // when
        KeyEvents.pressDown()

        // then
        assertThat(called).isFalse()
    }

    @Test
    fun testFocusListenerReceivesAllFocusRequests() {
        // given
        val keyPresses = 49

        // when
        KeyEvents.pressDown(keyPresses)
        waitForIdleScrollState()

        // then
        val events = getFocusEvents()
        assertThat(events).hasSize(keyPresses + 1)
        assertThat(events.map { it.position }.sorted()).isEqualTo(List(50) { it })
    }

}
