package com.rubensousa.dpadrecyclerview.helpers

import androidx.test.uiautomator.Configurator
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class FastUiAutomatorRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val previousTimeout = Configurator.getInstance().waitForIdleTimeout
                Configurator.getInstance().waitForIdleTimeout = 0L
                try {
                    base.evaluate()
                } finally {
                    Configurator.getInstance().waitForIdleTimeout = previousTimeout
                }
            }
        }
    }
}