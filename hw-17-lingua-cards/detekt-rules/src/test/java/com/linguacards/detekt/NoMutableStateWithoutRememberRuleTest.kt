package com.linguacards.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

class NoMutableStateWithoutRememberRuleTest {

    private val rule = NoMutableStateWithoutRememberRule(Config.empty)

    @Test
    fun `reports mutableStateOf without remember`() {
        val code = """
            @Composable
            fun MyScreen() {
                val state = mutableStateOf("")
            }
        """.trimIndent()
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 1
    }

    @Test
    fun `does not report mutableStateOf inside remember`() {
        val code = """
            @Composable
            fun MyScreen() {
                val state = remember { mutableStateOf("") }
            }
        """.trimIndent()
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports delegated mutableStateOf without remember`() {
        val code = """
            @Composable
            fun MyScreen() {
                var text by mutableStateOf("")
            }
        """.trimIndent()
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 1
    }

    @Test
    fun `does not report delegated mutableStateOf inside remember`() {
        val code = """
            @Composable
            fun MyScreen() {
                var text by remember { mutableStateOf("") }
            }
        """.trimIndent()
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }

    @Test
    fun `does not report mutableStateOf outside composable`() {
        val code = """
            class NotComposable {
                val state = mutableStateOf("")
            }
        """.trimIndent()
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }
}