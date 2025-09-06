package com.yourorg.withmebrowser.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.jcef.JBCefApp
import javax.swing.JLabel

class WithMeBrowserToolWindowFactory : ToolWindowFactory {
  override fun isApplicable(project: Project): Boolean = true

  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val contentFactory = ContentFactory.getInstance()
    if (!JBCefApp.isSupported()) {
      toolWindow.contentManager.addContent(
        contentFactory.createContent(
          JLabel("JCEF not supported on this platform."), "Unavailable", false
        )
      )
      return
    }
    val panel = WithMeBrowserPanel(project)
    toolWindow.contentManager.addContent(contentFactory.createContent(panel, "", false))
    panel.openHome()
  }
}
