package org.jetbrains.plugins.gradle.ui.dependencies

import com.intellij.openapi.editor.Document
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall

class GradleGroovyDepsExtractor {

    fun extract(file: PsiFile): List<DepRow> {
        val project = file.project
        val document: Document? = PsiDocumentManager.getInstance(project).getDocument(file)
        val result = mutableListOf<DepRow>()
        val calls = PsiTreeUtil.collectElementsOfType(file, GrMethodCall::class.java)
        for (call in calls) {
            val name = call.invokedExpression.text
            if (name != "dependencies") continue
            val closure = call.closureArguments.firstOrNull() ?: continue
            parseClosure(closure, document, result)
        }
        return result
    }

    private fun parseClosure(block: GrClosableBlock, document: Document?, result: MutableList<DepRow>) {
        for (statement in block.statements) {
            if (statement !is GrMethodCall) continue
            val configuration = statement.invokedExpression.text
            var group: String? = null
            var artifact: String? = null
            var version: String? = null
            var type = "normal"
            var extras = ""
            val arg = statement.expressionArguments.firstOrNull()
            if (arg is GrLiteral && arg.value is String) {
                val parts = (arg.value as String).split(":")
                group = parts.getOrNull(0)
                artifact = parts.getOrNull(1)
                version = parts.getOrNull(2)
            } else if (statement.namedArguments.isNotEmpty()) {
                for (na in statement.namedArguments) {
                    val name = na.labelName
                    val value = (na.expression as? GrLiteral)?.value?.toString()
                    when (name) {
                        "group" -> group = value
                        "name", "module" -> artifact = value
                        "version" -> version = value
                        else -> extras += "$name=$value; "
                    }
                }
            } else if (arg != null) {
                extras = arg.text
            }
            val line = document?.getLineNumber(statement.textOffset)?.plus(1) ?: 0
            result.add(DepRow(configuration, group, artifact, version, type, extras.trim(), line))
        }
    }
}

data class DepRow(
    val configuration: String,
    val group: String?,
    val artifact: String?,
    val version: String?,
    val type: String,
    val extras: String,
    val line: Int
)
