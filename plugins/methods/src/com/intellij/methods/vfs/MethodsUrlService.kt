package com.intellij.methods.vfs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager

/**
 * Utility for constructing and resolving methods:// URLs.
 * Can be moved into a plugin later.
 */
object MethodsUrlService {
  fun byFile(project: Project, file: VirtualFile): String {
    val base = project.basePath
    val path = file.path
    return if (base != null && path.startsWith(base)) {
      var rel = path.substring(base.length)
      if (rel.startsWith("/")) rel = rel.substring(1)
      "${MethodsVirtualFileSystem.PROTOCOL}://by-file/${project.name}/$rel"
    }
    else {
      "${MethodsVirtualFileSystem.PROTOCOL}://by-file/abs/$path"
    }
  }

  fun byScope(project: Project): String =
    "${MethodsVirtualFileSystem.PROTOCOL}://by-scope/${project.name}"

  fun resolve(url: String): VirtualFile? =
    VirtualFileManager.getInstance().findFileByUrl(url)
}
