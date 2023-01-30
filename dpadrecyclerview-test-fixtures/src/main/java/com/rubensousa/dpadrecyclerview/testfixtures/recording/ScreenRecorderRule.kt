package com.rubensousa.dpadrecyclerview.testfixtures.recording

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

class ScreenRecorderRule : TestWatcher() {

    companion object {

        const val TAG = "TestScreenRecorder"

        private const val RECORDING_DIR_NAME = "recordings"

        fun getRecordingDir(context: Context): File {
            return File("storage/emulated/0", "$RECORDING_DIR_NAME/${context.packageName}")
        }
    }

    private val screenRecordingDir = getRecordingDir(
        InstrumentationRegistry.getInstrumentation().targetContext
    )
    private val executor = Executors.newSingleThreadExecutor()
    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    override fun starting(description: Description) {
        super.starting(description)
        executor.execute {
            startScreenRecording(getScreenRecordingFile(description))
        }
    }

    override fun failed(e: Throwable, description: Description) {
        super.failed(e, description)
        stopScreenRecording(delete = false, getScreenRecordingFile(description))
    }

    override fun succeeded(description: Description) {
        super.succeeded(description)
        stopScreenRecording(delete = true, getScreenRecordingFile(description))
    }

    @WorkerThread
    private fun startScreenRecording(file: File) {
        try {
            device.executeShellCommand("screenrecord --size 1280x720 ${file.absolutePath}")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopScreenRecording(delete: Boolean, file: File) {
        try {
            if (delete) {
                device.executeShellCommand("pkill -9 screenrecord")
                device.executeShellCommand("rm ${file.absolutePath}")
            } else {
                Log.i(TAG, "Saving recording: ${file.absolutePath}")
                device.executeShellCommand("pkill -2 screenrecord")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getScreenRecordingFile(description: Description): File {
        return File(screenRecordingDir, TestRecording.getFilename(description))
    }

}
