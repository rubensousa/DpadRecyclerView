package com.rubensousa.dpadrecyclerview.testfixtures.recording

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.test.internal.runner.listener.InstrumentationRunListener
import androidx.test.uiautomator.UiDevice
import org.junit.runner.Description
import org.junit.runner.Result
import org.junit.runner.notification.Failure
import java.io.File
import java.io.IOException
import java.io.PrintStream

@SuppressLint("RestrictedApi")
class TestRecordingListener : InstrumentationRunListener() {

    companion object {
        const val TAG = "TestRecordingListener"
    }

    private val successfulRecordings = ArrayList<String>()
    private val failedRecordings = LinkedHashSet<String>()

    override fun testRunStarted(description: Description) {
        super.testRunStarted(description)
        successfulRecordings.clear()
        failedRecordings.clear()
        val recordingDir = ScreenRecorderRule.getRecordingDir(instrumentation.targetContext)
        UiDevice.getInstance(instrumentation)
            .executeShellCommand("mkdir -p ${recordingDir.absolutePath}")
        UiDevice.getInstance(instrumentation)
            .executeShellCommand("rm ${recordingDir.absolutePath}/*")
    }

    override fun testFailure(failure: Failure) {
        super.testFailure(failure)
        failedRecordings.add(TestRecording.getFilename(failure.description))
    }

    override fun testFinished(description: Description) {
        super.testFinished(description)
        val filename = TestRecording.getFilename(description)
        if (!failedRecordings.contains(filename)) {
            successfulRecordings.add(filename)
        }
    }

    override fun instrumentationRunFinished(
        streamResult: PrintStream?,
        resultBundle: Bundle?,
        junitResults: Result?
    ) {
        val device = UiDevice.getInstance(instrumentation)
        val recordingDir = ScreenRecorderRule.getRecordingDir(instrumentation.targetContext)
        successfulRecordings.forEach { filename ->
            val file = File(recordingDir, filename)
            try {
                device.executeShellCommand("rm ${file.absolutePath}")
            } catch (exception: IOException) {
                Log.e(TAG, "Failed removal of $filename", exception)
            }
        }
    }

}
