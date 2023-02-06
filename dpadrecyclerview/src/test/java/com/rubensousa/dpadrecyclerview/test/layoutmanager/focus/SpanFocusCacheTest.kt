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

package com.rubensousa.dpadrecyclerview.test.layoutmanager.focus

import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.SpanFocusCache
import org.junit.Before
import org.junit.Test

class SpanFocusCacheTest {

    private val itemCount = 1000
    private val spanCount = 5
    private val cache = SpanFocusCache()
    private val headerPosition = 0
    private val headerSpanSizeLookup = object : DpadSpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return if (position == headerPosition) {
                spanCount
            } else {
                1
            }
        }
    }

    @Before
    fun setup() {
        cache.reset(newSpanCount = spanCount)
    }

    @Test
    fun `span focus is not saved for single span items`() {
        val position = 1
        cache.updateFocus(position, headerSpanSizeLookup)
        assertThat(cache.getSpanIndex(headerSpanSizeLookup.getSpanGroupIndex(position, spanCount)))
            .isEqualTo(RecyclerView.NO_POSITION)
    }

    @Test
    fun `span focus is saved for single span items if full span item is currently focused`() {
        val itemPosition = headerPosition + 1
        cache.updateFocus(position = itemPosition, headerSpanSizeLookup)
        cache.updateFocus(position = headerPosition, headerSpanSizeLookup)

        assertThat(
            cache.getSpanIndex(headerSpanSizeLookup.getSpanGroupIndex(itemPosition, spanCount))
        ).isEqualTo(headerSpanSizeLookup.getSpanIndex(itemPosition, spanCount))
    }

    @Test
    fun `span index is saved when focus changes between different span sizes`() {
        repeat(spanCount) { index ->
            val itemPosition = headerPosition + 1 + index
            cache.updateFocus(itemPosition, headerSpanSizeLookup)
            cache.updateFocus(headerPosition, headerSpanSizeLookup)
            assertThat(
                cache.getSpanIndex(headerSpanSizeLookup.getSpanGroupIndex(itemPosition, spanCount))
            ).isEqualTo(headerSpanSizeLookup.getSpanIndex(itemPosition, spanCount))
        }
    }

    @Test
    fun `next adapter position is returned for saved span indexes`() {
        repeat(spanCount) { index ->
            val itemPosition = headerPosition + 1 + index
            cache.updateFocus(itemPosition, headerSpanSizeLookup)
            cache.updateFocus(headerPosition, headerSpanSizeLookup)
            assertThat(
                cache.findNextSpanPosition(
                    focusedPosition = headerPosition,
                    spanSizeLookup = headerSpanSizeLookup,
                    forward = true,
                    itemCount = itemCount,
                    reverseLayout = false
                )
            ).isEqualTo(itemPosition)
        }
    }

}
