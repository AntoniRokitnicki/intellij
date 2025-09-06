package com.yourorg.methodsfs.scope

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.scope.packageSet.CustomScopesProviderEx
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.psi.search.scope.packageSet.PackageSetBase
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.util.CachedValueProvider
import com.intellij.util.CachedValuesManager

class MethodsScopeProvider : CustomScopesProviderEx() {

  override fun getScopes(project: Project): MutableList<NamedScope> {
    val anyMethods = NamedScope(
      "FilesWithAnyMethod",
      FilesWithMethodPackageSet("FilesWithAnyMethod", regex = null)
    )
    val methodsRegex = NamedScope(
      "FilesWithMethodNameRegex",
      FilesWithMethodPackageSet("FilesWithMethodNameRegex", regex = Regex(".*"))
    )
    return mutableListOf(anyMethods, methodsRegex)
  }

  private class FilesWithMethodPackageSet(
    private val id: String,
    private val regex: Regex?
  ) : PackageSetBase(id, 0) {

    override fun getText(): String = id

    override fun contains(file: VirtualFile, project: Project, holder: FileIndexFacade): Boolean {
      val psiFile = PsiManager.getInstance(project).findFile(file) ?: return false
      val cached = CachedValuesManager.getManager(project).getCachedValue(psiFile) {
        val names = collectMethodNames(psiFile)
        CachedValueProvider.Result.create(names, psiFile)
      }
      return if (regex == null) cached.isNotEmpty() else cached.any { regex.matches(it) }
    }

    private fun collectMethodNames(psiFile: com.intellij.psi.PsiFile): List<String> {
      val out = ArrayList<String>()
      psiFile.accept(object : com.intellij.psi.JavaRecursiveElementVisitor() {
        override fun visitMethod(method: PsiMethod) {
          out += method.name
          super.visitMethod(method)
        }
      })
      return out
    }
  }
}
