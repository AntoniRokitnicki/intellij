package com.yourorg.zenecjava

import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class ZenecJavaViewProvider : FileEditorProvider {
  override fun accept(project: Project, file: VirtualFile): Boolean {
    val name = file.name.lowercase()
    return name == "zenec.json" || name.endsWith(".zenec.json")
  }

  override fun createEditor(project: Project, file: VirtualFile): FileEditor {
    return ZenecJavaEditor(project, file)
  }

  override fun getEditorTypeId(): String = "ZenecJavaPreview"
  override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.PLACE_AFTER_DEFAULT
}
