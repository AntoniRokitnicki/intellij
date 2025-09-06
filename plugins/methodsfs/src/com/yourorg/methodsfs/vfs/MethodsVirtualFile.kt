package com.yourorg.methodsfs.vfs

import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

internal object MethodsFileRegistry {
  private val cache = ConcurrentHashMap<String, MethodsVirtualFile>()

  fun getOrCreate(path: String): MethodsVirtualFile =
    cache.computeIfAbsent(path) { MethodsVirtualFile(it) }

  fun update(path: String, bytes: ByteArray, stamp: Long = System.currentTimeMillis()) {
    getOrCreate(path).update(bytes, stamp)
  }
}

class MethodsVirtualFile(private val pathInScheme: String) : VirtualFile() {
  @Volatile private var bytes: ByteArray = ByteArray(0)
  @Volatile private var ts: Long = System.currentTimeMillis()

  fun update(newBytes: ByteArray, stamp: Long) {
    bytes = newBytes
    ts = stamp
  }

  fun pathInScheme(): String = pathInScheme

  override fun getName(): String = pathInScheme.substringAfterLast('/')
  override fun getFileSystem() = MethodsVirtualFileSystem.instance()
  override fun getPath(): String = "${MethodsVirtualFileSystem.SCHEME}://$pathInScheme"
  override fun isWritable(): Boolean = false
  override fun isDirectory(): Boolean = false
  override fun isValid(): Boolean = true
  override fun getParent(): VirtualFile? = null
  override fun getChildren(): Array<VirtualFile> = emptyArray()
  override fun getOutputStream(requestor: Any?, newModificationStamp: Long, newTimeStamp: Long): OutputStream { error("read only") }
  override fun contentsToByteArray(): ByteArray = bytes
  override fun getTimeStamp(): Long = ts
  override fun getLength(): Long = bytes.size.toLong()
  override fun refresh(asynchronous: Boolean, recursive: Boolean, postRunnable: Runnable?) { postRunnable?.run() }
  override fun getInputStream(): InputStream = bytes.inputStream()
}
