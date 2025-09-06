package com.example.ignorestrings

import com.intellij.find.findUsages.FindUsagesHandler
import com.intellij.find.findUsages.FindUsagesHandlerFactory
import com.intellij.find.findUsages.FindUsagesOptions
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.CommonClassNames

class StringFieldFindUsagesHandlerFactory : FindUsagesHandlerFactory() {
    override fun canFindUsages(element: PsiElement): Boolean {
        return element is PsiField && element.type.equalsToText(CommonClassNames.JAVA_LANG_STRING)
    }

    override fun createFindUsagesHandler(element: PsiElement, forHighlightUsages: Boolean): FindUsagesHandler {
        return object : FindUsagesHandler(element) {
            override fun getFindUsagesOptions(dataContext: DataContext?): FindUsagesOptions {
                return FindUsagesOptions(element.project)
            }

            override fun getPrimaryElements(): Array<PsiElement> = PsiElement.EMPTY_ARRAY
            override fun getSecondaryElements(): Array<PsiElement> = PsiElement.EMPTY_ARRAY
        }
    }
}
