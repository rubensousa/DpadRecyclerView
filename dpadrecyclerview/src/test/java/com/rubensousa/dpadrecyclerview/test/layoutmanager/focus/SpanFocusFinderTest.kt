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

import com.google.common.truth.Truth.assertThat
import com.rubensousa.dpadrecyclerview.DpadSpanSizeLookup
import com.rubensousa.dpadrecyclerview.layoutmanager.focus.SpanFocusFinder
import org.junit.Before
import org.junit.Test

class SpanFocusFinderTest {

    private val itemCount = 1000
    private val spanCount = 5
    private val finder = SpanFocusFinder()
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
    private val multipleHeadersLookup = object : DpadSpanSizeLookup() {
        override fun getSpanSize(position: Int): Int {
            return if (position.rem(spanCount + 1) == 0 || position == 0) {
                spanCount
            } else {
                1
            }
        }
    }
    private val secondHeaderPosition = spanCount + 1

    @Before
    fun setup() {
        finder.reset(newSpanCount = spanCount)
    }

    @Test
    fun `span focus is saved for single span items`() {
        repeat(spanCount) { spanIndex ->
            val position = spanIndex + 1
            finder.updateFocus(position, headerSpanSizeLookup)
            assertThat(finder.getCachedSpanIndex()).isEqualTo(
                headerSpanSizeLookup.getSpanIndex(position, spanCount)
            )
        }
    }

    @Test
    fun `span focus is kept saved for single span items if full span item gains focus`() {
        val itemPosition = headerPosition + 1
        finder.updateFocus(position = itemPosition, headerSpanSizeLookup)
        finder.updateFocus(position = headerPosition, headerSpanSizeLookup)

        assertThat(
            finder.getCachedSpanIndex()
        ).isEqualTo(headerSpanSizeLookup.getSpanIndex(itemPosition, spanCount))
    }

    @Test
    fun `span index is saved when focus changes between different span sizes`() {
        repeat(spanCount) { index ->
            val itemPosition = headerPosition + 1 + index
            finder.updateFocus(itemPosition, headerSpanSizeLookup)
            finder.updateFocus(headerPosition, headerSpanSizeLookup)
            assertThat(
                finder.getCachedSpanIndex()
            ).isEqualTo(headerSpanSizeLookup.getSpanIndex(itemPosition, spanCount))
        }
    }

    @Test
    fun `next adapter position is returned for saved span indexes`() {
        repeat(spanCount) { index ->
            val itemPosition = headerPosition + 1 + index
            finder.updateFocus(itemPosition, headerSpanSizeLookup)
            finder.updateFocus(headerPosition, headerSpanSizeLookup)
            assertThat(
                finder.findNextSpanPosition(
                    focusedPosition = headerPosition,
                    spanSizeLookup = headerSpanSizeLookup,
                    forward = true,
                    edgePosition = itemCount - 1,
                    reverseLayout = false
                )
            ).isEqualTo(itemPosition)
        }
    }

    @Test
    fun `previous adapter position is returned for saved span indexes`() {
        repeat(spanCount) { index ->
            val itemPosition = headerPosition + 1 + index
            finder.updateFocus(itemPosition, multipleHeadersLookup)
            finder.updateFocus(secondHeaderPosition, multipleHeadersLookup)
            assertThat(
                finder.findNextSpanPosition(
                    focusedPosition = secondHeaderPosition,
                    spanSizeLookup = headerSpanSizeLookup,
                    forward = false,
                    edgePosition = 0,
                    reverseLayout = false
                )
            ).isEqualTo(itemPosition)
        }
    }

    @Test
    fun `previous adapter position is returned for saved span indexes in reverse layout`() {
        repeat(spanCount) { index ->
            val itemPosition = headerPosition + 1 + index
            finder.updateFocus(itemPosition, multipleHeadersLookup)
            finder.updateFocus(secondHeaderPosition, multipleHeadersLookup)
            assertThat(
                finder.findNextSpanPosition(
                    focusedPosition = secondHeaderPosition,
                    spanSizeLookup = headerSpanSizeLookup,
                    forward = false,
                    edgePosition = 0,
                    reverseLayout = true
                )
            ).isEqualTo(itemPosition)
        }
    }

}
