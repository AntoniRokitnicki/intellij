package com.yourorg.methodsfs.editor

import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.yourorg.methodsfs.vfs.MethodsVirtualFileSystem
import javax.swing.JComponent

class MethodsEditorProvider : FileEditorProvider {
  override fun accept(project: Project, file: VirtualFile): Boolean =
    file.fileSystem.protocol == MethodsVirtualFileSystem.SCHEME

  override fun createEditor(project: Project, file: VirtualFile): FileEditor =
    MethodsListEditor(project, file)

  override fun getEditorTypeId(): String = "MethodsListEditor"
  override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

private class MethodsListEditor(
  private val project: Project,
  private val file: VirtualFile
) : UserDataHolderBase(), FileEditor {

  private val panel = MethodsPanel(project, file)

  override fun getComponent(): JComponent = panel
  override fun getPreferredFocusedComponent(): JComponent = panel
  override fun getName(): String = "Methods"
  override fun setState(state: FileEditorState) {}
  override fun isModified(): Boolean = false
  override fun isValid(): Boolean = true
  override fun addPropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
  override fun removePropertyChangeListener(listener: java.beans.PropertyChangeListener) {}
  override fun selectNotify() { panel.refresh() }
  override fun deselectNotify() {}
  override fun getCurrentLocation(): FileEditorLocation? = null
  override fun dispose() {}
}
