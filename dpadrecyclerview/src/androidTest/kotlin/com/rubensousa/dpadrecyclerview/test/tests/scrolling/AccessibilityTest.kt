/*
 * Copyright 2023 Rúben Sousa
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

package com.rubensousa.dpadrecyclerview.test.tests.scrolling

import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.assertFocusAndSelection
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForIdleScrollState
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.KeyEvents
import org.junit.Before
import org.junit.Test

class AccessibilityTest : DpadRecyclerViewTest() {

    private val spanCount = 5
    private val accessibilityNodeInfo = AccessibilityNodeInfoCompat.obtain()

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = spanCount,
            orientation = RecyclerView.VERTICAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            )
        )
    }

    @Before
    fun setup() {
        launchFragment()
        initializeAccessibility()
    }

    @Test
    fun testAccessibilityInfo() {
        assertThat(accessibilityNodeInfo.actionList.find {
            it.id == AccessibilityActionCompat.ACTION_SCROLL_DOWN.id
        }).isNotNull()
        assertThat(accessibilityNodeInfo.actionList.find {
            it.id == AccessibilityActionCompat.ACTION_SCROLL_UP.id
        }).isNull()

        repeat(10) {
            KeyEvents.pressDown()
        }
        waitForIdleScrollState()
        initializeAccessibility()

        assertThat(accessibilityNodeInfo.actionList.find {
            it.id == AccessibilityActionCompat.ACTION_SCROLL_DOWN.id
        }).isNotNull()
        assertThat(accessibilityNodeInfo.actionList.find {
            it.id == AccessibilityActionCompat.ACTION_SCROLL_UP.id
        }).isNotNull()
    }

    @Test
    fun testVerticalScroll() {
        assertFocusAndSelection(position = 0)

        performAccessibilityAction(AccessibilityAction.ACTION_SCROLL_FORWARD)
        assertFocusAndSelection(position = spanCount)

        performAccessibilityAction(AccessibilityAction.ACTION_SCROLL_BACKWARD)
        assertFocusAndSelection(position = 0)
    }

    private fun initializeAccessibility() {
        onRecyclerView("Init accessibility") { recyclerView ->
            recyclerView.compatAccessibilityDelegate?.onInitializeAccessibilityNodeInfo(
                recyclerView, accessibilityNodeInfo
            )
        }
    }

    private fun performAccessibilityAction(action: AccessibilityAction) {
        onRecyclerView("Perform accessibility action: $action") { recyclerView ->
            recyclerView.compatAccessibilityDelegate?.performAccessibilityAction(
                recyclerView, action.id, null
            )
        }
    }

}
