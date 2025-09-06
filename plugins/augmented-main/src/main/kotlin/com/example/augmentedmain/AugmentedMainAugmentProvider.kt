package com.example.augmentedmain

import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class AugmentedMainAugmentProvider : PsiAugmentProvider() {
    override fun getAugments(
        element: PsiElement,
        type: Class<out PsiElement>,
        nameHint: String?,
        project: Project
    ): MutableList<PsiElement> {
        val results = mutableListOf<PsiElement>()
        if (element is PsiClass && type == PsiMethod::class.java && element.language == JavaLanguage.INSTANCE) {
            val hasMain = element.findMethodsByName("main", false).any {
                it.parameterList.parametersCount == 1 &&
                    it.parameterList.parameters[0].type.equalsToText("java.lang.String[]") &&
                    it.hasModifierProperty(PsiModifier.PUBLIC) &&
                    it.hasModifierProperty(PsiModifier.STATIC)
            }
            if (!hasMain) {
                val factory = JavaPsiFacade.getElementFactory(project)
                val className = element.qualifiedName ?: element.name ?: "Unknown"
                val methodText = """
                    public static void main(String[] args) {
                        System.out.println("Running $className");
                    }
                """.trimIndent()
                results.add(factory.createMethodFromText(methodText, element))
            }
        }
        return results
    }
}
