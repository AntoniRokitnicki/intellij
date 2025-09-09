package com.yourorg.ghread.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.yourorg.ghread.settings.GhSettings

class GhToolWindowFactory : ToolWindowFactory {
  override fun shouldBeAvailable(project: Project): Boolean = GhSettings.getInstance().enabled

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    if (!GhSettings.getInstance().enabled) return
    val panel = GhRootPanel(project)
    val content = ContentFactory.getInstance().createContent(panel, "", false)
    toolWindow.contentManager.addContent(content)
  }
}
