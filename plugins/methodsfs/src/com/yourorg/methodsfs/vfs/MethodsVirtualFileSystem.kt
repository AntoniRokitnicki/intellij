package com.yourorg.methodsfs.vfs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.OutputStream

class MethodsVirtualFileSystem : VirtualFileSystem() {
  companion object {
    const val SCHEME = "methods"
    fun instance(): MethodsVirtualFileSystem =
      VirtualFileManager.getInstance().getFileSystem(SCHEME) as MethodsVirtualFileSystem
  }

  override fun getProtocol(): String = SCHEME

  override fun findFileByPath(path: String): VirtualFile? =
    MethodsFileRegistry.getOrCreate(path)

  override fun refresh(asynchronous: Boolean) {}

  override fun refreshAndFindFileByPath(path: String): VirtualFile? = findFileByPath(path)

  override fun addRootToWatch(url: String, watchRecursively: Boolean) = null

  override fun deleteFile(requestor: Any?, vFile: VirtualFile) { error("read only") }
  override fun moveFile(requestor: Any?, vFile: VirtualFile, newParent: VirtualFile) { error("read only") }
  override fun renameFile(requestor: Any?, vFile: VirtualFile, newName: String) { error("read only") }
  override fun createChildFile(requestor: Any?, vDir: VirtualFile, fileName: String): VirtualFile { error("read only") }
  override fun createChildDirectory(requestor: Any?, vDir: VirtualFile, dirName: String): VirtualFile { error("read only") }
  override fun copyFile(requestor: Any?, virtualFile: VirtualFile, newParent: VirtualFile, copyName: String): VirtualFile { error("read only") }
  override fun isReadOnly(): Boolean = true
}
