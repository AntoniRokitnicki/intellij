package com.example.ignorestrings

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.CommonClassNames

class StringFieldInspectionSuppressor : InspectionSuppressor, DumbAware {
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (!IgnoredStringsSettings.enabled(element.project)) return false
        val field = element as? PsiField ?: return false
        return field.type.equalsToText(CommonClassNames.JAVA_LANG_STRING)
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return SuppressQuickFix.EMPTY_ARRAY
    }
}
