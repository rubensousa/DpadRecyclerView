package com.rubensousa.dpadrecyclerview.helpers

import android.os.SystemClock
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice

object UiAutomatorHelper {

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    fun pressKey(key: Int, times: Int = 1, delay: Long = 0) {
        repeat(times) {
            device.pressKeyCode(key)
            if (delay > 0) {
                SystemClock.sleep(delay)
            }
        }
    }

}
