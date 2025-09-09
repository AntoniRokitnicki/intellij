package com.intellij.methods.fs

import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileListener
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.VirtualFileSystem
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.NonNls

import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory VFS for synthetic method listings.
 */
class MethodsVirtualFileSystem : VirtualFileSystem() {

    private val files = ConcurrentHashMap<String, MethodsVirtualFile>()

    override fun getProtocol(): String = PROTOCOL

    fun createFile(path: String, content: String): MethodsVirtualFile {
        val file = MethodsVirtualFile(path, content)
        files[path] = file
        return file
    }

    override fun findFileByPath(path: String): MethodsVirtualFile? = files[path]

    override fun refresh(asynchronous: Boolean) {}

    override fun refreshAndFindFileByPath(path: String): VirtualFile? = findFileByPath(path)

    override fun addVirtualFileListener(listener: VirtualFileListener) {}

    override fun removeVirtualFileListener(listener: VirtualFileListener) {}

    override fun deleteFile(requestor: Any?, vFile: VirtualFile) {
        throw IncorrectOperationException()
    }

    override fun moveFile(requestor: Any?, vFile: VirtualFile, newParent: VirtualFile) {
        throw IncorrectOperationException()
    }

    override fun renameFile(requestor: Any?, vFile: VirtualFile, newName: String) {
        throw IncorrectOperationException()
    }

    override fun createChildFile(requestor: Any?, vDir: VirtualFile, fileName: String): VirtualFile {
        throw IncorrectOperationException()
    }

    override fun createChildDirectory(requestor: Any?, vDir: VirtualFile, dirName: String): VirtualFile {
        throw IncorrectOperationException()
    }

    override fun copyFile(requestor: Any?, virtualFile: VirtualFile, newParent: VirtualFile, copyName: String): VirtualFile {
        throw IncorrectOperationException()
    }

    override fun isReadOnly(): Boolean = true

    companion object {
        @NonNls
        const val PROTOCOL: String = "methods"

        fun getInstance(): MethodsVirtualFileSystem =
            VirtualFileManager.getInstance().getFileSystem(PROTOCOL) as MethodsVirtualFileSystem
    }
}

