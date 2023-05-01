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

package com.rubensousa.dpadrecyclerview.testing.assertions

import android.view.View
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.ViewAssertion
import com.google.common.truth.Truth.assertThat

object DpadViewAssertions {

    @JvmStatic
    fun isNotFocused(): ViewAssertion = FocusedAssertion(isFocused = false)

    @JvmStatic
    fun isFocused(): ViewAssertion = FocusedAssertion(isFocused = true)

    @JvmStatic
    fun doesNotHaveFocus(): ViewAssertion = HasFocusAssertion(hasFocus = false)

    @JvmStatic
    fun hasFocus(): ViewAssertion = HasFocusAssertion(hasFocus = true)

    private class FocusedAssertion(private val isFocused: Boolean) : ViewAssertion {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (noViewFoundException != null) {
                throw noViewFoundException
            }
            assertThat(requireNotNull(view).isFocused).isEqualTo(isFocused)
        }
    }

    private class HasFocusAssertion(private val hasFocus: Boolean) : ViewAssertion {
        override fun check(view: View?, noViewFoundException: NoMatchingViewException?) {
            if (noViewFoundException != null) {
                throw noViewFoundException
            }
            assertThat(requireNotNull(view).hasFocus()).isEqualTo(hasFocus)
        }
    }

}
