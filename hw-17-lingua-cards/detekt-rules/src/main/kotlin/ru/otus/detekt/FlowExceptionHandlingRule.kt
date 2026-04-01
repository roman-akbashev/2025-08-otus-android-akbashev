package ru.otus.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression

class FlowExceptionHandlingRule : Rule {
    constructor(config: Config) : super(config)

    override val issue: Issue = Issue(
        id = javaClass.simpleName,
        severity = Severity.CodeSmell,
        description = "Flow should handle exceptions using catch() operator before calling launchIn()",
        debt = Debt.TWENTY_MINS
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (expression.calleeExpression?.text != "launchIn") return

        val hasCatch = checkChainForCatch(expression)

        if (!hasCatch) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(expression),
                    message = "Flow must handle exceptions with .catch() before .launchIn()"
                )
            )
        }
    }

    private fun checkChainForCatch(expression: KtCallExpression): Boolean {
        var dotExpression = expression.parent as? KtDotQualifiedExpression

        while (dotExpression != null) {
            val selector = dotExpression.selectorExpression as? KtCallExpression
            if (selector?.calleeExpression?.text == "catch") {
                return true
            }
            dotExpression = dotExpression.receiverExpression as? KtDotQualifiedExpression
        }

        return false
    }
}