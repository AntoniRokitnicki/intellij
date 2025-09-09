package com.intellij.methods.vfs

import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory registry mapping path strings to [MethodsVirtualFile] instances.
 * This class can later be moved into a standalone plugin.
 */
internal object MethodsFileRegistry {
  private val files = ConcurrentHashMap<String, MethodsVirtualFile>()

  fun getOrCreate(path: String): MethodsVirtualFile =
    files.computeIfAbsent(path) { MethodsVirtualFile(it) }

  fun update(path: String, bytes: ByteArray, stamp: Long) {
    getOrCreate(path).updateContent(bytes, stamp)
  }
}
