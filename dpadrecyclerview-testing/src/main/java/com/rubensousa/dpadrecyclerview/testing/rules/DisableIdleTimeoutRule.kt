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

package com.rubensousa.dpadrecyclerview.testing.rules

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Configurator
import androidx.test.uiautomator.UiDevice
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Disables the default idle timeout of UiAutomator to speed up key events
 */
class DisableIdleTimeoutRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val previousTimeout = Configurator.getInstance().waitForIdleTimeout
                Configurator.getInstance().waitForIdleTimeout = 0L
                try {
                    base.evaluate()
                } finally {
                    Configurator.getInstance().waitForIdleTimeout = previousTimeout
                    // Wait for idle to avoid passing key events across different tests
                    UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).waitForIdle()
                }
            }
        }
    }
}