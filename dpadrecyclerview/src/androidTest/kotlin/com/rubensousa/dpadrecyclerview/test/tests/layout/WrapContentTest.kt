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

package com.rubensousa.dpadrecyclerview.test.tests.layout

import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.ChildAlignment
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.test.TestLayoutConfiguration
import com.rubensousa.dpadrecyclerview.test.helpers.getItemViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.getRecyclerViewBounds
import com.rubensousa.dpadrecyclerview.test.helpers.onRecyclerView
import com.rubensousa.dpadrecyclerview.test.tests.DpadRecyclerViewTest
import com.rubensousa.dpadrecyclerview.testing.R
import org.junit.Test

class WrapContentTest : DpadRecyclerViewTest() {

    override fun getDefaultLayoutConfiguration(): TestLayoutConfiguration {
        return TestLayoutConfiguration(
            spans = 1,
            orientation = RecyclerView.HORIZONTAL,
            parentAlignment = ParentAlignment(
                edge = ParentAlignment.Edge.NONE,
                fraction = 0.5f
            ),
            childAlignment = ChildAlignment(
                fraction = 0.5f
            )
        )
    }

    @Test
    fun testHorizontalWrapContentIsReplacedWithMatchParent() {
        launchFragment(
            getDefaultLayoutConfiguration(), getDefaultAdapterConfiguration()
                .copy(
                    itemLayoutId = R.layout.dpadrecyclerview_test_item_horizontal,
                    numberOfItems = 1
                )
        )
        var rootWidth = 0
        executeOnFragment { fragment ->
            rootWidth = fragment.requireView().width
        }
        onRecyclerView("Change layout params to WRAP_CONTENT") { recyclerView ->
            recyclerView.updateLayoutParams {
                width = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        val bounds = getRecyclerViewBounds()
        assertThat(bounds.width()).isEqualTo(rootWidth)

        val childBounds = getItemViewBounds(position = 0)
        assertThat(childBounds.centerX()).isEqualTo(rootWidth / 2)
    }

    @Test
    fun testVerticalWrapContentIsReplacedWithMatchParent() {
        launchFragment(
            getDefaultLayoutConfiguration().copy(orientation = RecyclerView.VERTICAL),
            getDefaultAdapterConfiguration().copy(numberOfItems = 1)
        )
        var rootHeight = 0
        executeOnFragment { fragment ->
            rootHeight = fragment.requireView().height
        }

        onRecyclerView("Change layout params to WRAP_CONTENT") { recyclerView ->
            recyclerView.updateLayoutParams {
                height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }

        val bounds = getRecyclerViewBounds()
        assertThat(bounds.height()).isEqualTo(rootHeight)

        val childBounds = getItemViewBounds(position = 0)
        assertThat(childBounds.centerY()).isEqualTo(rootHeight / 2)
    }


}
