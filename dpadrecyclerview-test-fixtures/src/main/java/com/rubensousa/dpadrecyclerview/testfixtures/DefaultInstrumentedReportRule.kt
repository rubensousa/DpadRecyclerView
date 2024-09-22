package com.rubensousa.dpadrecyclerview.testfixtures

import com.rubensousa.carioca.android.report.InstrumentedReportRule
import com.rubensousa.carioca.android.report.recording.RecordingOptions

class DefaultInstrumentedReportRule : InstrumentedReportRule(
    recordingOptions = RecordingOptions(
        scale = 0.5f,
        keepOnSuccess = false,
        startDelay = 500L,
        stopDelay = 1000L,
    ),
)
