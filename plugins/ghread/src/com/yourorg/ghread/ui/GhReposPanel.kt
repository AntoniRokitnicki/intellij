package com.yourorg.ghread.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.yourorg.ghread.core.GhClient
import com.yourorg.ghread.ui.common.GhTablePanel
import org.jetbrains.annotations.VisibleForTesting
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class GhReposPanel(private val project: Project) : GhTablePanel() {
  private val gh = GhClient(project)
  private val userField = JBTextField().apply { toolTipText = "user or org (empty = authed user)" }
  private val refresh = JButton("Refresh").apply { addActionListener { load() } }
  private val bar = JToolBar().apply { isFloatable = false }

  init {
    setColumns("Name", "Full name", "Private", "URL", "Stars", "Lang")
    bar.add(userField)
    bar.add(refresh)
    add(bar, BorderLayout.NORTH)
    table.selectionModel.addListSelectionListener {
      val row = table.selectedRow
      if (row >= 0) {
        val url = table.getValueAt(row, 3)?.toString() ?: return@addListSelectionListener
        BrowserUtil.browse(url)
      }
    }
  }

  data class Repo(val name: String, val full_name: String, val private: Boolean, val html_url: String, val stargazers_count: Int, val language: String?)

  private fun load() {
    clearRows()
    val user = userField.text.trim()
    val endpoint = if (user.isEmpty()) "/user/repos?per_page=100&type=all" else "/users/$user/repos?per_page=100&type=all"
    object : Task.Backgroundable(project, "Loading repos") {
      override fun run(indicator: ProgressIndicator) {
        val res = gh.apiJson(endpoint, paginate = true, timeoutMs = 60_000)
        if (!res.ok) {
          SwingUtilities.invokeLater { addRow("ERROR", res.err, "", "", "", "") }
          return
        }
        val items: List<Repo> = gh.parseJson(res.out)
        SwingUtilities.invokeLater {
          items.forEach { r -> addRow(r.name, r.full_name, r.private, r.html_url, r.stargazers_count, r.language ?: "") }
        }
      }
    }.queue()
  }
}
