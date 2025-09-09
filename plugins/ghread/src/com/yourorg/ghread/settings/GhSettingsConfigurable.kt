package com.yourorg.ghread.settings

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class GhSettingsConfigurable : BoundConfigurable("GitHub Read") {
  private var enabled = GhSettings.getInstance().enabled

  override fun createPanel() = panel {
    row {
      checkBox("Enable GitHub Read tool window").bindSelected(::enabled)
    }
  }

  override fun apply() {
    super.apply()
    GhSettings.getInstance().enabled = enabled
    ProjectManager.getInstance().openProjects.forEach { project ->
      ToolWindowManager.getInstance(project).getToolWindow("GitHub Read")?.setAvailable(enabled, null)
    }
  }
}
