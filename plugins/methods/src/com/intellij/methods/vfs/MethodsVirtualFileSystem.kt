package com.intellij.methods.vfs

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.*
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.KeyedLazyInstance
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Virtual file system exposing generated method listings.
 * The implementation relies on internal APIs and can be moved to a plugin later.
 */
class MethodsVirtualFileSystem : VirtualFileSystem() {
  companion object {
    const val PROTOCOL: String = "methods"
    private const val MAX_SCOPE_ITEMS = 500
    @Volatile private var instance: MethodsVirtualFileSystem? = null
    private val registered = AtomicBoolean(false)

    fun getInstance(): MethodsVirtualFileSystem {
      ensureRegistered()
      return instance!!
    }

    fun ensureRegistered() {
      if (registered.compareAndSet(false, true)) {
        val fs = MethodsVirtualFileSystem()
        instance = fs
        VirtualFileSystem.EP_NAME.point.registerExtension(object : KeyedLazyInstance<VirtualFileSystem> {
          override fun getKey() = PROTOCOL
          override fun getInstance() = fs
        }, ApplicationManager.getApplication())
      }
    }

    internal fun buildContent(path: String): ByteArray = when {
      path.startsWith("by-file/") -> buildFileContent(path.removePrefix("by-file/"))
      path.startsWith("by-scope/") -> buildScopeContent(path.removePrefix("by-scope/"))
      else -> ByteArray(0)
    }

    private fun buildFileContent(spec: String): ByteArray {
      val text = ReadAction.compute<String, RuntimeException> {
        var project: Project? = null
        var real: VirtualFile? = null
        if (spec.startsWith("abs/")) {
          val abs = spec.removePrefix("abs/")
          real = StandardFileSystems.local().findFileByPath(abs)
          if (real != null) {
            project = ProjectLocator.getInstance().guessProjectForFile(real)
          }
        } else {
          val idx = spec.indexOf('/')
          if (idx > 0) {
            val projectName = spec.substring(0, idx)
            val rel = spec.substring(idx + 1)
            for (p in ProjectManager.getInstance().openProjects) {
              if (p.name == projectName) {
                project = p
                val base = p.basePath
                if (base != null) {
                  real = StandardFileSystems.local().findFileByPath("$base/$rel")
                }
                break
              }
            }
          }
        }
        if (project == null || real == null) return@compute ""
        val dumb = DumbService.getInstance(project)
        if (dumb.isDumb) {
          dumb.runWhenSmart {
            MethodsFileRegistry.update("by-file/$spec", buildFileContent(spec), System.currentTimeMillis())
          }
          return@compute "Indexing in progress"
        }
        val psi = PsiManager.getInstance(project).findFile(real) ?: return@compute ""
        val names = ArrayList<String>()
        psi.accept(object : JavaRecursiveElementVisitor() {
          override fun visitMethod(method: PsiMethod) {
            names.add(method.name)
          }
        })
        names.joinToString("\n")
      }
      return text.toByteArray(StandardCharsets.UTF_8)
    }

    private fun buildScopeContent(projectId: String): ByteArray {
      val text = ReadAction.compute<String, RuntimeException> {
        val project = ProjectManager.getInstance().openProjects.firstOrNull { it.name == projectId } ?: return@compute ""
        val dumb = DumbService.getInstance(project)
        if (dumb.isDumb) {
          dumb.runWhenSmart {
            MethodsFileRegistry.update("by-scope/$projectId", buildScopeContent(projectId), System.currentTimeMillis())
          }
          return@compute "Indexing in progress"
        }
        val cache = PsiShortNamesCache.getInstance(project)
        val scope = GlobalSearchScope.projectScope(project)
        val builder = StringBuilder()
        var count = 0
        for (name in cache.allMethodNames) {
          if (count >= MAX_SCOPE_ITEMS) break
          for (method in cache.getMethodsByName(name, scope)) {
            builder.append(name).append(' ').append(method.containingClass?.qualifiedName ?: "").append('\n')
            count++
            if (count >= MAX_SCOPE_ITEMS) break
          }
        }
        builder.toString().trimEnd()
      }
      return text.toByteArray(StandardCharsets.UTF_8)
    }
  }

  override fun getProtocol(): String = PROTOCOL
  override fun findFileByPath(path: String): VirtualFile? = MethodsFileRegistry.getOrCreate(path)
  override fun refresh(asynchronous: Boolean) {}
  override fun refreshAndFindFileByPath(path: String): VirtualFile? = findFileByPath(path)
  override fun addVirtualFileListener(listener: VirtualFileListener) {}
  override fun removeVirtualFileListener(listener: VirtualFileListener) {}
  override fun fireBeforeFileDeletion(file: VirtualFile, requestor: Any) {}
  override fun fireBeforeFileChange(file: VirtualFile, requestor: Any) {}
  override fun deleteFile(requestor: Any?, vFile: VirtualFile) { throw UnsupportedOperationException("read only") }
  override fun moveFile(requestor: Any?, vFile: VirtualFile, newParent: VirtualFile) { throw UnsupportedOperationException("read only") }
  override fun renameFile(requestor: Any?, vFile: VirtualFile, newName: String) { throw UnsupportedOperationException("read only") }
  override fun createChildFile(requestor: Any?, parent: VirtualFile, file: String): VirtualFile { throw UnsupportedOperationException("read only") }
  override fun createChildDirectory(requestor: Any?, parent: VirtualFile, dir: String): VirtualFile { throw UnsupportedOperationException("read only") }
  override fun copyFile(requestor: Any?, vFile: VirtualFile, newParent: VirtualFile, copyName: String): VirtualFile { throw UnsupportedOperationException("read only") }
  override fun isReadOnly(): Boolean = true
}
