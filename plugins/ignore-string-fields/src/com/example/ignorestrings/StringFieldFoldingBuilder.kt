package com.example.ignorestrings

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.impl.source.tree.JavaElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.CommonClassNames

class StringFieldFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        if (!IgnoredStringsSettings.enabled(root.project)) return emptyArray()
        val descriptors = mutableListOf<FoldingDescriptor>()
        root.accept(object : JavaRecursiveElementVisitor() {
            override fun visitField(field: PsiField) {
                if (field.type.equalsToText(CommonClassNames.JAVA_LANG_STRING)) {
                    descriptors += FoldingDescriptor(field.node, field.textRange)
                }
            }
        })
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "/* String field hidden */"

    override fun isCollapsedByDefault(node: ASTNode): Boolean = true
}
