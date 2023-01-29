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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.mock

import androidx.recyclerview.widget.RecyclerView
import io.mockk.every
import io.mockk.mockk

class RecyclerViewStateMock {


    var hasTargetScrollPosition = false
    var remainingScrollHorizontal = 0
    var remainingScrollVertical = 0
    var isPreLayout = false
    var didStructureChange = false
    var itemCount = 1000
    var willRunPredictiveAnimations = false
    var willRunSimpleAnimations = false

    private val mock = mockk<RecyclerView.State>()

    init {
        every { mock.hasTargetScrollPosition() }.answers { hasTargetScrollPosition }
        every { mock.remainingScrollHorizontal }.answers { remainingScrollHorizontal }
        every { mock.remainingScrollVertical }.answers { remainingScrollVertical }
        every { mock.isPreLayout }.answers { isPreLayout }
        every { mock.itemCount }.answers { itemCount }
        every { mock.willRunPredictiveAnimations() }.answers { willRunPredictiveAnimations }
        every { mock.willRunSimpleAnimations() }.answers { willRunSimpleAnimations }
        every { mock.didStructureChange() }.answers { didStructureChange }
    }

    fun get(): RecyclerView.State = mock

}
