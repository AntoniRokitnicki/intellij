package com.yourorg.methodsfs.index

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.yourorg.methodsfs.editor.MethodRow
import git4idea.repo.GitRepositoryManager

object MethodsIndexAccess {

  fun collectForFile(project: Project, repoRelativePath: String): List<MethodRow> {
    val repoRoot = guessRepoRootForProject(project) ?: return emptyList()
    val vf = repoRoot.findFileByRelativePath(repoRelativePath) ?: return emptyList()
    val psi = PsiManager.getInstance(project).findFile(vf) ?: return emptyList()
    val rows = mutableListOf<MethodRow>()

    ReadAction.run<RuntimeException> {
      psi.accept(object : JavaRecursiveElementVisitor() {
        override fun visitMethod(method: PsiMethod) {
          rows += MethodRow(
            name = method.name,
            signature = method.parameterList.text,
            element = method as NavigatablePsiElement
          )
          super.visitMethod(method)
        }
      })
    }
    return rows
  }

  fun collectForScope(project: Project, scopeId: String): List<MethodRow> {
    val scope: GlobalSearchScope = GlobalSearchScope.projectScope(project)
    val cache = PsiShortNamesCache.getInstance(project)
    val rows = ArrayList<MethodRow>(256)
    DumbService.getInstance(project).waitForSmartMode()
    val names = cache.allMethodNames
    var budget = 5000
    for (n in names) {
      if (budget-- <= 0) break
      cache.getMethodsByNameIfNotMoreThan(n, scope, 1000).forEach { m ->
        rows += MethodRow(
          name = m.name,
          signature = m.parameterList.text,
          element = m as NavigatablePsiElement
        )
      }
    }
    return rows
  }

  private fun guessRepoRootForProject(project: Project): com.intellij.openapi.vfs.VirtualFile? {
    val repos = GitRepositoryManager.getInstance(project).repositories
    return repos.firstOrNull()?.root
  }
}
