package com.intellij.methods.vfs

import com.intellij.openapi.vfs.VirtualFile
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Lightweight virtual file storing generated content in-memory.
 * Designed to be extracted into a plugin in the future.
 */
class MethodsVirtualFile(private val myPath: String) : VirtualFile() {
  private val myName: String = myPath.substringAfterLast('/')
  @Volatile private var myContent: ByteArray = ByteArray(0)
  @Volatile private var myStamp: Long = 0

  fun updateContent(data: ByteArray, stamp: Long) {
    myContent = data
    myStamp = stamp
  }

  private fun fullPath(): String = "${MethodsVirtualFileSystem.PROTOCOL}://$myPath"

  override fun getName(): String = myName
  override fun getFileSystem() = MethodsVirtualFileSystem.getInstance()
  override fun getPath(): String = fullPath()
  override fun isWritable(): Boolean = false
  override fun isDirectory(): Boolean = false
  override fun isValid(): Boolean = true
  override fun getParent(): VirtualFile? = null
  override fun getChildren(): Array<VirtualFile> = EMPTY_ARRAY
  override fun getInputStream(): InputStream = ByteArrayInputStream(contentsToByteArray())

  override fun contentsToByteArray(): ByteArray {
    var data = myContent
    if (data.isEmpty()) {
      data = MethodsVirtualFileSystem.buildContent(myPath)
      updateContent(data, System.currentTimeMillis())
    }
    return data
  }

  override fun getTimeStamp(): Long = myStamp
  override fun getLength(): Long = contentsToByteArray().size.toLong()
  override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) {}
  override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream {
    throw IOException("Read only")
  }
  override fun getModificationStamp(): Long = myStamp
}
