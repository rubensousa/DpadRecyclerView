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

import android.graphics.Rect
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.testing.actions.DpadRecyclerViewActions
import com.rubensousa.dpadrecyclerview.testing.actions.DpadViewActions
import org.junit.Test

class DpadViewActionsTest : RecyclerViewTest() {

    @Test
    fun testRetrievingViewBounds() {
        launchGridFragment()

        val expectedRect = Rect()
        performActions(DpadRecyclerViewActions.execute("Getting view bounds") { recyclerView ->
            recyclerView.getGlobalVisibleRect(expectedRect)
        })

        val actualRect = Rect()
        performActions(DpadViewActions.getViewBounds(actualRect))

        assertThat(actualRect).isEqualTo(expectedRect)
    }

    @Test
    fun testFocusChanges() {
        launchGridFragment()
        
        var hasFocus = true

        performActions(DpadViewActions.clearFocus())
        performActions(DpadRecyclerViewActions.execute("Checking focus state") { recyclerView ->
            hasFocus = recyclerView.hasFocus()
        })

        assertThat(hasFocus).isEqualTo(false)
        hasFocus = false

        performActions(DpadViewActions.requestFocus())
        performActions(DpadRecyclerViewActions.execute("Checking focus state") { recyclerView ->
            hasFocus = recyclerView.hasFocus()
        })

        assertThat(hasFocus).isEqualTo(true)
    }

}
