package com.yourorg.methodsfs.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.yourorg.methodsfs.vfs.MethodsUrlService
import com.yourorg.methodsfs.vfs.MethodsVirtualFileSystem

class OpenMethodsForFileAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val file = FileEditorManager.getInstance(project).selectedFiles.firstOrNull() ?: return
    val url = MethodsUrlService.byFileUrl(project, file)
    val vf = MethodsUrlService.resolve(url) ?: MethodsVirtualFileSystem.instance().findFileByPath(url)
    if (vf != null) {
      FileEditorManager.getInstance(project).openFile(vf, true)
    }
  }

  override fun update(e: AnActionEvent) {
    val project = e.project
    e.presentation.isEnabledAndVisible = project != null && FileEditorManager.getInstance(project).selectedFiles.isNotEmpty()
  }
}
