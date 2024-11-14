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

package com.rubensousa.dpadrecyclerview.layoutmanager

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ListExtensionTest {

    @Test
    fun `empty list does nothing`() {
        // given
        val list = emptyList<Int>()
        var invoked = false

        // when
        list.forEachReversed {
            invoked = true
        }

        // then
        assertThat(invoked).isFalse()
    }

    @Test
    fun `removing list within itself works`() {
        // given
        val list = mutableListOf("value1", "value2", "value3")

        // when
        list.forEachReversed { value ->
            list.remove(value)
        }

        // then
        assertThat(list).isEmpty()
    }

    @Test
    fun `values are iterated in reverse order`() {
        // given
        val list = mutableListOf("value1", "value2", "value3")
        val iterated = mutableListOf<String>()

        // when
        list.forEachReversed { value ->
            iterated.add(value)
        }

        // then
        assertThat(iterated).isEqualTo(list.reversed())
    }

}
