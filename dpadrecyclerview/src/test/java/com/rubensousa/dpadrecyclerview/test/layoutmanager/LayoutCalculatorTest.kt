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

package com.rubensousa.dpadrecyclerview.test.layoutmanager

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.layoutmanager.LayoutConfiguration
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutCalculator
import com.rubensousa.dpadrecyclerview.layoutmanager.layout.LayoutState
import org.junit.Before
import org.junit.Test

class LayoutCalculatorTest {

    private lateinit var layoutCalculator: LayoutCalculator
    private val mockState = RecyclerViewStateMock()
    private val configuration = LayoutConfiguration(RecyclerView.LayoutManager.Properties())
    private val mockLayoutInfo = LayoutInfoMock(configuration)

    @Before
    fun setup() {
        layoutCalculator = LayoutCalculator(mockLayoutInfo.get())
    }

    @Test
    fun `init sets default configuration for layout`() {
        val layoutState = LayoutState()
        configuration.setReverseLayout(true)
        mockState.isPreLayout = false
        layoutCalculator.init(layoutState, mockState.get())

        assertThat(layoutState.isPreLayout).isFalse()
        assertThat(layoutState.reverseLayout).isTrue()
    }

}
