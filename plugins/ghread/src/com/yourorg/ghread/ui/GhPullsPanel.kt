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

class GhPullsPanel(project: Project) : GhTablePanel() {
  private val gh = GhClient(project)
  private val owner = JBTextField().apply { toolTipText = "owner"; columns = 10 }
  private val repo = JBTextField().apply { toolTipText = "repo"; columns = 14 }
  private val state = JBTextField("all")
  private val refresh = JButton("Refresh").apply { addActionListener { load() } }
  private val bar = JToolBar().apply { isFloatable = false }

  init {
    setColumns("#", "Title", "State", "Author", "URL", "Updated")
    listOf(owner, repo, state, refresh).forEach(bar::add)
    add(bar, BorderLayout.NORTH)
    table.selectionModel.addListSelectionListener {
      val row = table.selectedRow
      if (row >= 0) BrowserUtil.browse(table.getValueAt(row, 4).toString())
    }
  }

  data class Pull(val number: Int, val title: String, val state: String, val user: User, val html_url: String, val updated_at: String)
  data class User(val login: String)

  private fun load() {
    clearRows()
    val o = owner.text.trim(); val r = repo.text.trim()
    if (o.isEmpty() || r.isEmpty()) {
      addRow("info", "set owner/repo", "", "", "", "")
      return
    }
    val endpoint = "/repos/$o/$r/pulls?state=${state.text.trim()}&per_page=100"
    object : Task.Backgroundable(project, "Loading pulls") {
      override fun run(indicator: ProgressIndicator) {
        val res = gh.apiJson(endpoint, paginate = true)
        if (!res.ok) {
          SwingUtilities.invokeLater { addRow("ERROR", res.err, "", "", "", "") }
          return
        }
        val items: List<Pull> = gh.parseJson(res.out)
        SwingUtilities.invokeLater {
          items.forEach { p -> addRow(p.number, p.title, p.state, p.user.login, p.html_url, p.updated_at) }
        }
      }
    }.queue()
  }
}
