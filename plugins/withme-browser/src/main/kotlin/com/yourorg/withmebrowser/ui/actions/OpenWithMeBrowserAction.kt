package com.yourorg.withmebrowser.ui.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

class OpenWithMeBrowserAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val tw = ToolWindowManager.getInstance(e.project!!).getToolWindow("WithMe Browser")
    tw?.activate(null, true)
  }
}
