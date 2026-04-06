package com.linguacards.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPropertyDelegate
import org.jetbrains.kotlin.psi.psiUtil.getStrictParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents

class NoMutableStateWithoutRememberRule(config: Config) : Rule(config) {

    override val issue = Issue(
        id = "NoMutableStateWithoutRemember",
        severity = Severity.CodeSmell,
        description = "mutableStateOf must be wrapped in remember to survive recomposition",
        debt = Debt.FIVE_MINS
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val calleeName = expression.calleeExpression?.text
        if (calleeName != "mutableStateOf") return

        // Пропускаем, если выражение не находится внутри composable-функции
        if (!isInsideComposable(expression)) return

        // Проверяем, обёрнуто ли в remember
        if (isWrappedInRemember(expression)) return

        // Проверяем, не является ли оно делегатом свойства (by mutableStateOf(...))
        // Для делегатов тоже нужно remember
        val propertyDelegate = expression.getStrictParentOfType<KtPropertyDelegate>()
        if (propertyDelegate != null && isInsideComposable(propertyDelegate) && !isWrappedInRemember(
                expression
            )
        ) {
            reportSmell(
                expression,
                "Delegated property using 'by mutableStateOf' must also be inside remember"
            )
            return
        }

        reportSmell(expression)
    }

    private fun isInsideComposable(element: org.jetbrains.kotlin.psi.KtElement): Boolean {
        val containingFunction = element.parents.filterIsInstance<KtNamedFunction>().firstOrNull()
            ?: return false
        return containingFunction.hasComposableAnnotation()
    }

    private fun KtNamedFunction.hasComposableAnnotation(): Boolean {
        return annotationEntries.any { it.text == "@Composable" }
    }

    private fun isWrappedInRemember(expression: KtCallExpression): Boolean {
        // Поднимаемся по дереву в поисках вызова remember(...) или remember { ... }
        var current = expression.parent
        while (current != null) {
            if (current is KtCallExpression) {
                val callee = current.calleeExpression?.text
                if (callee == "remember") {
                    return true
                }
            }
            // Также проверяем, не находится ли выражение внутри лямбды, которая передаётся в remember
            // Например: remember { mutableStateOf(0) }
            val parentCall = current.getStrictParentOfType<KtCallExpression>()
            if (parentCall?.calleeExpression?.text == "remember") {
                return true
            }
            current = current.parent
        }
        return false
    }

    private fun reportSmell(expression: KtCallExpression, customMessage: String? = null) {
        report(
            CodeSmell(
                issue = issue,
                entity = Entity.from(expression),
                message = customMessage
                    ?: "mutableStateOf must be wrapped in remember, e.g. 'val state = remember { mutableStateOf(...) }' or 'var state by remember { mutableStateOf(...) }'"
            )
        )
    }
}