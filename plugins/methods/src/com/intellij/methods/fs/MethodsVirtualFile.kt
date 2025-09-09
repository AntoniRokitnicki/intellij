package com.intellij.methods.fs

import com.intellij.testFramework.LightVirtualFile
import java.io.File

/**
 * Lightweight file backed by [MethodsVirtualFileSystem].
 */
class MethodsVirtualFile(private val myPath: String, content: String) : LightVirtualFile(File(myPath).name, content) {

    init {
        isWritable = false
    }

    override fun getPath(): String = myPath

    override fun getFileSystem(): MethodsVirtualFileSystem = MethodsVirtualFileSystem.getInstance()
}

