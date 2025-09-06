package com.example.ignorestrings

import com.intellij.codeInsight.completion.*
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiField
import com.intellij.psi.CommonClassNames
import com.intellij.util.ProcessingContext

class StringFieldCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                if (!IgnoredStringsSettings.enabled(parameters.position.project)) return
                result.runRemainingContributors(parameters) { completionResult ->
                    val field = completionResult.lookupElement.psiElement as? PsiField
                    if (field != null && field.type.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
                        // skip String fields
                    } else {
                        result.passResult(completionResult)
                    }
                }
            }
        })
    }
}
