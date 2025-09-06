package com.yourorg.ghread.ui

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.yourorg.ghread.core.GhClient
import com.yourorg.ghread.ui.common.GhTablePanel
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class GhGistsPanel(project: Project) : GhTablePanel() {
  private val gh = GhClient(project)
  private val userField = JBTextField().apply { toolTipText = "user (empty = authed)" }
  private val refresh = JButton("Refresh").apply { addActionListener { load() } }
  private val bar = JToolBar().apply { isFloatable = false }

  init {
    setColumns("ID", "Description", "Public", "URL", "Updated")
    bar.add(userField)
    bar.add(refresh)
    add(bar, BorderLayout.NORTH)
    table.selectionModel.addListSelectionListener {
      val row = table.selectedRow
      if (row >= 0) BrowserUtil.browse(table.getValueAt(row, 3).toString())
    }
  }

  data class Gist(val id: String, val description: String?, val html_url: String, val public: Boolean, val updated_at: String)

  private fun load() {
    clearRows()
    val user = userField.text.trim()
    val endpoint = if (user.isEmpty()) "/gists?per_page=100" else "/users/$user/gists?per_page=100"
    object : Task.Backgroundable(project, "Loading gists") {
      override fun run(indicator: ProgressIndicator) {
        val res = gh.apiJson(endpoint, paginate = true)
        if (!res.ok) {
          SwingUtilities.invokeLater { addRow("ERROR", res.err, "", "", "") }
          return
        }
        val items: List<Gist> = gh.parseJson(res.out)
        SwingUtilities.invokeLater {
          items.forEach { g -> addRow(g.id, g.description ?: "", g.public, g.html_url, g.updated_at) }
        }
      }
    }.queue()
  }
}
