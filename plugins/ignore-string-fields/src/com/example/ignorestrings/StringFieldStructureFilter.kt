package com.example.ignorestrings

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.structureView.impl.java.JavaStructureViewBuilderProvider
import com.intellij.ide.structureView.newStructureView.StructureViewModel
import com.intellij.ide.structureView.newStructureView.FilteredStructureViewModel
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.CommonClassNames

class StringFieldStructureFilter : com.intellij.ide.structureView.StructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? {
        val baseBuilder = JavaStructureViewBuilderProvider().getStructureViewBuilder(psiFile) ?: return null
        return object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel {
                val model = (baseBuilder as TreeBasedStructureViewBuilder).createStructureViewModel(editor)
                return object : FilteredStructureViewModel by FilteredStructureViewModel.Wrap(model) {
                    override fun isSuitable(element: Any?): Boolean {
                        if (!IgnoredStringsSettings.enabled(psiFile.project)) return true
                        val field = element as? PsiField
                        return field?.type?.equalsToText(CommonClassNames.JAVA_LANG_STRING) != true
                    }
                }
            }
        }
    }
}
