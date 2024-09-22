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
import android.view.InputDevice
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.test.espresso.Espresso
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.rubensousa.dpadrecyclerview.testing.matchers.FocusedRootMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import kotlin.math.max

object KeyEvents {

    private const val DEFAULT_KEY_PRESS_DELAY = 50L

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @JvmStatic
    fun pressKey(key: Int, times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        repeat(times) {
            device.pressKeyCode(key)
            if (times > 1) {
                val actualDelay = max(25L, delay)
                SystemClock.sleep(actualDelay)
            }
        }
    }

    @JvmStatic
    fun click(times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        pressKey(KeyEvent.KEYCODE_DPAD_CENTER, times, delay)
    }

    @JvmStatic
    fun longClick() {
        Espresso.onView(ViewMatchers.isFocused())
            .inRoot(FocusedRootMatcher())
            .perform(object : ViewAction {
                override fun getDescription(): String {
                    return "Invoking a long click using key events"
                }

                override fun getConstraints(): Matcher<View> {
                    return Matchers.any(View::class.java)
                }

                override fun perform(uiController: UiController, view: View) {
                    // First trigger a down event
                    var eventTime = SystemClock.uptimeMillis()
                    val downEvent = KeyEvent(
                        eventTime, eventTime, KeyEvent.ACTION_DOWN,
                        KeyEvent.KEYCODE_DPAD_CENTER, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
                        InputDevice.SOURCE_DPAD
                    )
                    uiController.injectKeyEvent(downEvent)

                    // Now wait until the timeout for long press is reached
                    val longPressTimeout = (ViewConfiguration.getLongPressTimeout() * 1.5f).toLong()
                    uiController.loopMainThreadForAtLeast(longPressTimeout)

                    // Then finally send the up event that will trigger the long click
                    eventTime = SystemClock.uptimeMillis()
                    val upEvent = KeyEvent(
                        eventTime, eventTime, KeyEvent.ACTION_UP,
                        KeyEvent.KEYCODE_DPAD_CENTER, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
                        InputDevice.SOURCE_DPAD
                    )
                    uiController.injectKeyEvent(upEvent)
                }
            })
    }

    @JvmStatic
    fun back(times: Int = 1, delay: Long = DEFAULT_KEY_PRESS_DELAY) {
        pressKey(KeyEvent.KEYCODE_BACK, times, delay)
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
