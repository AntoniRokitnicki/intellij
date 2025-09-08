package com.yourorg.methodsfs.index

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.NavigatablePsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
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
      collectKotlinFunctions(psi, rows)
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
      kotlinFunctionsByName(project, n, scope).forEach { fn ->
        val name = getKotlinName(fn)
        val signature = getKotlinParams(fn)
        rows += MethodRow(name, signature, fn as NavigatablePsiElement)
      }
    }
    return rows
  }

  private fun getKotlinName(fn: Any): String = try {
    Class.forName("org.jetbrains.kotlin.psi.KtNamedFunction").getMethod("getName").invoke(fn) as String
  } catch (_: Throwable) { "" }

  private fun getKotlinParams(fn: Any): String = try {
    val m = Class.forName("org.jetbrains.kotlin.psi.KtNamedFunction").getMethod("getValueParameterList")
    m.invoke(fn)?.toString() ?: ""
  } catch (_: Throwable) { "" }

  private fun kotlinFunctionsByName(project: Project, name: String, scope: GlobalSearchScope): Collection<Any> {
    return try {
      val indexClass = Class.forName("org.jetbrains.kotlin.idea.stubindex.KotlinFunctionShortNameIndex")
      val instance = indexClass.getMethod("getInstance").invoke(null)
      val get = indexClass.getMethod("get", String::class.java, Project::class.java, GlobalSearchScope::class.java)
      @Suppress("UNCHECKED_CAST")
      get.invoke(instance, name, project, scope) as Collection<Any>
    } catch (_: Throwable) {
      emptyList()
    }
  }

  private fun collectKotlinFunctions(psi: com.intellij.psi.PsiElement, rows: MutableList<MethodRow>) {
    try {
      val ktNamedFunctionClass = Class.forName("org.jetbrains.kotlin.psi.KtNamedFunction")
      fun visit(e: com.intellij.psi.PsiElement) {
        if (ktNamedFunctionClass.isInstance(e)) {
          val name = ktNamedFunctionClass.getMethod("getName").invoke(e) as String
          val params = ktNamedFunctionClass.getMethod("getValueParameterList").invoke(e)
          val paramText = params?.toString() ?: ""
          rows += MethodRow(name, paramText, e as NavigatablePsiElement)
        }
        for (child in e.children) visit(child)
      }
      if (ktNamedFunctionClass.packageName != "") { // just to avoid unused
        visit(psi)
      }
    } catch (_: Throwable) {
    }
  }

  private fun guessRepoRootForProject(project: Project): VirtualFile? {
    val repos = GitRepositoryManager.getInstance(project).repositories
    return repos.firstOrNull()?.root
  }
}
