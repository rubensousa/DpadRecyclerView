package com.rubensousa.dpadrecyclerview.testfixtures

import com.rubensousa.carioca.report.android.InstrumentedReportRule
import com.rubensousa.carioca.report.android.recording.RecordingOptions

class DefaultInstrumentedReportRule(
    enableRecording: Boolean = true,
) : InstrumentedReportRule(
    recordingOptions = RecordingOptions(
        enabled = enableRecording,
        scale = 0.5f,
        keepOnSuccess = false,
        startDelay = 500L,
        stopDelay = 1000L,
    ),
)
