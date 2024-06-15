/*
 * Copyright 2024 Rúben Sousa
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

import androidx.recyclerview.widget.RecyclerView.LayoutManager.Properties
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutManager
import org.junit.Test

class PivotLayoutManagerTest {

    @Test
    fun testDefaultSpanCountIsSetThroughConstructor() {
        val properties = Properties()
        properties.spanCount = 5
        val pivotLayoutManager = PivotLayoutManager(properties)
        assertThat(pivotLayoutManager.getSpanCount()).isEqualTo(properties.spanCount)
    }

    @Test
    fun testDefaultFocusOutIsEnabledForAllProperties() {
        val pivotLayoutManager = PivotLayoutManager(Properties())
        val config = pivotLayoutManager.getConfig()
        assertThat(config.focusOutFront).isTrue()
        assertThat(config.focusOutBack).isTrue()
        assertThat(config.focusOutSideFront).isTrue()
        assertThat(config.focusOutSideBack).isTrue()
    }

}
