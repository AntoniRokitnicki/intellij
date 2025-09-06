package com.yourorg.ghread.ui

import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.yourorg.ghread.core.GhClient
import com.yourorg.ghread.ui.common.GhTablePanel
import java.awt.BorderLayout
import java.time.Instant
import javax.swing.JButton
import javax.swing.JToolBar
import javax.swing.SwingUtilities

class GhRateLimitPanel(project: Project) : GhTablePanel() {
  private val gh = GhClient(project)
  private val refresh = JButton("Refresh").apply { addActionListener { load() } }
  private val bar = JToolBar().apply { isFloatable = false }

  init {
    setColumns("Resource", "Limit", "Remaining", "Reset")
    bar.add(refresh)
    add(bar, BorderLayout.NORTH)
  }

  data class RateLimit(val resources: Resources)
  data class Resources(val core: Limit, val graphql: Limit)
  data class Limit(val limit: Int, val remaining: Int, val reset: Long)

  private fun load() {
    clearRows()
    object : Task.Backgroundable(project, "Loading rate limit") {
      override fun run(indicator: ProgressIndicator) {
        val res = gh.apiJson("/rate_limit")
        if (!res.ok) {
          SwingUtilities.invokeLater { addRow("ERROR", res.err, "", "") }
          return
        }
        val rate: RateLimit = gh.parseJson(res.out)
        SwingUtilities.invokeLater {
          fun fmt(l: Limit) = Instant.ofEpochSecond(l.reset).toString()
          addRow("core", rate.resources.core.limit, rate.resources.core.remaining, fmt(rate.resources.core))
          addRow("graphql", rate.resources.graphql.limit, rate.resources.graphql.remaining, fmt(rate.resources.graphql))
        }
      }
    }.queue()
  }
}
