package com.yourorg.ghread.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class OpenGhToolWindowAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val tw = ToolWindowManager.getInstance(e.project!!).getToolWindow("GitHub Read")
    tw?.activate(null, true)
  }
}
