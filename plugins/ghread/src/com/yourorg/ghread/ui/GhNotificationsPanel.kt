package com.yourorg.ghread.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.yourorg.ghread.core.GhClient
import com.yourorg.ghread.ui.common.GhTablePanel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class GhNotificationsPanel(project: Project) : GhTablePanel() {
  private val gh = GhClient(project)
  private val refresh = JButton("Refresh").apply { addActionListener { load() } }
  private val bar = JToolBar().apply { isFloatable = false }

  init {
    setColumns("Reason", "Repository", "Subject", "Type", "URL", "Updated")
    bar.add(refresh)
    add(bar, BorderLayout.NORTH)
    table.selectionModel.addListSelectionListener {
      val row = table.selectedRow
      if (row >= 0) BrowserUtil.browse(table.getValueAt(row, 4).toString())
    }
  }

  data class Notification(val reason: String, val repository: Repo, val subject: Subject, val updated_at: String)
  data class Repo(val full_name: String)
  data class Subject(val title: String, val url: String?, val type: String?)

  private fun subjectHtmlUrl(url: String?): String =
    url?.replace("api.github.com/repos", "github.com")?.replace("/pulls/", "/pull/") ?: ""

  private fun load() {
    clearRows()
    object : Task.Backgroundable(project, "Loading notifications") {
      override fun run(indicator: ProgressIndicator) {
        val res = gh.apiJson("/notifications?all=true", paginate = true)
        if (!res.ok) {
          SwingUtilities.invokeLater { addRow("ERROR", res.err, "", "", "", "") }
          return
        }
        val items: List<Notification> = gh.parseJson(res.out)
        SwingUtilities.invokeLater {
          items.forEach { n ->
            addRow(n.reason, n.repository.full_name, n.subject.title, n.subject.type ?: "", subjectHtmlUrl(n.subject.url), n.updated_at)
          }
        }
      }
    }.queue()
  }
}
