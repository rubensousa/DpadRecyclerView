package com.rubensousa.dpadrecyclerview.testfixtures.rules

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class RepeatRule : TestRule {

    private class RepeatStatement(
        private val statement: Statement,
        private val times: Int
    ) : Statement() {

        @Throws(Throwable::class)
        override fun evaluate() {
            repeat(times) {
                statement.evaluate()
            }
        }

    }

    override fun apply(
        statement: Statement,
        description: Description
    ): Statement {
        var result = statement
        val repeat: Repeat? = description.getAnnotation(Repeat::class.java)
        if (repeat != null) {
            val times = repeat.times
            require(times > 0)
            result = RepeatStatement(statement, times)
        }
        return result
    }
}