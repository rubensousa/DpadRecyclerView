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

package com.rubensousa.dpadrecyclerview.test.tests.recycling

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.UnboundViewPool
import com.rubensousa.dpadrecyclerview.test.TestAdapterConfiguration
import com.rubensousa.dpadrecyclerview.test.TestGridFragment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.helpers.waitForCondition
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.R
import com.rubensousa.dpadrecyclerview.testing.rules.DisableIdleTimeoutRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RecyclingTest : DpadRecyclerViewTest() {

    @get:Rule
    val idleTimeoutRule = DisableIdleTimeoutRule()

    private val viewPool = UnboundViewPool()

    override fun getDefaultAdapterConfiguration(): TestAdapterConfiguration {
        return super.getDefaultAdapterConfiguration()
            .copy(itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal)
    }

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                offset = 0,
                offsetRatio = 0.5f
            ),
            childAlignment = ChildAlignment(
                offset = 0,
                offsetRatio = 0.5f
            ),
            useCustomViewPool = true
        )
    }

    @Before
    fun setup() {
        TestGridFragment.installViewPool(viewPool)
    }

    @Test
    fun testViewsAreRecycledWhenRecyclerViewIsDetachedFromWindowAndRecycleChildrenOnDetachIsEnabled() {
        launchFragment(
            getDefaultLayoutConfiguration().copy(
                recycleChildrenOnDetach = true
            )
        )

        waitForCondition("Waiting for RecyclerView layout") { recyclerView ->
            recyclerView.layoutManager!!.itemCount > 0 && !recyclerView.isLayoutRequested
        }

        var numberOfChildren = 0
        onRecyclerView("Remove RecyclerView from window") { recyclerView ->
            numberOfChildren = recyclerView.layoutManager!!.childCount

            val parent = recyclerView.parent as ViewGroup
            parent.removeAllViews()
        }

        assertThat(viewPool.getRecycledViewCount(0)).isEqualTo(numberOfChildren)

    }

    @Test
    fun testViewsAreNotRecycledWhenRecyclerViewIsDetachedFromWindowAndRecycleChildrenOnDetachIsDisabled() {
        launchFragment(
            getDefaultLayoutConfiguration().copy(
                recycleChildrenOnDetach = false
            )
        )

        waitForCondition("Waiting for RecyclerView layout") { recyclerView ->
            recyclerView.layoutManager!!.childCount > 0 && !recyclerView.isLayoutRequested
        }

        onRecyclerView("Remove RecyclerView from window") { recyclerView ->
            val parent = recyclerView.parent as ViewGroup
            parent.removeAllViews()
        }

        assertThat(viewPool.getRecycledViewCount(0)).isEqualTo(0)
    }


}
