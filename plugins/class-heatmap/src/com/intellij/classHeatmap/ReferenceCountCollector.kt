package com.intellij.classHeatmap

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.ReferencesSearch

/**
 * Collects reference counts for all classes in the given [Project].
 */
object ReferenceCountCollector {
  data class ClassUsageInfo(val psiClass: PsiClass, val referenceCount: Int)

  /**
   * Computes reference counts for classes inside [scope].
   *
   * The operation runs sequentially and reports progress to [indicator].
   */
  fun collect(project: Project, scope: GlobalSearchScope = GlobalSearchScope.projectScope(project), indicator: ProgressIndicator? = null): List<ClassUsageInfo> {
    val cache = PsiShortNamesCache.getInstance(project)
    val names = cache.allClassNames
    val result = ArrayList<ClassUsageInfo>()
    val progress = indicator ?: ProgressManager.getInstance().progressIndicator
    for ((index, name) in names.withIndex()) {
      progress?.checkCanceled()
      progress?.fraction = index.toDouble() / names.size.toDouble()
      val classes = cache.getClassesByName(name, scope)
      for (psiClass in classes) {
        val refs = ReferencesSearch.search(psiClass, scope).findAll()
        result += ClassUsageInfo(psiClass, refs.size)
      }
    }
    return result
  }
}
