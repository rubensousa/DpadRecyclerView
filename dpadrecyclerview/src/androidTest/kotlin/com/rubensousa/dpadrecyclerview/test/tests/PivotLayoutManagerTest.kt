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

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragment
import androidx.recyclerview.widget.RecyclerView.LayoutManager.Properties
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadRecyclerView
import com.rubensousa.dpadrecyclerview.layoutmanager.PivotLayoutManager
import com.rubensousa.dpadrecyclerview.test.R
import com.rubensousa.dpadrecyclerview.testfixtures.DefaultInstrumentedReportRule
import org.junit.Rule
import org.junit.Test

class PivotLayoutManagerTest {

    @get:Rule(order = -1)
    val report = DefaultInstrumentedReportRule(enableRecording = false)

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

    @Test
    fun testPivotLayoutManagerFromXML() {
        // given
        val fragment = launchFragment<XMLFragment>()
        var recyclerView: DpadRecyclerView? = null

        // when
        fragment.onFragment {
            recyclerView = it.requireView() as DpadRecyclerView
        }

        // then
        assertThat(recyclerView).isNotNull()
    }

    class XMLFragment : Fragment(R.layout.dpadrecyclerview_layout_name)

}
