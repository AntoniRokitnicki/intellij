package com.intellij.classHeatmap

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.table.JBTable
import javax.swing.table.DefaultTableModel

/**
 * Simple tool window that displays reference counts for project classes.
 */
class ClassHeatmapToolWindowFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val tableModel = DefaultTableModel(arrayOf("Class", "Refs"), 0)
    val table = JBTable(tableModel)
    val panel = JBScrollPane(table)

    val content = ContentFactory.getInstance().createContent(panel, "", false)
    toolWindow.contentManager.addContent(content)

    Task.Backgroundable(project, "Collecting Class Usage", true) {
      override fun run(indicator: ProgressIndicator) {
        val usages = ReferenceCountCollector.collect(project, GlobalSearchScope.projectScope(project), indicator)
        ApplicationManager.getApplication().invokeLater {
          for (info in usages.sortedByDescending { it.referenceCount }.take(100)) {
            val name = info.psiClass.qualifiedName ?: info.psiClass.name
            tableModel.addRow(arrayOf(name, info.referenceCount))
          }
        }
      }
    }.queue()
  }
}
