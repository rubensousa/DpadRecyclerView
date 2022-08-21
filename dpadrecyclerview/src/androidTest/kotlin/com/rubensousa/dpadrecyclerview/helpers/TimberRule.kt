package com.rubensousa.dpadrecyclerview.helpers

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import timber.log.Timber

class TimberRule : TestRule {

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                val debugTree = Timber.DebugTree()
                Timber.plant(debugTree)
                try {
                    base.evaluate()
                } finally {
                    Timber.uproot(debugTree)
                }
            }
        }
    }

}
