package com.yourorg.methodsfs.vfs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VfsUtilCore
import git4idea.repo.GitRepositoryManager

object MethodsUrlService {
  fun byFileUrl(project: Project, vf: VirtualFile): String {
    val repo = GitRepositoryManager.getInstance(project).getRepositoryForFileQuick(vf)
      ?: error("not in git repo")
    val rel = VfsUtilCore.getRelativePath(vf, repo.root)
      ?: error("file outside repo root")
    return "by-file/$rel"
  }

  fun byScopeUrl(scopeId: String): String = "by-scope/$scopeId"

  fun resolve(url: String): VirtualFile? =
    VirtualFileManager.getInstance().findFileByUrl("${MethodsVirtualFileSystem.SCHEME}://$url")
}
