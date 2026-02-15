package ru.otus.detekt

import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class GlobalScopeRuleTest {

    private val rule = GlobalScopeRule(TestConfig())

    @Test
    fun `should report when GlobalScope launch is used`() {
        val code = """
            import kotlinx.coroutines.GlobalScope
            import kotlinx.coroutines.launch
            
            fun test() {
                GlobalScope.launch {
                    println("Hello")
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].message).contains("Avoid using GlobalScope.launch()")
    }

    @Test
    fun `should report when GlobalScope async is used`() {
        val code = """
            import kotlinx.coroutines.GlobalScope
            import kotlinx.coroutines.async
            
            fun test() {
                GlobalScope.async {
                    "result"
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
        assertThat(findings[0].message).contains("Avoid using GlobalScope.async()")
    }

    @Test
    fun `should report with custom dispatcher`() {
        val code = """
            import kotlinx.coroutines.GlobalScope
            import kotlinx.coroutines.launch
            import kotlinx.coroutines.Dispatchers
            
            fun test() {
                GlobalScope.launch(Dispatchers.IO) {
                    println("IO operation")
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `should not report when GlobalScope is not used`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.launch
            import kotlin.coroutines.EmptyCoroutineContext
            
            fun test() {
                CoroutineScope(EmptyCoroutineContext).launch {
                    println("OK")
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `should not report when using other methods on GlobalScope`() {
        val code = """
            import kotlinx.coroutines.GlobalScope
            
            fun test() {
                // Это не launch или async, не должно триггерить
                GlobalScope.toString()
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }

    @Test
    fun `should report for chained calls`() {
        val code = """
            import kotlinx.coroutines.GlobalScope
            import kotlinx.coroutines.launch
            
            fun test() {
                GlobalScope
                    .launch {
                        println("Chained")
                    }
                    .start()
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertThat(findings).hasSize(1)
    }

    @Test
    fun `should not report when launch is called on other scope`() {
        val code = """
            import kotlinx.coroutines.launch
            import kotlinx.coroutines.CoroutineScope
            import kotlin.coroutines.EmptyCoroutineContext
            
            class MyClass {
                val scope = CoroutineScope(EmptyCoroutineContext)
                
                fun test() {
                    scope.launch {
                        println("OK")
                    }
                }
            }
        """.trimIndent()

        val findings = rule.lint(code)

        assertThat(findings).isEmpty()
    }
}
