package com.rubensousa.dpadrecyclerview.test.helpers

import android.os.SystemClock
import android.view.KeyEvent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

object UiAutomatorHelper {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @JvmStatic
    fun pressKey(key: Int, times: Int = 1, delay: Long = 0) {
        repeat(times) {
            device.pressKeyCode(key)
            if (delay > 0) {
                SystemClock.sleep(delay)
            }
        }
    }

    @JvmStatic
    fun pressDown(times: Int = 1, delay: Long = 0) {
        pressKey(KeyEvent.KEYCODE_DPAD_DOWN, times, delay)
    }

    @JvmStatic
    fun pressUp(times: Int = 1, delay: Long = 0) {
        pressKey(KeyEvent.KEYCODE_DPAD_UP, times, delay)
    }

    @JvmStatic
    fun pressLeft(times: Int = 1, delay: Long = 0) {
        pressKey(KeyEvent.KEYCODE_DPAD_LEFT, times, delay)
    }

    @JvmStatic
    fun pressRight(times: Int = 1, delay: Long = 0) {
        pressKey(KeyEvent.KEYCODE_DPAD_RIGHT, times, delay)
    }

}
