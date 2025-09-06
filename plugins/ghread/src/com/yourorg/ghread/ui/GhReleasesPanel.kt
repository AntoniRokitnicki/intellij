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

class GhReleasesPanel(project: Project) : GhTablePanel() {
  private val gh = GhClient(project)
  private val owner = JBTextField().apply { toolTipText = "owner"; columns = 10 }
  private val repo = JBTextField().apply { toolTipText = "repo"; columns = 14 }
  private val refresh = JButton("Refresh").apply { addActionListener { load() } }
  private val bar = JToolBar().apply { isFloatable = false }

  init {
    setColumns("Name", "Tag", "Draft", "Pre", "URL", "Published")
    listOf(owner, repo, refresh).forEach(bar::add)
    add(bar, BorderLayout.NORTH)
    table.selectionModel.addListSelectionListener {
      val row = table.selectedRow
      if (row >= 0) BrowserUtil.browse(table.getValueAt(row, 4).toString())
    }
  }

  data class Release(
    val name: String?,
    val tag_name: String,
    val draft: Boolean,
    val prerelease: Boolean,
    val html_url: String,
    val published_at: String?
  )

  private fun load() {
    clearRows()
    val o = owner.text.trim(); val r = repo.text.trim()
    if (o.isEmpty() || r.isEmpty()) {
      addRow("info", "set owner/repo", "", "", "", "")
      return
    }
    val endpoint = "/repos/$o/$r/releases"
    object : Task.Backgroundable(project, "Loading releases") {
      override fun run(indicator: ProgressIndicator) {
        val res = gh.apiJson(endpoint, paginate = true)
        if (!res.ok) {
          SwingUtilities.invokeLater { addRow("ERROR", res.err, "", "", "", "") }
          return
        }
        val items: List<Release> = gh.parseJson(res.out)
        SwingUtilities.invokeLater {
          items.forEach { rel -> addRow(rel.name ?: "", rel.tag_name, rel.draft, rel.prerelease, rel.html_url, rel.published_at ?: "") }
        }
      }
    }.queue()
  }
}
