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

package com.rubensousa.dpadrecyclerview.testing

import android.os.SystemClock
import android.view.KeyEvent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

object KeyEvents {

    private const val DEFAULT_KEY_PRESS_DELAY = 50L

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @JvmStatic
    fun pressKey(key: Int, times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        repeat(times) {
            device.pressKeyCode(key)
            if (delay > 0) {
                SystemClock.sleep(delay)
            }
        }
    }

    @JvmStatic
    fun click(times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        pressKey(KeyEvent.KEYCODE_DPAD_CENTER, times, delay)
    }

    @JvmStatic
    fun pressDown(times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times, delay)
    }

    @JvmStatic
    fun pressUp(times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        pressKey(KeyEvent.KEYCODE_DPAD_UP, times, delay)
    }

    @JvmStatic
    fun pressLeft(times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        pressKey(KeyEvent.KEYCODE_DPAD_LEFT, times, delay)
    }

    @JvmStatic
    fun pressRight(times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times, delay)
    }

}
