package com.wafflestudio.team2server.helper

import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.assertj.core.api.Assertions.assertThat
import org.springframework.aop.framework.ProxyFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.stereotype.Component
import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource

@Component
class QueryCounter : BeanPostProcessor {
    companion object {
        private val queryCount = ThreadLocal.withInitial { 0L }
    }

    override fun postProcessAfterInitialization(
        bean: Any,
        beanName: String,
    ): Any =
        if (bean is DataSource) {
            ProxyFactory(bean)
                .apply {
                    addAdvice(DataSourceInterceptor())
                }.proxy
        } else {
            bean
        }

    fun <R> assertQueryCount(
        expectedCount: Long,
        block: () -> R,
    ): R {
        clear()
        val result =
            try {
                block()
            } finally {
                val actualCount = getCount()
                clear()

                assertThat(actualCount)
                    .withFailMessage("\n[Query Count Mismatch]\nExpected: $expectedCount\nActual:   $actualCount")
                    .isEqualTo(expectedCount)
            }
        return result
    }

    private fun getCount(): Long = queryCount.get()

    private fun clear() = queryCount.remove()

    private class DataSourceInterceptor : MethodInterceptor {
        override fun invoke(invocation: MethodInvocation): Any? {
            val result = invocation.proceed()
            return if (invocation.method.name == "getConnection" && result is Connection) {
                createConnectionProxy(result)
            } else {
                result
            }
        }

        private fun createConnectionProxy(connection: Connection): Connection =
            ProxyFactory(connection)
                .apply {
                    addAdvice(ConnectionInterceptor())
                }.proxy as Connection
    }

    private class ConnectionInterceptor : MethodInterceptor {
        override fun invoke(invocation: MethodInvocation): Any? {
            val result = invocation.proceed()
            return if (result is Statement) {
                createStatementProxy(result)
            } else {
                result
            }
        }

        private fun createStatementProxy(statement: Statement): Statement =
            ProxyFactory(statement)
                .apply {
                    addAdvice(StatementInterceptor())
                }.proxy as Statement
    }

    private class StatementInterceptor : MethodInterceptor {
        override fun invoke(invocation: MethodInvocation): Any? {
            if (invocation.method.name.startsWith("execute")) {
                queryCount.set(queryCount.get() + 1)
            }
            return invocation.proceed()
        }
    }
}
