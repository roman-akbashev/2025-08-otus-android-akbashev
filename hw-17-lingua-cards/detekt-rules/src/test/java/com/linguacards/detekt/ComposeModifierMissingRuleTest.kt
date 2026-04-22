package com.linguacards.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.compileAndLint
import io.kotest.matchers.collections.shouldHaveSize
import org.junit.jupiter.api.Test

class ComposeModifierMissingRuleTest() {
    private val rule = ComposeModifierMissingRule(Config.empty)

    @Test
    fun `reports modifier in Composable fun`() {
        val code = """
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier

        @Composable
        fun Greeting() {
            Text(text = "Hello!")
        }
        """
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report in ordinal fun`() {
        val code = """
        fun Greeting() {
            println( "Hello!")
        }
        """
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report modifier with other params in Composable fun`() {
        val code = """
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier

        @Composable
        fun Greeting(
            name: String,
            modifier: Modifier
        ) {
            Text(text = "Hello!")
        }
        """
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report modifier in Composable fun`() {
        val code = """
        import androidx.compose.runtime.Composable
        import androidx.compose.ui.Modifier

        @Composable
        fun Greeting(modifier: Modifier = Modifier) {
            Text(
                text = "Hello!",
                modifier = modifier
            )
        }
        """
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }

    @Test
    fun `reports with other params in Composable fun`() {
        val code = """
        import androidx.compose.runtime.Composable

        @Composable
        fun Greeting(name: String)  {
            Text(text = "Hello!")
        }
        """
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 1
    }

    @Test
    fun `doesn't report in Composable preview`() {
        val code = """
        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.Preview

        @Preview
        @Composable
        fun Greeting(name: String)  {
            Text(text = "Hello!")
        }
        """
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }

    @Test
    fun `doesn't report Composable fun with different return type`() {
        val code = """
        import androidx.compose.runtime.Composable

        @Composable
        fun rememberMyName(name: String): String {
            return remember { name }
        }
        """
        val findings = rule.compileAndLint(code)
        findings shouldHaveSize 0
    }
}