package com.rubensousa.dpadrecyclerview.test.rules

import androidx.test.uiautomator.Configurator
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

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
                }
            }
        }
    }
}