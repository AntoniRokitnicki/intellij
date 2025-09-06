package com.yourorg.methodsfs.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFileManager
import com.yourorg.methodsfs.vfs.MethodsUrlService
import com.yourorg.methodsfs.vfs.MethodsVirtualFileSystem

class OpenMethodsForCurrentFileAction : AnAction("Open Methods for File") {
  override fun actionPerformed(e: AnActionEvent) {
    val project = e.project ?: return
    val current = FileEditorManager.getInstance(project).selectedFiles.firstOrNull() ?: return
    val url = MethodsUrlService.byFileUrl(project, current)
    val vf = VirtualFileManager.getInstance().findFileByUrl("${MethodsVirtualFileSystem.SCHEME}://$url") ?: return
    FileEditorManager.getInstance(project).openFile(vf, true)
  }
}
