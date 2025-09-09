package com.intellij.methods.editor

import com.intellij.methods.fs.MethodsVirtualFileSystem
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.TextEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * Fallback editor that delegates to the standard text editor.
 */
class MethodsEditorProvider : FileEditorProvider {
    override fun accept(project: Project, file: VirtualFile): Boolean =
        file.fileSystem is MethodsVirtualFileSystem

    override fun createEditor(project: Project, file: VirtualFile): FileEditor =
        TextEditorProvider.getInstance().createEditor(project, file)

    override fun getEditorTypeId(): String = "methods-view"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

