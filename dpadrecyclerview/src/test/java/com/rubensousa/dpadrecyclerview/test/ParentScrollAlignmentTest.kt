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

package com.rubensousa.dpadrecyclerview.test

import androidx.recyclerview.widget.RecyclerView
import com.rubensousa.dpadrecyclerview.ParentAlignment
import com.rubensousa.dpadrecyclerview.layoutmanager.alignment.ParentScrollAlignment
import org.junit.Test

class ParentScrollAlignmentTest {

    private val alignment = ParentScrollAlignment()
    private val width = 1920
    private val height = 1080
    private val halfHorizontalKeyline = width / 2
    private val halfVerticalKeyline = height / 2
    private val minPadding = 16
    private val maxPadding = 24

    @Test
    fun `keyline for child near min edge is the min edge`() {
        setupVerticalAlignmentSizes()

        alignment.defaultAlignment = ParentAlignment(
            edge = ParentAlignment.Edge.MIN_MAX,
            offset = 0,
            offsetRatio = 0.5f
        )

        // Any child before height / 2 should not align to the keyline

        var viewCenter = height / 3
    }

    private fun setupVerticalAlignmentSizes() {
        alignment.setSize(width, height, RecyclerView.VERTICAL)
        alignment.setPadding(minPadding, maxPadding, minPadding, maxPadding, RecyclerView.VERTICAL)
    }
}
