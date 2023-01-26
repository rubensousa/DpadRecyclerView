package com.rubensousa.dpadrecyclerview.testfixtures.recording

import org.junit.runner.Description

object TestRecording {

    fun getFilename(description: Description): String {
        return description.testClass.simpleName + "_" + description.methodName + ".mp4"
    }
}