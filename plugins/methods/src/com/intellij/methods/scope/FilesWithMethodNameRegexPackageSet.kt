package com.intellij.methods.scope

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.scope.packageSet.NamedScopesHolder
import com.intellij.psi.search.scope.packageSet.PackageSetBase
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import java.util.regex.Pattern

/**
 * Package set true for files containing a method matching a default regex.
 * Designed for future extraction into a plugin.
 */
class FilesWithMethodNameRegexPackageSet : PackageSetBase(null) {
  companion object {
    private val REGEX: Pattern = Pattern.compile(".*")
  }

  override fun contains(file: VirtualFile, project: Project, holder: NamedScopesHolder): Boolean {
    if (DumbService.isDumb(project)) return false
    val psi = PsiManager.getInstance(project).findFile(file) ?: return false
    val names = CachedValuesManager.getManager(project).getCachedValue(psi) {
      val list = ArrayList<String>()
      psi.accept(object : JavaRecursiveElementVisitor() {
        override fun visitMethod(method: PsiMethod) {
          list.add(method.name)
        }
      })
      CachedValueProvider.Result.create(list, PsiModificationTracker.getInstance(project))
    }
    return names.any { REGEX.matcher(it).matches() }
  }
}
