package com.rubensousa.dpadrecyclerview.testfixtures.recording

import android.os.SystemClock
import androidx.annotation.WorkerThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class TestScreenRecorder : TestWatcher() {

    companion object {
        private val RECORDING_DIR_NAME = "recordings"

        fun getRecordingDir(): File {
            return File("storage/emulated/0", RECORDING_DIR_NAME)
        }
    }

    private val screenRecordingDir = getRecordingDir()
    private val executor = Executors.newSingleThreadExecutor()
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    override fun starting(description: Description) {
        super.starting(description)
        executor.execute {
            startScreenRecording(TestRecording.getFilename(description))
        }
    }

    override fun failed(e: Throwable, description: Description) {
        super.failed(e, description)
        stopScreenRecording(delete = false, TestRecording.getFilename(description))
    }

    override fun succeeded(description: Description) {
        super.succeeded(description)
        stopScreenRecording(delete = true, TestRecording.getFilename(description))
    }

    @WorkerThread
    private fun startScreenRecording(filename: String) {
        val file = File(screenRecordingDir, filename)
        try {
            device.executeShellCommand("screenrecord --size 1280x720 ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopScreenRecording(delete: Boolean, filename: String) {
        try {
            if (delete) {
                device.executeShellCommand("pkill -9 screenrecord")
                val file = File(screenRecordingDir, filename)
                device.executeShellCommand("rm ${file.absolutePath}")
            } else {
                device.executeShellCommand("pkill -2 screenrecord")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
